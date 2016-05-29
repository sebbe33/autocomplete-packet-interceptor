package com.alexsebbe.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alexsebbe.ChunkedSnifferRunner;
import com.alexsebbe.ChunkedSnifferRunner.AutoCompletePacketsReceivedListener;
import com.alexsebbe.WebFlow;
import com.alexsebbe.WebFlow.Direction;
import com.alexsebbe.WebState;
import com.alexsebbe.WebStateUtils;

public class PrecomputedProfileInterceptor implements AutoCompletePacketsReceivedListener {
	private ChunkedSnifferRunner snifferRunner;
	private Map<Integer, List<List<WebFlow>>> portsToIntercept = new HashMap<Integer, List<List<WebFlow>>>();
	private ResultReceivedListener resultReceivedListener;
	private WebState rootStateOfProfile;
	
	public PrecomputedProfileInterceptor(ChunkedSnifferRunner snifferRunner, WebState rootStateOfProfile, 
			ResultReceivedListener resultReceivedListener) {
		this.snifferRunner = snifferRunner;
		this.resultReceivedListener = resultReceivedListener;
		this.rootStateOfProfile = rootStateOfProfile;
		snifferRunner.registerAutoCompletePacketsReceivedListener(this);
	}

	public void onAutoCompletePacketsReceived(int port,
			List<Integer> packetSizes) {
		
		List<List<WebFlow>> portWebFlow = portsToIntercept.get(port);
		if(portWebFlow == null) {
			portsToIntercept.put(port, new ArrayList<List<WebFlow>>());
		} 
		
		// Convert incoming packets to web-flow vector and add them to the sequence of web flow vectors
		portWebFlow.add(convertIncomingPacketSizeToWebFlowVector(packetSizes));

		List<WebState> possibleWebStates = WebStateUtils.getStatesFromWebFlowVectors(portWebFlow, rootStateOfProfile);
		resultReceivedListener.onResultRecieved(portWebFlow.size(), possibleWebStates);
		// TODO : if it is not an empty packet

		
		// Start new iteration on sniffer runner in order to be able to receive more packets
		System.out.println("Intercepted packets on port " + port + ". Web flow vector: " + portWebFlow + ". Calculating possible states...");
		snifferRunner.startNextIteration(port);
	}
	
	public void reset() {
		portsToIntercept.clear();
	}
	
	private List<WebFlow> convertIncomingPacketSizeToWebFlowVector(List<Integer> packetLengths) {
		List<WebFlow> webFlowVector = new ArrayList<WebFlow>();
		// Clear every packet but the last, since every search query we've observed is sent in only one packet
		// The other packets can be images or other metadata sent by amazon's autocomplete server
		webFlowVector.add(new WebFlow(Direction.INGOING, packetLengths.get(packetLengths.size()-1)));
		
		return webFlowVector;
	}
	
    
    public interface ResultReceivedListener {
    	void onResultRecieved(int level, List<WebState> possibleStates);
    }
	
}
