package com.alexsebbe.statistics;

import java.io.IOException;

import com.alexsebbe.JSONSerializer;
import com.alexsebbe.WebState;
import com.alexsebbe.WebFlow.Direction;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class StatisticsRunner {

	public static void main(String[] args) {
		System.out.println("Loading file...");
		WebState rootState = null;
		try {
			rootState = JSONSerializer.deSerializeWebStates("mappings/5/5_2704_0007.json");
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
		int numValidInputs = Statistics.getAmountOfValidInput(rootState);
		long numTotalPossible = Statistics.getTotalAmountOfPossibleCombinations("abcdefghijklmnopqrstuvwxyz",4);
		int numOfDistInputs = Statistics.getAmountOfDistinguishableInput(rootState);
		double rate = Statistics.getAveragePredictionRate(rootState);
		
		System.out.println("Density of first layer: " + Statistics.getDensity(rootState.getChildren(), Direction.INGOING));
		System.out.println("Amount of valid input: " + numValidInputs + " out of " + numTotalPossible + " => " + ((numValidInputs/(double)numTotalPossible)*100) + "%");
		System.out.println("Amount of distinguishable input: " + numOfDistInputs + " out of " + numTotalPossible + " => " + (numOfDistInputs/(double)numTotalPossible)*100 + "%");
		System.out.println("Average prediction rate for valid inputs: " + (rate*100) + "%");
	}

}
