package com.alexsebbe;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.alexsebbe.Sniffer.OnDataPackageReceivedListener;

public class WebFlowVectorFetcherExecutor {
	private LinkedBlockingQueue<WebFlowVectorFetcher> fetcherQueue;
	private LinkedBlockingQueue<FetcherTask> taskQueue = new LinkedBlockingQueue<WebFlowVectorFetcherExecutor.FetcherTask>();
	private Set<WebFlowVectorFetcher> allFetchers;
	private boolean isActive = true;
	
	private Object portListenerLock = new Object();
	private long lastActivity = 0;
	private int currentPort = 0;
	private Set<Integer> portsUsed = new HashSet<Integer>();
	
	private OnDataPackageReceivedListener portListener = new OnDataPackageReceivedListener() {

		public void onDataPackageReceived(int length, String IPSource,
				String IPDestination, int sourcePort, int destinationPort,
				long timeStamp) {
			currentPort = destinationPort;
			synchronized (portListenerLock) {
				portListenerLock.notify();
			}
		}
		
	};
	
	public WebFlowVectorFetcherExecutor(int size, WebRequestEmulatorFactory factory, WebSiteProperties properties, ChunkedSnifferRunner snifferRunner) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		allFetchers = new HashSet<WebFlowVectorFetcher>(size);
		fetcherQueue = new LinkedBlockingQueue<WebFlowVectorFetcher>(size);
		snifferRunner.getSniffer().addListener(portListener);
		
		// Find the ports for the web emulators
		for(int i = 0; i < size; i++) {
			WebRequestEmulator emulator = factory.createWebRequestEmulator();
			
			while(currentPort == 0) {
				System.out.println("Trying to establish port: ");
				try {
					emulator.doSearch("", properties);
					synchronized (portListenerLock) {
						portListenerLock.wait(5000);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			System.out.println("Current port: " + currentPort);
			emulator.setDestinationPort(currentPort);
			portsUsed.add(currentPort);
			currentPort = 0;
			WebFlowVectorFetcher fetcher = new WebFlowVectorFetcher(this, emulator, snifferRunner);
			fetcherQueue.add(fetcher);
			allFetchers.add(fetcher);
		}
		
		snifferRunner.getSniffer().removeListener(portListener);
		Thread executor = new Thread(new Executor());
		executor.start();
	}
	
	public synchronized void stackFetchingTask(String searchString, WebSiteProperties properties, WebFlowVectorReceivedListener listener) {
		taskQueue.add(new FetcherTask(searchString, properties, listener));
	}
	
	public void signalDone(WebFlowVectorFetcher fetcher) {
		fetcherQueue.add(fetcher);
	}
	
	/**
	 * Destroy and release all resources occupied by this executor.
	 * @throws IOException
	 */
	public void destroy() throws IOException {
		isActive = false;
		for(WebFlowVectorFetcher fetcher : allFetchers) {
			fetcher.destoy();
		}
	}
	
	/**
	 * Returns the timestamp of the last activity of the executor
	 * @return UNIX timestamp
	 */
	public long getLastActivity() {
		return lastActivity;
	}
	
	/**
	 * Returns all port numbers (TCP) used by the executor
	 * @return
	 */
	public Set<Integer> getPortsUsed() {
		return portsUsed;
	}
	
	private static class FetcherTask {
		private String searchString;
		private WebSiteProperties properties;
		private WebFlowVectorReceivedListener listener;
		
		public FetcherTask(String searchString,  WebSiteProperties properties, WebFlowVectorReceivedListener listener) {
			this.searchString = searchString;
			this.properties = properties;
			this.listener = listener;
		}
	}
	
	private class Executor implements Runnable {

		public void run() {
			while(isActive) {
				FetcherTask task = null;
				try {
					task = taskQueue.poll(600, TimeUnit.SECONDS);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				if(task != null) {
					WebFlowVectorFetcher fetcher = null;
					while(fetcher == null) {
						try {
							fetcher = fetcherQueue.poll(60, TimeUnit.SECONDS);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}					
					
					fetcher.initialize(task.searchString, task.properties, task.listener);
					if(fetcher.isAlive()) {
						fetcher.restart();
					} else {
						fetcher.start();
					}
					lastActivity = System.currentTimeMillis();
				}
			}
		}
	}
}
