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

}
