package com.alexsebbe.interceptor;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class OnTheFlyInterceptor {
	private WebFlowVectorFetcherExecutor executor;
	
	public OnTheFlyInterceptor(int threadCount, WebSiteProperties properties, ChunkedSnifferRunner snifferRunner) 
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		executor = new WebFlowVectorFetcherExecutor(threadCount, WebRequestEmulatorFactoryImpl.INSTANCE, properties, snifferRunner);
		
	}
	

}
