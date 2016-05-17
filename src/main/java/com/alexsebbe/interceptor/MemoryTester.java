package com.alexsebbe.interceptor;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;

public class MemoryTester implements WebFlowVectorReceivedListener {

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
		
		int threadCount = 40;
		int depth = 4;
		try {
			WebFlowVectorFetcherExecutor executor = new WebFlowVectorFetcherExecutor(threadCount, WebRequestEmulatorFactoryImpl.INSTANCE, AmazonUKProperties.INSTANCE, snifferRunner);
		
			for(int i = 0; i < 50*1000; i++) {
			executor.stackFetchingTask(
					""+i, AmazonUKProperties.INSTANCE, new MemoryTester());
			}
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		
	}

	public void onFailure(WebFlowVectorFetcher source) {
		// TODO Auto-generated method stub
		
	}

	public void onEmptyResultResponse(WebFlowVectorFetcher source,
			String searchString) {
		// TODO Auto-generated method stub
		
	}

	public void onNonEmptyResultResponse(WebFlowVectorFetcher source,
			List<WebFlow> webFlowVector, String searchString) {
		// TODO Auto-generated method stub
		
	}

}
