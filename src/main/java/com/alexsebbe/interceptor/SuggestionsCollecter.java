package com.alexsebbe.interceptor;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;


public class SuggestionsCollecter 
{
	private final static int HTTPS_PORT = 443, PACKET_RECIEVED_TIMEOUT = 10000;
	private WebRequestEmulator emulator = null;
	
	public static void main( String[] args )
    {
		SuggestionsCollecter app = new SuggestionsCollecter();
		app.gatherSuggestions("abcdefghijklmnopqrstuvwxyz", AmazonUKProperties.INSTANCE, 3);
    }
    
    public void gatherSuggestions(String alternativesString, WebSiteProperties webSiteProperties, int depth) {
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
		
		long time = System.currentTimeMillis();
		System.out.println("Beginning collecting suggestions... This might take a long time");
		
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
 
    	
		// Start sniffer thread
    	ChunkedSnifferRunner snifferRunner = new ChunkedSnifferRunner(nif, webSiteProperties.getIPAddresses(), HTTPS_PORT);
		Thread snifferThread = new Thread(snifferRunner);
		snifferThread.start();

		CharacterEntry rootEntry = new CharacterEntry(null, null);
		
		// Begin iterating over combinations
        try {

        	gatherChildren(rootEntry, depth, alternativesString, webSiteProperties, snifferRunner, 0);
        	
        	emulator.destroy();
        	
        	JSONSerializer.serializeCharacterEntry(rootEntry, "test.json");
        	
        	System.out.println("Finished collection suggestions. "
        			+ "Time elapsed" + (System.currentTimeMillis() - time)/1000.0 + " seconds "
					+ "("+(System.currentTimeMillis() - time)/(1000.0*60)+" min)");
        	
        	
        	snifferRunner.destroy();
        	
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void gatherChildren(CharacterEntry parentEntry, int maxDepth, String alternativesString, 
    		WebSiteProperties webSiteProperties, ChunkedSnifferRunner snifferRunner, int currentDepth) 
    				throws IOException {
    	
    	for(char c : alternativesString.toCharArray()) {
    		String searchString = parentEntry.getCharaceterSequenceUpToAndIncludingThis() + c;

    		snifferRunner.startNextIteration();
    		
    		String response = emulator.doSearch(searchString, webSiteProperties);
        	while(!snifferRunner.waitForPacketsToBeReceived(PACKET_RECIEVED_TIMEOUT)) {
        		System.out.println("Timed out while waiting for packet to be captured. Trying again! "
        				+ "Current search string '"+searchString+"'");
        		response = emulator.doSearch(searchString, webSiteProperties);
        	}
        	
        	if(snifferRunner.getCurrentPacketsSizes().size() == 1 
        			&& webSiteProperties.isNoResultResponse(response, searchString)) {
        		// Got an empty result -> no need to continue traversing
        		continue;
        	} else {
				// Add new result as a children to the parent
        		CharacterEntry currentEntry = new CharacterEntry(snifferRunner.getCurrentPacketsSizes(), c, parentEntry);
        		parentEntry.getChildren().add(currentEntry);
        		
        		// If we haven't reached the max depth, call recursively with the current entry as parent
        		if(currentDepth + 1 < maxDepth) {
        			gatherChildren(currentEntry, maxDepth, alternativesString, webSiteProperties, snifferRunner, currentDepth + 1);
        		}
			}
        	
    	}
    }
}
