package com.alexsebbe.interceptor;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;

public class StateCollectorRunner {
	private static final int NO_ACTIVITY_TIMEOUT = 30*1000;
	private static final int ACTIVITY_POLL_INTERVAL = 3*1000;
	public static void main(String[] args) {
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
		
		System.out.println("Initializing setup....");
		
		try {
			int threadCount = 60;
			int depth = 4;
			WebFlowVectorFetcherExecutor executor = new WebFlowVectorFetcherExecutor(threadCount, WebRequestEmulatorFactoryImpl.INSTANCE, AmazonUKProperties.INSTANCE, snifferRunner);
			StateCollector stateCollector = new StateCollector("abcdefghijklmnopqrstuvwxyz", depth, AmazonUKProperties.INSTANCE, threadCount, snifferRunner, executor);
			System.out.println("Beginning to collect suggestions... This might take a long time");
			long time = System.currentTimeMillis();
			WebState rootState = stateCollector.collect();
			
			long lastTimestamp;
			do {
				try {
					Thread.sleep(ACTIVITY_POLL_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastTimestamp = executor.getLastActivity();
			} while (lastTimestamp + NO_ACTIVITY_TIMEOUT > System.currentTimeMillis());
			
			System.out.println("No activity within "+(NO_ACTIVITY_TIMEOUT/1000)+" sec. Finishing...");
			System.out.println("Finished collection suggestions. "
        			+ "Time elapsed" + (lastTimestamp - time)/1000.0 + " seconds "
					+ "("+(lastTimestamp - time)/(1000.0*60)+" min)");
			JSONSerializer.serializeWebStates(rootState, "testing.json");
			executor.destroy();
			snifferRunner.destroy();
			
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
