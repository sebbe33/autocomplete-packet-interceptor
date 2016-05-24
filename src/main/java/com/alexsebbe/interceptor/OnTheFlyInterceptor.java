package com.alexsebbe.interceptor;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alexsebbe.interceptor.ChunkedSnifferRunner.AutoCompletePacketsReceivedListener;
import com.alexsebbe.interceptor.WebFlow.Direction;

public class OnTheFlyInterceptor implements AutoCompletePacketsReceivedListener {
	private WebFlowVectorFetcherExecutor executor;
	private ChunkedSnifferRunner snifferRunner;
	private String alternativesString;
	private WebSiteProperties webSiteProperties;
	private Map<Integer, List<AutocompleteResult>> portsToIntercept = new HashMap<Integer, List<AutocompleteResult>>();
	private ResultReceivedListener resultReceivedListener;
	
	public OnTheFlyInterceptor(WebFlowVectorFetcherExecutor webFlowVectorExecutor, 
			ChunkedSnifferRunner snifferRunner, String alternativesString, 
			WebSiteProperties webSiteProperties, ResultReceivedListener resultReceivedListener) 
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		this.executor = webFlowVectorExecutor;
		this.snifferRunner = snifferRunner;
		this.alternativesString = alternativesString;
		this.webSiteProperties = webSiteProperties;
		this.resultReceivedListener = resultReceivedListener;
		snifferRunner.registerAutoCompletePacketsReceivedListener(this);
	}

	public void onAutoCompletePacketsReceived(int port,
			List<Integer> packetSizes) {
		if(executor.getPortsUsed().contains(port)) {
			// We should not intercept packets from one of the fetchers - only from a user
			return;
		}
		
		List<WebState> previousStates;
		AutocompleteResult currentResult;
		if(!portsToIntercept.containsKey(port)) {
			// Create new entry in hash map and add current result
			List<AutocompleteResult> resultsOnPort = new ArrayList<OnTheFlyInterceptor.AutocompleteResult>();
			currentResult = new AutocompleteResult(1);
			resultsOnPort.add(currentResult);
			portsToIntercept.put(port, resultsOnPort);
			
			// Add 'fake' root node which consists of an empty string
			previousStates = new ArrayList<WebState>();
			previousStates.add(new WebState("",null,null,null));
		} else {
			// Get last level's possible states
			List<AutocompleteResult> resultsOnPort = portsToIntercept.get(port);
			previousStates = resultsOnPort.get(resultsOnPort.size()-1).possibleStates;
			
			// Add current result
			currentResult = new AutocompleteResult(resultsOnPort.size()+1);
			resultsOnPort.add(currentResult);
		}
		
		// Convert incoming packets to web-flow vector
		List<WebFlow> webFlowVector = convertIncomingPacketSizeToWebFlowVector(packetSizes);
		// TODO : if it is not an empty packet
		
		// For each of the previous states, check possibilities based on the input just received.
		currentResult.combinationsToTry = previousStates.size()*alternativesString.length();
		for(WebState state : previousStates) {
			for(char c : alternativesString.toCharArray()) {
				executor.stackFetchingTask(state.getName()+c, webSiteProperties, new PostFetchExecutor(currentResult, webFlowVector));
			}
		}
		
		// Start new iteration on sniffer runner in order to be able to receive more packets
		System.out.println("Intercepted packets on port " + port + ". Web flow vector: " + webFlowVector + ". Calculating possible states...");
		snifferRunner.startNextIteration(port);
	}
	
	public void reset() {
		portsToIntercept.clear();
	}
	
	private class AutocompleteResult {
		private int level;
		private List<WebState> possibleStates = new ArrayList<WebState>();
		private int combinationsToTry;
		private int combinationsTried;
		
		public AutocompleteResult(int level) {
			this.level = level;
		}
		
		public synchronized void incrementCombinationsTried() {
			combinationsTried++;
			if(combinationsTried == combinationsToTry) {
				resultReceivedListener.onResultRecieved(level, possibleStates);
			}
		}
	}
	
	private List<WebFlow> convertIncomingPacketSizeToWebFlowVector(List<Integer> packetLengths) {
		List<WebFlow> webFlowVector = new ArrayList<WebFlow>();
		// Clear every packet but the last, since every search query we've observed is sent in only one packet
		// The other packets can be images or other metadata sent by amazon's autocomplete server
		webFlowVector.add(new WebFlow(Direction.INGOING, packetLengths.get(packetLengths.size()-1)));
		
		return webFlowVector;
	}
	
    private class PostFetchExecutor implements WebFlowVectorReceivedListener {
    	private AutocompleteResult result;
    	private List<WebFlow> packetSizesToMatch;
    	public PostFetchExecutor(AutocompleteResult result, List<WebFlow> packetSizesToMatch) {
    		this.result = result;
    		this.packetSizesToMatch = packetSizesToMatch;
    	}
    	
		public void onFailure(WebFlowVectorFetcher source) {
			System.out.println("Failed!");
		}
		
		public void onEmptyResultResponse(WebFlowVectorFetcher source,
				String searchString) {
			result.incrementCombinationsTried();
		}

		public void onNonEmptyResultResponse(WebFlowVectorFetcher source,
				List<WebFlow> webFlowVector, String searchString) {
			if(packetSizesToMatch.equals(webFlowVector)) {
				WebState currentWebState = new WebState(searchString, null, "", webFlowVector);
				result.possibleStates.add(currentWebState);
			}
			result.incrementCombinationsTried();
		}
    	
    }
    
    public interface ResultReceivedListener {
    	void onResultRecieved(int level, List<WebState> possibleStates);
    }
	
}
