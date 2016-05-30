package com.alexsebbe.statistics;

import java.io.IOException;

import com.alexsebbe.JSONSerializer;
import com.alexsebbe.WebState;
import com.alexsebbe.WebFlow.Direction;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class StatisticsRunner {

	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Usage: StatisticsRunner file_path_to_profile depth_of_profile");
			System.exit(-1);
		}
		int depth = Integer.parseInt(args[1]);
		
		System.out.println("Loading file...");
		WebState rootState = null;
		
		try {
			rootState = JSONSerializer.deSerializeWebStates(args[0]);
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
		long numTotalPossible = Statistics.getTotalAmountOfPossibleCombinations("abcdefghijklmnopqrstuvwxyz",depth);
		int numOfDistInputs = Statistics.getAmountOfDistinguishableInput(rootState);
		double rate = Statistics.getAveragePredictionRate(rootState);
		
		System.out.println("Density of first layer: " + Statistics.getDensity(rootState.getChildren(), Direction.INGOING));
		System.out.println("Amount of valid input: " + numValidInputs + " out of " + numTotalPossible + " => " + ((numValidInputs/(double)numTotalPossible)*100) + "%");
		System.out.println("Amount of distinguishable input: " + numOfDistInputs + " out of " + numTotalPossible + " => " + (numOfDistInputs/(double)numTotalPossible)*100 + "%");
		System.out.println("Average prediction rate for valid inputs: " + (rate*100) + "%");
	}

}
