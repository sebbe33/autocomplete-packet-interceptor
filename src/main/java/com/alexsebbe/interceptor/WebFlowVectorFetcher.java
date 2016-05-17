package com.alexsebbe.interceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alexsebbe.interceptor.WebFlow.Direction;

public class WebFlowVectorFetcher extends Thread {
	private final static int PACKET_RECIEVED_TIMEOUT = 10000;
	private WebRequestEmulator emulator;
	private int timesRun = 0;
	private int destinationPort;
	private String searchString;
	private WebSiteProperties webSiteProperties;
	private WebFlowVectorReceivedListener listener;
	private ChunkedSnifferRunner chukedSnifferRunner;
	private WebFlowVectorFetcherExecutor executor;
	
	private Object restartLock = new Object();
	private boolean isAlive = true;
	
	public WebFlowVectorFetcher(WebFlowVectorFetcherExecutor executor, WebRequestEmulator emulator, ChunkedSnifferRunner chukedSnifferRunner) {
		super();
		this.emulator = emulator;
		this.chukedSnifferRunner = chukedSnifferRunner;
		this.destinationPort = emulator.getDestinationPort();
		this.executor = executor;
	}
	
	public void initialize(String searchString, WebSiteProperties webSiteProperties, WebFlowVectorReceivedListener listener) {
		this.searchString = searchString;
		this.webSiteProperties = webSiteProperties;
		this.listener = listener;
	}
	
	public void restart() {
		synchronized (restartLock) {
			restartLock.notify();
		}
	}
	
	@Override
	public void run() {
		while(isAlive) {
			timesRun++;
			String httpResponse = "";
			List<WebFlow> webFlowVector = new ArrayList<WebFlow>();
			try {
				chukedSnifferRunner.startNextIteration(destinationPort);
				httpResponse = emulator.doSearch(searchString, webSiteProperties);
	        	while(!chukedSnifferRunner.waitForPacketsToBeReceived(destinationPort, PACKET_RECIEVED_TIMEOUT)) {
	        		System.out.println("Timed out while waiting for packet to be captured. Trying again! "
	        				+ "Current search string '"+searchString+"'");
	        		httpResponse = emulator.doSearch(searchString, webSiteProperties);
	        	}
	        	if(timesRun % 500 == 0) {
	        		System.out.println(httpResponse);
	        	}
	        	
	        	List<Integer> packetLengths = chukedSnifferRunner.getCurrentPacketsSizes(destinationPort);
	        	for(Integer i : packetLengths) {
	        		webFlowVector.add(new WebFlow(Direction.INGOING, i));
	        	}
	        	//System.out.println("Port: "+ destinationPort + " - " + webFlowVector + " " + response);
	        	
			} catch (IOException e) {
				listener.onFailure(this);
			}
			
			
			if(webSiteProperties.isNoResultResponse(httpResponse, searchString)) {
				listener.onEmptyResultResponse(this, searchString);
			} else {
				listener.onNonEmptyResultResponse(this, webFlowVector, searchString);
			}
			
			synchronized (restartLock) {
				executor.signalDone(this);
				try {
					restartLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void destoy() throws IOException {
		isAlive = false;
		emulator.destroy();
		restart();
	}
}
