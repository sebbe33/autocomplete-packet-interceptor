package com.alexsebbe;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONSerializer {

	private final static ObjectMapper mapper = new ObjectMapper();
	
	public static void serializeWebStates(WebState rootState, String fileName) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		//mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.writeValue(new File(fileName), rootState);
		
		System.out.println("Done serializing");
	}
	
	public static WebState deSerializeWebStates(String fileName) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(new File(fileName), WebState.class);
	}
}
