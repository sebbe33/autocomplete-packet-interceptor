package com.alexsebbe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebFlow {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public enum Direction {
		INGOING, OUTGOING
	}
	
	private Direction direction;
	private int length;
	
	public WebFlow() {}
	
	public WebFlow(Direction direction, int length) {
		this.direction = direction;
		this.length = length;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public int getLength() {
		return length;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) {
			return false;
		}
		
		if(this.getClass() != other.getClass()) {
			return false;
		}
		
		WebFlow otherWebFlow = (WebFlow) other;
		
		return direction == otherWebFlow.direction && length == otherWebFlow.length;
	}
	
	@Override
	public String toString() {
		return (direction == Direction.INGOING? "<-" : "->") + length;
	}
}
