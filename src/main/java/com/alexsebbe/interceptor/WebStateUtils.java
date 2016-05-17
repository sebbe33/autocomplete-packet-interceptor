package com.alexsebbe.interceptor;

public class WebStateUtils {
	public static void printWebstateAndDescendants(WebState webstate) {
		System.out.println(webstate.getName());
		if(webstate.getChildren() == null) {
			return;
		}
		
		for(WebState child : webstate.getChildren()) {
			printWebstateAndDescendants(child);
		}
	}
}
