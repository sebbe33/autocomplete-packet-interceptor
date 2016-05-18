package com.alexsebbe.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WebStateUtils {
	public static void printWebstateAndDescendants(WebState webstate) {
		System.out.println(webstate.getName());
		if(webstate.getChildren() == null) {
			return;
		}
		
		for(WebState child : webstate.getChildren()) {
			printWebstateAndDescendants(child);
		}
	}
	
	/**
	 * Retrieves all possible Web States whose transitions matches the specified list
	 * of web flow vectors. 
	 * 
	 * For example, if the web flow vectors are: [[->10, <-50],[->11, <-50]], 
	 * the the root state contains states that transitions:
	 * A --[->10, <-50]--> E --[->11, <-50]]--> F
	 * 					   G --[->11, <-50]]--> H
	 * B --[->10, <-50]--> I --[->11, <-50]]--> J
	 * C --[->10, <-50]--> K --[->11, <-50]]--> L
	 * 
	 * this method would return the states F, H, J.
	 * 
	 * @param webFlowVectors
	 * @param rootState
	 * @return matching states based on web flow vectors
	 */
	public static List<WebState> getStatesFromWebFlowVectors(List<List<WebFlow>> webFlowVectors, WebState rootState) {
		List<WebState> result = new ArrayList<WebState>();
		for(WebState child : rootState.getChildren()) {
			if(child != null)
				getPossibleInputsFromWebFlowVectorsHelper(child, webFlowVectors, 0, result);
		}
		
		return result;
	}
	
	private static void getPossibleInputsFromWebFlowVectorsHelper(WebState currentState, List<List<WebFlow>> webFlowVectors, 
			int currentDepth, List<WebState> resultSet) {
		List<WebFlow> flowVectorToMatch = webFlowVectors.get(currentDepth);
		
		if(!flowVectorToMatch.equals(currentState.getWebFlowVector())) {
			return; // No need to continue to check children since current didn't match. 
		}
		
		if(currentDepth == webFlowVectors.size() - 1) { 
			resultSet.add(currentState);
			// We have cleared the depth and come this far => we have a match
			return;
		}
		
		for(WebState child : currentState.getChildren()) {
			if(child != null)
				getPossibleInputsFromWebFlowVectorsHelper(child, webFlowVectors, currentDepth+1,resultSet);
		}
	}
	
	/**
	 * Returns the sequence of web vectors leading up to the specified web state
	 * @return sequence of web vectors
	 */
	public static List<List<WebFlow>> getWebFlowVectorSequenceForState(WebState webState) {
		List<List<WebFlow>> result = new ArrayList<List<WebFlow>>();
		WebState currentState = webState;
		do {
			result.add(currentState.getWebFlowVector());
			currentState = currentState.getParent();
		} while(currentState.getParent() != null);
		
		// Reverse list to get the correct order. The flow vectors are added beginning with the current state
		Collections.reverse(result);
		return result;
	}
}
