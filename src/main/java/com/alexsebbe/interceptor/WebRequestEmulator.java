package com.alexsebbe.interceptor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

public class WebRequestEmulator {
	private CloseableHttpClient httpClient;
	private int counter = 0;
	public WebRequestEmulator() {
		
	}
	
	public void initialize() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		SSLContextBuilder builder = new SSLContextBuilder();
	    builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
	    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
	            builder.build());
	    httpClient = HttpClients.custom().setSSLSocketFactory(
	            sslsf).build();
	    counter = 0;
	    
	}
	
	public String doSearch(String searchString, WebSiteProperties webSiteProperties) throws IOException {
		HttpGet httpGet = new HttpGet(webSiteProperties.getAutocompleteRequestURL(searchString));
	    CloseableHttpResponse response = httpClient.execute(httpGet);
	    String returnValue = "";
	    try {
	        //System.out.println(response.getStatusLine());
	        HttpEntity entity = response.getEntity();
	        returnValue = EntityUtils.toString(entity);
	        if(counter++ % 100 == 0) {
	        	System.out.println(returnValue);
	        }
	        
	        EntityUtils.consume(entity);
	    }
	    finally {
	        response.close();
	    }
	    return returnValue;
	}
	
	public void destroy() throws IOException {
		httpClient.close();
	}
}
