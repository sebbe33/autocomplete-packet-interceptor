package com.alexsebbe.notused;

import java.io.IOException;
import java.util.List;

import com.alexsebbe.interceptor.JSONSerializer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class Statistics {
	
	public static void main(String[] args) {
		CharacterEntry rootEntry = null;
		try {
			rootEntry = JSONSerializer.deSerializeCharacterEntry("mappings/amazon_uk_3_chars.json");
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(rootEntry == null) {
			return;
		}

		int numValidInputs = getAmountOfValidInput(rootEntry);
		long numTotalPossible = getTotalAmountOfCombinations("abcdefghijklmnopqrstuvwxyz",3);
		int numOfDistInputs = getTotalAmountOfDistingishableInputs(rootEntry);
		
		System.out.println("Amount of valid input: " + numValidInputs + " out of " + numTotalPossible + " = " + numValidInputs/(double)numTotalPossible);
		System.out.println("Amoun of distinguishable input: " + numOfDistInputs + " out of " + numTotalPossible + " = " + numOfDistInputs/(double)numTotalPossible);
	}
	
	public static int getAmountOfValidInput(CharacterEntry rootEntry) {
		return getAmountValidHelper(rootEntry) - 1;
	}
	
	private static int getAmountValidHelper(CharacterEntry currentEntry) {
		if(currentEntry.getChildren() == null || currentEntry.getChildren().isEmpty()) {
			return 1; // Reached a non-result entry
		}
		int result = 1;
		
		for(CharacterEntry child : currentEntry.getChildren()) {
			result += getAmountValidHelper(child);
		}
		
		return result;
	}
	
	public static int getTotalAmountOfDistingishableInputs(CharacterEntry rootEntry) {
		return getTotalAmountOfDistingishableInputsHelper(rootEntry, rootEntry);
	}
	
	private static int getTotalAmountOfDistingishableInputsHelper(CharacterEntry currentEntry, CharacterEntry rootEntry) {
		int result = 0;
		
		if(currentEntry.getChildren() != null) {
			for(CharacterEntry child : currentEntry.getChildren()) {
				result += getTotalAmountOfDistingishableInputsHelper(child, rootEntry);
			}
		}
		
		if(currentEntry != rootEntry) {
			result += getAmountOfPossibleInputsFromLengths(currentEntry.getLengthsUpToAndIncludingNow(), rootEntry) == 1? 1 : 0;
		}
		
		return result;
	}
	
	public static int getAmountOfPossibleInputsFromLengths(List<List<Integer>> lengths, CharacterEntry rootEntry) {
		int result = 0;
		for(CharacterEntry child : rootEntry.getChildren()) {
			result += getAmountOfPossibleInputsFromLengthsHelper(child, lengths, 0);
		}
		
		return result;
	}
	
	public static int getAmountOfPossibleInputsFromLengthsHelper(CharacterEntry currentEntry, List<List<Integer>> lengths, int currentDepth) {		
		List<Integer> lengthsToMatch = lengths.get(currentDepth);
		
		if(!lengthsToMatch.equals(currentEntry.getLengths())) {
			return 0; // No need to continue to check children since current didn't match
		}
		
		if(currentDepth == lengths.size() - 1) { 
			// We have cleared the depth and come this far => we have a match
			return 1;
		}
		
		int result = 0;
		for(CharacterEntry child : currentEntry.getChildren()) {
			result += getAmountOfPossibleInputsFromLengthsHelper(child, lengths, currentDepth+1);
		}
		
		return result;
	}
	

	public static long getTotalAmountOfCombinations(String alternativesString, int maxDepth) {
		long result = 0;
		for(int i = 1; i <= maxDepth; i++) {
			result += Math.pow(alternativesString.length(), i);
		}
		
		return result;
	}
}
