package com.alexsebbe.interceptor;

import java.io.IOException;

import com.alexsebbe.interceptor.WebFlow.Direction;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class StatisticsRunner {

	public static void main(String[] args) {
		WebState rootState = null;
		try {
			rootState = JSONSerializer.deSerializeWebStates("mappings/amazon_uk_6_characters_60_threads.json");
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(rootState == null) {
			return;
		}
		
		System.out.println("Starting calculations...");
		int numValidInputs = StatisticsUtil.getAmountOfValidInput(rootState);
		long numTotalPossible = StatisticsUtil.getTotalAmountOfPossibleCombinations("abcdefghijklmnopqrstuvwxyz",6);
		int numOfDistInputs = StatisticsUtil.getAmountOfDistinguishableInput(rootState);
		double rate = StatisticsUtil.getAveragePredictionRate(rootState);
		
		System.out.println("Density of first layer: " + StatisticsUtil.getDensity(rootState.getChildren(), Direction.INGOING));
		System.out.println("Amount of valid input: " + numValidInputs + " out of " + numTotalPossible + " => " + ((numValidInputs/(double)numTotalPossible)*100) + "%");
		System.out.println("Amount of distinguishable input: " + numOfDistInputs + " out of " + numTotalPossible + " => " + (numOfDistInputs/(double)numTotalPossible)*100 + "%");
		System.out.println("Average prediction rate for valid inputs: " + (rate*100) + "%");
	}

}
