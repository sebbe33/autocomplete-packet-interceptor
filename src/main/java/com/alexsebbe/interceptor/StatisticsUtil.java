package com.alexsebbe.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	/**
	 * Returns the density for a set of web states
	 * @param webStates
	 * @param direction
	 * @return density
	 */
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
	
	/**
	 * Returns the number of distinguishable input, i.e. input whose 
	 * web-flow vector sequence is unique
	 * @param rootState
	 * @return # distinguishable input
	 */
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
	
	
	/**
	 * Returns the average prediction rate [0,1] for the profile
	 * @param rootState - the root state of the profile
	 * @return average prediction rate
	 */
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
	
	private static class DegradationResult {
		double oldProfilePercentage, newProfilePercentage;
	}
	
	/**
	 * Returns the % of degradation of a new profile, compared to an old profile
	 * @param rootOfOldProfile
	 * @param rootOfNewProfile
	 * @return degradation
	 */
	public static double getDegradationOfProfile(WebState rootOfOldProfile, WebState rootOfNewProfile) {
		DegradationResult result = new DegradationResult();
		for(WebState child : rootOfOldProfile.getChildren()) {
			if(child != null)
				degradationHelper(child, rootOfOldProfile, rootOfNewProfile, result);
		}
		System.out.println(result.newProfilePercentage +  " - " + result.oldProfilePercentage);
		return 1 - (result.newProfilePercentage/result.oldProfilePercentage);
	}
	
	private static void degradationHelper(WebState currentState, WebState rootOfOldProfile,
			WebState rootOfNewProfile, DegradationResult result) {
		List<List<WebFlow>> webFlowVectors = WebStateUtils.getWebFlowVectorSequenceForState(currentState);

		// Convert to set, in order to not regard the order in the equals method
		Set<WebState> oldStatesSet = new HashSet<WebState>(WebStateUtils.getStatesFromWebFlowVectors(webFlowVectors, rootOfOldProfile));
		Set<WebState> newStatesSet = new HashSet<WebState>(WebStateUtils.getStatesFromWebFlowVectors(webFlowVectors, rootOfNewProfile));
		
		result.oldProfilePercentage += 1.0/oldStatesSet.size();
		
		if(oldStatesSet.equals(newStatesSet)) {
			// Since the state haven't changed, the probability of finding the current state 
			// is exactly the same as before: 1/sizeof(possible states from web-flow vectors)
			result.newProfilePercentage += 1.0/oldStatesSet.size();
		} else if(!newStatesSet.contains(currentState)) {
			// The current state does no longer have the same web-flow vector sequence
			// in the new profile => 0% chance of finding it.
			result.newProfilePercentage += 0;
		} else if(oldStatesSet.size() <  newStatesSet.size()) {
			// The resulting state set has increased -> meaning that by using
			// the old result set we would give a higher probability of finding this
			// state, while giving 0 probability to find the new states
			// E.g. Old state	New State	
			// 		'aa'		'aa'		result => 0.33
			//		'ab'		'ab'		result => 0.33
			//					'ac'
			// Total result of old state = 1. Total result of new state = 0.66,
			// meaning that the new state had degraded with 33%. I.e. using the old
			// profile will lead to 33% false inferences -> saying that we will never get
			// 'ac', while in reality, we will in 30% of the cases.
			result.newProfilePercentage += 1.0/newStatesSet.size();
		} else if(oldStatesSet.size() >  newStatesSet.size()) {
			// The resulting state set has decreased -> meaning that by using 
			// the old result set we would give a lower probability if finding this 
			// state than the actual
			// E.g. Old state	New State	
			// 		'aa'		'aa'		result => 0.33
			//		'ab'		'ef'		result => 0		(will be caught in !newStatesSet.contains(currentState))
			//		'ac'					result => 0		(will be caught in !newStatesSet.contains(currentState))
			// Total result of old state = 1. Total result of new state = 0,
			// meaning that the new state had degraded with 33%. I.e. using the old
			// profile will lead to 33% false positives - saying that we get 'ac' in
			// 33% of the cases, while in reality we cannot get 'ac'
			result.newProfilePercentage += 1.0/oldStatesSet.size();
			
		} else {
			// The resulting state set has changed but not decreased in size -> meaning that by using 
			// the old result set we would get 'false' positives, i.e. say that we have found something,
			// while in reality, it's something completely different
			// E.g. Old state	New State	
			// 		'aa'		'aa'		result => 0.33
			//		'ab'		'eb'		result => 0 	(will be caught in !newStatesSet.contains(currentState))
			//		'ac'		'ep'		result => 0		(will be caught in !newStatesSet.contains(currentState))
			result.newProfilePercentage += 1.0/oldStatesSet.size();
		}
		
		
		for(WebState child : currentState.getChildren()) {
			if(child != null)
				degradationHelper(child, rootOfOldProfile, rootOfNewProfile, result);
		}
	}
	
}
