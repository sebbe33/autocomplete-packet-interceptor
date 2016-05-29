package com.alexsebbe.interceptor;

import java.io.IOException;
import java.util.List;

import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;

import com.alexsebbe.ChunkedSnifferRunner;
import com.alexsebbe.JSONSerializer;
import com.alexsebbe.WebState;
import com.alexsebbe.interceptor.PrecomputedProfileInterceptor.ResultReceivedListener;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class PrecomputedProfileInterceptorRunner {

	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Usage: PrecomputedProfileInterceptorRunner filePathToProfile");
		}
		
		System.out.println("Welcome to the autcomplete precomputed-profile interceptor");
		String profilePath = args[0];
		
		WebState profile = null;
		try {
			profile = JSONSerializer.deSerializeWebStates(profilePath);
		} catch (JsonParseException e1) {
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if(profile == null) {
			System.out.println("Failed to load profile. Please make sure you specified a valid profile");
			System.exit(-1);
		}
		
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
		
		// Start sniffer thread
    	ChunkedSnifferRunner snifferRunner = new ChunkedSnifferRunner(nif, AmazonUKProperties.INSTANCE.getIPAddresses(), 443);
		Thread snifferThread = new Thread(snifferRunner);
		snifferThread.start();
		
		PrecomputedProfileInterceptor interceptor = new PrecomputedProfileInterceptor(snifferRunner, profile, listener);
	}
	
	private static ResultReceivedListener listener = new ResultReceivedListener() {  
		public void onResultRecieved(int level, List<WebState> possibleStates) {
			System.out.println("I think you typed " + level + " character(s) and I guess that you wrote one of the following queries:");
			for(WebState state : possibleStates) {
				System.out.println(state.getName());
			}
		}
	};

}
