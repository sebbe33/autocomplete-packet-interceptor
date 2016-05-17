package com.alexsebbe.interceptor;


public class StatisticsUtil {

	public static int getAmountOfValidInput(WebState rootState) {
		return countStatesRecursively(rootState) - 1;
	}
	
	private static int countStatesRecursively(WebState currentState) {
		if(currentState.getChildren() == null || currentState.getChildren().isEmpty()) {
			return 1; // Reached a non-result entry
		}
		int result = 1;
		
		for(WebState child : currentState.getChildren()) {
			result += countStatesRecursively(child);
		}
		
		return result;
	}
}
