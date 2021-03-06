package com.alexsebbe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;

public class ChunkedSnifferRunner implements Runnable {
	private final static int EMPTY_HTTP_PACKET_SIZE = 37;

	private PcapNetworkInterface nif;
	private List<String> ipAddressFiters;
	private int sourcePortFilter;
	
	private Map<Integer, PortPacketsCollection> portMappings = new HashMap<Integer, PortPacketsCollection>();
	private List<AutoCompletePacketsReceivedListener> listeners = 
			new ArrayList<ChunkedSnifferRunner.AutoCompletePacketsReceivedListener>();
	private Sniffer sniffer;
	
	public ChunkedSnifferRunner(PcapNetworkInterface nif, List<String> filterBySourceIPs, int sourcePortFilter) {
		this.nif = nif;
		this.ipAddressFiters = filterBySourceIPs;
		this.sourcePortFilter = sourcePortFilter;
	}
	
	public void run() {
		sniffer = new Sniffer();
		sniffer.addListener(dataCapturedListener);
		try {
			sniffer.run(nif, ipAddressFiters, sourcePortFilter);
		} catch (PcapNativeException e) {
			e.printStackTrace();
		} catch (NotOpenException e) {
			e.printStackTrace();
		}
	}

	private Sniffer.OnDataPackageReceivedListener dataCapturedListener = new Sniffer.OnDataPackageReceivedListener() {
		public void onDataPackageReceived(int length, String source,
				String destination, int sourcePort, int destinationPort, long timeStamp) {
			//System.out.println("length: " + length + " port: " + destinationPort);
			PortPacketsCollection portAttribute = portMappings.get(destinationPort);
			if(portAttribute == null) {
				portAttribute = new PortPacketsCollection();
				portMappings.put(destinationPort, portAttribute);
			}

			if(!portAttribute.hasReceivedEmptyHTTPPacket && length != EMPTY_HTTP_PACKET_SIZE) {
				//System.out.println("Received data. Length: " + length + ". From: " + source);
				portAttribute.packetSizes.add(length);
			} else if(!portAttribute.hasReceivedEmptyHTTPPacket && length == EMPTY_HTTP_PACKET_SIZE) {
				synchronized(portAttribute.lock){
					portAttribute.hasReceivedEmptyHTTPPacket = true;
					portAttribute.lock.notify();
				}
				for(AutoCompletePacketsReceivedListener l : listeners) {
					l.onAutoCompletePacketsReceived(destinationPort, portAttribute.packetSizes);
				}
			}
		}
	};
	
	public synchronized List<Integer> getCurrentPacketsSizes(int destinationPort) {
		PortPacketsCollection portAttribute = portMappings.get(destinationPort);
		if(portAttribute == null) {
			return null;
		}
		return new ArrayList<Integer>(portAttribute.packetSizes);
	}
	
	public boolean waitForPacketsToBeReceived(int destinationPort, long timeoutMilliseconds) {
		PortPacketsCollection portAttribute = portMappings.get(destinationPort);
		if(portAttribute == null) {
			portAttribute = new PortPacketsCollection();
			portMappings.put(destinationPort, portAttribute);
		}
		
		synchronized (portAttribute.lock) {
			while (!portAttribute.hasReceivedEmptyHTTPPacket) {
				try {
					portAttribute.lock.wait(timeoutMilliseconds);
					if(!portAttribute.hasReceivedEmptyHTTPPacket) {
						return false;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return true;
		}
	}
	
	public void startNextIteration(int destinationPort) {
		PortPacketsCollection portAttribute = portMappings.get(destinationPort);
		if(portAttribute == null) {
			portAttribute = new PortPacketsCollection();
			portMappings.put(destinationPort, portAttribute);
		}
		
		synchronized (portAttribute.lock) {
			portAttribute.packetSizes.clear();
			portAttribute.hasReceivedEmptyHTTPPacket = false;
		}
	}
	
	public void destroy() throws NotOpenException {
		sniffer.destroy();
	}
	
	public Sniffer getSniffer() {
		return sniffer;
	}
	
	public void registerAutoCompletePacketsReceivedListener(AutoCompletePacketsReceivedListener listener) {
		listeners.add(listener);
	}
	
	public interface AutoCompletePacketsReceivedListener {
		void onAutoCompletePacketsReceived(int port, List<Integer> packetSizes);
	}
	
	private static class PortPacketsCollection {
		private List<Integer> packetSizes = new ArrayList<Integer>();
		private Object lock = new Object();
		private boolean hasReceivedEmptyHTTPPacket = false;
		
	}
}
