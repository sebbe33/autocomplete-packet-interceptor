package com.alexsebbe.interceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;

import com.alexsebbe.interceptor.OnTheFlyInterceptor.ResultReceivedListener;

public class OnTheFlyInterceptorRunner {
	private static final String ALTERNATIVES_STRING = "abcdefghijklmnopqrstuvwxyz";
	public static void main(String[] args) {
		System.out.println("Welcome to the autcomplete on-the-fly interceptor");
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
		
		System.out.println("Currently I'm initializing, this might take a while...");
		
		// Start sniffer thread
    	ChunkedSnifferRunner snifferRunner = new ChunkedSnifferRunner(nif, AmazonUKProperties.INSTANCE.getIPAddresses(), 443);
		Thread snifferThread = new Thread(snifferRunner);
		snifferThread.start();
		
		int threadCount = 10;
		try {
			WebFlowVectorFetcherExecutor executor = new WebFlowVectorFetcherExecutor(threadCount, WebRequestEmulatorFactoryImpl.INSTANCE, AmazonUKProperties.INSTANCE, snifferRunner);
			OnTheFlyInterceptor interceptor = new OnTheFlyInterceptor(executor, snifferRunner,ALTERNATIVES_STRING, AmazonUKProperties.INSTANCE, listener);
			
			System.out.println("Press q+enter to quit. Press r+enter to reset and intercept new requests");
			System.out.println("I'm ready to intercept new requests");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input;
				
			while((input=br.readLine())!=null){
				System.out.println(input);
				if(input.equals("r")) {
					interceptor.reset();
					System.out.println("I'm reset - ready to intercept new requests");
				} else if(input.equals("q")) {
					System.out.println("Shutting down...");
					executor.destroy();
					snifferRunner.destroy();
					System.exit(0);
				}
			}
			
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
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
