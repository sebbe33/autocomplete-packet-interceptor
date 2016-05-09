package com.alexsebbe.interceptor;

import java.util.Arrays;
import java.util.List;

public enum AmazonUKProperties implements WebSiteProperties {
	INSTANCE;
	
	private static final int NO_RESULT_PACKET_SIZE_MARGIN = 15;
	
	private static final List<String> knownIPAddresses = Arrays.asList(
			"178.236.6.47", 
			"54.239.33.84",
			"176.32.111.71");
	
	public String getAutocompleteRequestURL(String searchString) {
		return "https://completion.amazon.co.uk/search/complete?"
	    		+ "method=completion&mkt=3&client=amazon-search-ui&x=String&search-alias=aps"
	    		+ "&q="+searchString
	    		+ "&qs=&cf=1&noCacheIE=1462647402092&fb=1&sc=1&";
	}

	public Range<Integer> getNoResultRequestPacketLengthRange(String searchString) {
		int length = ("completion = [\""+searchString+"\",[],[],[]];String();").length();
		return new Range<Integer>(length - NO_RESULT_PACKET_SIZE_MARGIN, length + NO_RESULT_PACKET_SIZE_MARGIN);
	}

	public int getNoResultRequestPacketLength(String searchString) {
		return ("completion = [\""+searchString+"\",[\""+searchString+"\"],[{}],[]];String();").length();
	}

	public List<String> getIPAddresses() {
		return knownIPAddresses;
	}

	public boolean isNoResultResponse(String response, String searchString) {
		return ("completion = [\""+searchString+"\",[],[],[]];String();").equals(response.trim());
	}

}
