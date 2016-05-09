package com.alexsebbe.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;

public class ChunkedSnifferRunner implements Runnable, Sniffer.OnDataPackageReceivedListener {
	private PcapNetworkInterface nif;
	private List<String> ipAddressFiters;
	private int sourcePortFilter;
	private List<Integer> packetSizes = new ArrayList<Integer>();
	private final Object lock = new Object();
	
	private Boolean hasReceivedEmptyHTTPPacket = true;
	private final static int EMPTY_HTTP_PACKET_SIZE = 37;
	
	private Sniffer sniffer;
	
	public ChunkedSnifferRunner(PcapNetworkInterface nif, List<String> filterBySourceIPs, int sourcePortFilter) {
		this.nif = nif;
		this.ipAddressFiters = filterBySourceIPs;
		this.sourcePortFilter = sourcePortFilter;
	}
	
	public void run() {
		sniffer = new Sniffer();
		sniffer.addListener(this);
		try {
			sniffer.run(nif, ipAddressFiters, sourcePortFilter);
		} catch (PcapNativeException e) {
			e.printStackTrace();
		} catch (NotOpenException e) {
			e.printStackTrace();
		}
	}

	public void onDataPackageReceived(int length, String source,
			String destination, long timeStamp) {
		if(!hasReceivedEmptyHTTPPacket && length != EMPTY_HTTP_PACKET_SIZE) {
			//System.out.println("Received data. Length: " + length + ". From: " + source);
			packetSizes.add(length);
		} else if(!hasReceivedEmptyHTTPPacket && length == EMPTY_HTTP_PACKET_SIZE) {
			synchronized(lock){
				hasReceivedEmptyHTTPPacket = true;
				lock.notify();
			}
		}
		
	}
	
	public List<Integer> getCurrentPacketsSizes() {
		return new ArrayList<Integer>(packetSizes);
	}
	
	public boolean waitForPacketsToBeReceived(long timeoutMilliseconds) {
		synchronized (lock) {
			while (!hasReceivedEmptyHTTPPacket) {
				try {
					lock.wait(timeoutMilliseconds);
					if(!hasReceivedEmptyHTTPPacket) {
						return false;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return true;
		}
	}
	
	public void startNextIteration() {
		synchronized (lock) {
			packetSizes.clear();
			hasReceivedEmptyHTTPPacket = false;
		}
	}
	
	public void destroy() {
		sniffer.destroy();
	}
}
