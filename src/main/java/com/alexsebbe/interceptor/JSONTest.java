package com.alexsebbe.interceptor;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class JSONTest {

	public static void main(String[] args) {
		try {
			WebState rootState = JSONSerializer.deSerializeWebStates("testing.json");
			WebStateUtils.printWebstateAndDescendants(rootState);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
