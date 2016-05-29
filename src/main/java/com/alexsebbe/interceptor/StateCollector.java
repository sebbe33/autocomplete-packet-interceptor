package com.alexsebbe.interceptor;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.alexsebbe.ChunkedSnifferRunner;
import com.alexsebbe.WebFlow;
import com.alexsebbe.WebFlowVectorFetcher;
import com.alexsebbe.WebFlowVectorFetcherExecutor;
import com.alexsebbe.WebFlowVectorReceivedListener;
import com.alexsebbe.WebSiteProperties;
import com.alexsebbe.WebState;

public class StateCollector {	
	private String alternativesString;
	private int maxDepth;
	private WebSiteProperties properties;
	private ChunkedSnifferRunner snifferRunner;
	private WebFlowVectorFetcherExecutor requestExecutor;
	
	
	public StateCollector(String alternativesString, int maxDepth, WebSiteProperties properties, int threadSize, ChunkedSnifferRunner snifferRunner, WebFlowVectorFetcherExecutor executor) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		this.alternativesString = alternativesString;
		this.maxDepth = maxDepth;
		this.properties = properties;
		this.snifferRunner = snifferRunner;
		requestExecutor = executor;
	}
	
	
	public WebState collect() throws IOException {
		WebState rootState = new WebState("", null, null, null);
		collectForChildren(rootState, 0);
		return rootState;
	}
	
	
    private void collectForChildren(WebState parentState, int currentDepth) {
    	for(char c : alternativesString.toCharArray()) {
    		String searchString = parentState.getName() + c;
    		PostFetchExecutor postFetchListener = new PostFetchExecutor(currentDepth,parentState,c+"");
    		requestExecutor.stackFetchingTask(searchString, properties, postFetchListener);
    		
    	}
    }
    
    
    private class PostFetchExecutor implements WebFlowVectorReceivedListener {
    	private int currentDepth;
    	private WebState parentState;
    	private String input;
    	
    	public PostFetchExecutor(int currentDepth, WebState parentState, String input) {
    		this.currentDepth = currentDepth;
    		this.parentState = parentState;
    		this.input = input;
    	}
    	
		public void onFailure(WebFlowVectorFetcher source) {
			
		}
		
		public void onEmptyResultResponse(WebFlowVectorFetcher source,
				String searchString) {
			
		}

		public void onNonEmptyResultResponse(WebFlowVectorFetcher source,
				List<WebFlow> webFlowVector, String searchString) {
			WebState currentWebState = new WebState(searchString, parentState, input, webFlowVector);
			parentState.addChild(currentWebState);
			
			if(currentDepth + 1 < maxDepth) {
				collectForChildren(currentWebState, currentDepth+1);
			}
			
		}
    	
    }
}
