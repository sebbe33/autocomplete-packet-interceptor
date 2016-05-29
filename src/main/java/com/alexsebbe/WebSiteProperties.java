package com.alexsebbe;

import java.util.List;

public interface WebSiteProperties {
	String getAutocompleteRequestURL(String searchString);
	Range<Integer> getNoResultRequestPacketLengthRange(String searchString);
	int getNoResultRequestPacketLength(String searchString);
	boolean isNoResultResponse(String response, String searchString);
	List<String> getIPAddresses();
	
	static class Range<E extends Comparable<E>> {
		private E lowerBound, upperBound;
		public Range(E lowerBound, E upperBound) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}
		
		public E getLowerBound() {
			return lowerBound;
		}
		
		public E getUpperBound() {
			return upperBound;
		}
		
		public boolean isWithinRange(E object) {
			return object.compareTo(lowerBound) >= 0 && object.compareTo(upperBound) <= 0;
		}
	}
}
