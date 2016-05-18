package com.alexsebbe.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alexsebbe.interceptor.WebFlow.Direction;


public class StatisticsUtil {

	/**
	 * Returns the amount of valid inputs
	 * @param rootState
	 * @return #input
	 */
	public static int getAmountOfValidInput(WebState rootState) {
		return countStatesRecursively(rootState) - 1;
	}
	
	private static int countStatesRecursively(WebState currentState) {
		if(currentState.getChildren() == null || currentState.getChildren().isEmpty()) {
			return 1; // Reached a non-result entry
		}
		int result = 1;
		
		for(WebState child : currentState.getChildren()) {
			if(child != null)
				result += countStatesRecursively(child);
		}
		
		return result;
	}
	
	/**
	 * Returns the amount of possible combination, for a certain depth and alternatives.
	 * @param alternativesString
	 * @param maxDepth
	 * @return #possiblecombinations
	 */
	public static long getTotalAmountOfPossibleCombinations(String alternativesString, int maxDepth) {
		long result = 0;
		for(int i = 1; i <= maxDepth; i++) {
			result += Math.pow(alternativesString.length(), i);
		}
		
		return result;
	}
	
	
	public static double getDensity(List<WebState> webStates, Direction direction) {
		List<Integer> allPackets = new ArrayList<Integer>();
		for(WebState state : webStates) {
			if(state != null) {
				for(WebFlow flow : state.getWebFlowVector()) {
					if(flow.getDirection() == direction) {
						allPackets.add(flow.getLength());
					}
				}
			}
		}
		int min = Collections.min(allPackets);
		int max = Collections.max(allPackets);
		
		return allPackets.size()/(double)(max - min);
	}
	
	public static int getAmountOfDistinguishableInput(WebState rootState) {
		int result = 0;
		for(WebState child : rootState.getChildren()) {
			if(child != null)
				result += distinguishableInputHelper(rootState, child);
		}
		
		return result;
	}
	
	private static int distinguishableInputHelper(WebState rootState, WebState currentState) {
		List<List<WebFlow>> webFlowVectors = WebStateUtils.getWebFlowVectorSequenceForState(currentState);
		int result = WebStateUtils.getStatesFromWebFlowVectors(webFlowVectors, rootState).size() == 1? 1 : 0;

		if(currentState.getChildren() != null && !currentState.getChildren().isEmpty()) {
			for(WebState child : currentState.getChildren()) {
				if(child != null)
					result += distinguishableInputHelper(rootState, child);
			}
		}
		
		return result;
	}
	
	public static double getAveragePredictionRate(WebState rootState) {
		double result = 0;
		for(WebState child : rootState.getChildren()) {
			if(child != null)
				result += averagePredictionRateHelper(rootState, child);
		}
		
		return result/getAmountOfValidInput(rootState);
	}

	private static double averagePredictionRateHelper(WebState rootState, WebState currentState) {
		List<List<WebFlow>> webFlowVectors = WebStateUtils.getWebFlowVectorSequenceForState(currentState);
		double result = 1.0/WebStateUtils.getStatesFromWebFlowVectors(webFlowVectors, rootState).size();
		
		for(WebState child : currentState.getChildren()) {
			if(child != null)
				result += distinguishableInputHelper(rootState, child);
		}
		
		return result;
	}
	
}
