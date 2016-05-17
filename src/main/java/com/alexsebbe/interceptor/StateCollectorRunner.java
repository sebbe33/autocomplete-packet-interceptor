package com.alexsebbe.interceptor;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;

public class StateCollectorRunner {
	private static final int NO_ACTIVITY_TIMEOUT = 10*1000;
	private static final int ACTIVITY_POLL_INTERVAL = 8*1000;
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
		
		long time = System.currentTimeMillis();
		System.out.println("Beginning collecting suggestions... This might take a long time");
		
		try {
			int threadCount = 40;
			int depth = 4;
			WebFlowVectorFetcherExecutor executor = new WebFlowVectorFetcherExecutor(threadCount, WebRequestEmulatorFactoryImpl.INSTANCE, AmazonUKProperties.INSTANCE, snifferRunner);
			StateCollector stateCollector = new StateCollector("abcdefghijklmnopqrstuvwxyz", depth, AmazonUKProperties.INSTANCE, threadCount, snifferRunner, executor);
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
			
			System.out.println("No activity within 180 sec. Finishing...");
			System.out.println("Finished collection suggestions. "
        			+ "Time elapsed" + (System.currentTimeMillis() - time)/1000.0 + " seconds "
					+ "("+(System.currentTimeMillis() - time)/(1000.0*60)+" min)");
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
