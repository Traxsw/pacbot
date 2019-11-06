package com.tmobile.pacbot.azure.inventory.auth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.Azure.Authenticated;
import com.tmobile.pacman.commons.utils.CommonUtils;

@Component
public class AzureCredentialProvider {

	 /** The Constant logger. */
	static final Logger logger = LoggerFactory.getLogger(AzureCredentialProvider.class);
	Map<String,Azure> azureClients; ;
	Map<String,String> apiTokens;
	
	AzureCredentialProvider() {
		azureClients = new HashMap<>();
		apiTokens = new HashMap<>();
	}
	
	public Azure getClient(String tenant,String subscription){
		return azureClients.get(tenant+subscription);
	}
	
	public void putClient(String tenant,String subscription,Azure azure){
		azureClients.put(tenant+subscription,azure);
	}
	
	public String getToken(String tenant) {
		return apiTokens.get(tenant);
	}
	
	public void  putToken(String tenant, String token) {
		apiTokens.put(tenant,token);
	}
	/* Below methods to be moved to Commons */
	
	public  Azure authenticate(String tenant,String subscription) {
		return Azure.authenticate(getCredentials(tenant)).withSubscription(subscription);
		
	}
	
	public  Authenticated authenticate(String tenant) {
		return Azure.authenticate(getCredentials(tenant));
	}

	
	private  ApplicationTokenCredentials getCredentials(String tenant){
		String clientId = System.getProperty("azure.clientId."+tenant);
		String secret = System.getProperty("azure.secret."+tenant);
		return new ApplicationTokenCredentials(clientId, 
				tenant, secret, AzureEnvironment.AZURE);
	}
	
	public  String getAuthToken(String tenant) throws Exception {
		String url = "https://login.microsoftonline.com/%s/oauth2/token";
		
		String clientId = System.getProperty("azure.clientId."+tenant);
		String secret = System.getProperty("azure.secret."+tenant);
		
		
		Map<String,String> params = new HashMap<>();
		params.put("client_id", clientId);
		params.put("client_secret", secret);
		params.put("resource", "https://management.azure.com");
		params.put("grant_type", "client_credentials");
		url = String.format(url, tenant);
	
		try {
			String jsonResponse = CommonUtils.doHttpPost(url, params);
			Map<String,String> respMap = new Gson().fromJson(jsonResponse, new TypeToken<Map<String, String>>() {}.getType() );
			return respMap.get("access_token");
		} catch (Exception e) {
			logger.error("Error getting mangement API token from Azure",e);
			throw e;
		}
	}

	
}
