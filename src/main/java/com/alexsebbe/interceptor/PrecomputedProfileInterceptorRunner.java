package com.alexsebbe.interceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.pcap4j.core.NotOpenException;
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
		System.out.println("Press q+enter to quit. Press r+enter to reset and intercept new requests");
		System.out.println("I'm reset - ready to intercept new requests");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input;
		try {
			while((input=br.readLine())!=null){
				System.out.println(input);
				if(input.equals("r")) {
					interceptor.reset();
					System.out.println("I'm reset - ready to intercept new requests");
				} else if(input.equals("q")) {
					System.out.println("Shutting down...");
					snifferRunner.destroy();
					System.exit(0);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotOpenException e) {
			e.printStackTrace();
		}
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
