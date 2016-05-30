package com.alexsebbe.statistics;

import java.io.IOException;

import com.alexsebbe.JSONSerializer;
import com.alexsebbe.WebState;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class StatisticsDegradationRunner {

	/**
	 * Determines the rate of degradation for profiles. Usage StatisticsDegradationRunner file1 file2 file3 ...
	 * @param args
	 */
	public static void main(String[] args) {
		String[] files = args;
		if(files.length == 0) {
			files = new String[] {
					"mappings/4/4_0523_1126.json", 
					"mappings/4/4_0524_1730.json", 
					"mappings/4/4_0524_1830.json",
					"mappings/4/4_0524_2330.json",
					"mappings/4/4_2505_0135.json",
					"mappings/4/4_0525_1040.json",
					"mappings/4/4_2504_1716.json",
					"mappings/4/4_2704_1025.json"};
		}
		
		if(files.length < 2) {
			System.out.println("You must enter atleast two files to compare degradation");
		}
		
		WebState rootOfOldProfile = null;
		WebState rootOf1 = new WebState("", null,null,null);
		try {
			rootOfOldProfile = JSONSerializer.deSerializeWebStates(files[0]);
			for(WebState state : rootOfOldProfile.getChildren()) {
				rootOf1.addChild(new WebState(state.getName(), rootOf1, state.getInput(), state.getWebFlowVector()));
			}
			for(int i = 1; i < files.length; i++) {
				WebState tempRoot = new WebState("", null,null,null);
				
				WebState profile = JSONSerializer.deSerializeWebStates(files[i]);
				for(WebState state : profile.getChildren()) {
					tempRoot.addChild(new WebState(state.getName(), tempRoot, state.getInput(), state.getWebFlowVector()));
				}
				System.out.println("Degradation of profile "+files[i] + ": " + 
					Statistics.getDegradationOfProfile(rootOfOldProfile, profile) + " (first layer " + Statistics.getDegradationOfProfile(rootOf1, tempRoot) + ")");
				
				
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
