package com.alexsebbe.interceptor;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class WebState {
	private String name;
	private String input;
	private WebState parent;
	private List<WebFlow> webFlowVector;
	private List<WebState> children = new ArrayList<WebState>();
	
	public WebState() {}
	
	public WebState(String name, WebState parent, String input, List<WebFlow> webFlowVector) {
		this.name = name;
		this.parent = parent;
		this.input = input;
		this.webFlowVector = webFlowVector;
	}
	
	/**
	 * Returns the name of the state
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the web-flow vector leading up to this state
	 * @return web-flow vector
	 */
	public List<WebFlow> getWebFlowVector() {
		return webFlowVector;
	}
	

	/**
	 * Returns the input leading up to this state
	 * @return input
	 */
	public String getInput() {
		return input;
	}

	/**
	 * Returns the parent of this state
	 * @return parent
	 */
	public WebState getParent() {
		return parent;
	}

	/**
	 * Returns the children of this state
	 * @return children
	 */
	public List<WebState> getChildren() {
		return children;
	}
	
	/**
	 * Adds a child to the web state. Thread safe
	 * @param child
	 */
	public synchronized void addChild(WebState child) {
		children.add(child);
	}

	@Override
	public String toString() {
		return "WebState [name: " + getName() + ", input: " + getInput() + ", " + getWebFlowVector() + "]"; 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebState other = (WebState) obj;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (webFlowVector == null) {
			if (other.webFlowVector != null)
				return false;
		} else if (!webFlowVector.equals(other.webFlowVector))
			return false;
		return true;
	}
	
	
	
	/*
	@Override
	public boolean equals(Object other) {
		System.out.println("Entered equals");
		if(other == null || other.getClass() != getClass()) {
			return false;
		}
		
		WebState otherWebState = (WebState) other;
		boolean equality = true;

		
		if(getParent() == null) {
			if(otherWebState.getParent() != null) {
				equality = false;
			}
		} else {
			if(!getParent().equals(otherWebState.getParent())) {
				equality = false;
			}
		}
		
		if(getInput() == null) {
			if(otherWebState.getInput() != null) {
				equality = false;
			}
		} else {
			if(!getInput().equals(otherWebState.getInput())) {
				equality = false;
			}
		}
		
		if(getName() == null) {
			if(otherWebState.getName() != null) {
				equality = false;
			}
		} else {
			if(!getName().equals(otherWebState.getName())) {
				equality = false;
			}
		}
		
		if(getWebFlowVector() == null) {
			if(otherWebState.getWebFlowVector() != null) {
				equality = false;
			}
		} else {
			if(!getWebFlowVector().equals(otherWebState.getWebFlowVector())) {
				equality = false;
			}
		}
		System.out.println("Equality: " + equality);
		return 	equality;/*&& getChildren().equals(otherWebState.getChildren()) ;
	}*/
}
