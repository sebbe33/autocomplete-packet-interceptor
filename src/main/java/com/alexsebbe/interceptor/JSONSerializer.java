package com.alexsebbe.interceptor;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONSerializer {

	private final static ObjectMapper mapper = new ObjectMapper();
	
	public static void serializeCharacterEntry(CharacterEntry ce, String fileName) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(new File(fileName), ce);
		System.out.println("Done serializing");
	}
	
	public static CharacterEntry deSerializeCharacterEntry(String fileName) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(new File(fileName), CharacterEntry.class);
	}
}
