package com.alexsebbe;

import java.util.ArrayList;
import java.util.List;

import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.PcapStat;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

@SuppressWarnings("javadoc")
public class Sniffer {
	private static final int INFINITY = -1;
	private static final String READ_TIMEOUT_KEY = Sniffer.class.getName()
			+ ".readTimeout";
	private static final int READ_TIMEOUT = Integer.getInteger(
			READ_TIMEOUT_KEY, 10); // [ms]

	private static final String SNAPLEN_KEY = Sniffer.class.getName() + ".snaplen";
	private static final int SNAPLEN = Integer.getInteger(SNAPLEN_KEY, 65536); // [bytes]

	private PcapHandle handle;
	private List<OnDataPackageReceivedListener> onDataRecievedListeners = new ArrayList<OnDataPackageReceivedListener>();
	
	public void setIPSourceFilfer(String ipAddress) throws NotOpenException, PcapNativeException {
		if(ipAddress != null && !ipAddress.isEmpty()) {
			handle.setFilter("src host " + ipAddress, BpfCompileMode.OPTIMIZE);
		}
	}
	
	public void run(PcapNetworkInterface nif, List<String> srcIPAddressFilters, final int sourcePortFilter) throws PcapNativeException, NotOpenException {
		System.out.println("Using interface: " + nif.getName());

		handle = nif.openLive(SNAPLEN,
				PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);

		if(srcIPAddressFilters != null && !srcIPAddressFilters.isEmpty()) {
			String filter = "src host " + srcIPAddressFilters.get(0);
			boolean processedFirst = false;
			for(String f : srcIPAddressFilters) {
				if(!processedFirst) {
					processedFirst = true;
					continue;
				}
				filter += " or src host " + f;
			}
			handle.setFilter(filter, BpfCompileMode.OPTIMIZE);
		}

		PacketListener listener = new PacketListener() {
			
			public void gotPacket(Packet packet) {
				TcpPacket tcpPacket = packet.get(TcpPacket.class);
				IpV4Packet ipPacket = packet.get(IpV4Packet.class);
				
				// Disregard any packet not meant for the filtered source port
				if(tcpPacket == null || tcpPacket.getHeader().getSrcPort().valueAsInt() != sourcePortFilter) {
					return;
				}
				
				//System.out.println(packet);
				
				try {
					Packet tcpPayload = packet.getPayload().getPayload();
					if(tcpPayload.getPayload() != null) {
						for(OnDataPackageReceivedListener l : onDataRecievedListeners) {
							l.onDataPackageReceived(tcpPayload.getPayload().length(), 
									ipPacket.getHeader().getSrcAddr().toString(), 
									ipPacket.getHeader().getDstAddr().toString(),
									tcpPacket.getHeader().getSrcPort().valueAsInt(),
									tcpPacket.getHeader().getDstPort().valueAsInt(),
									handle.getTimestamp().getTime());
						}
					}
					
					
				} catch (NullPointerException e) {
					
				}
				
			}
		};

		try {
			handle.loop(INFINITY, listener);
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}
	
	public PcapStat getStats() throws PcapNativeException, NotOpenException {
		return handle.getStats();
	}
	
	public void destroy() throws NotOpenException {
		handle.breakLoop();
		handle.close();
	}
	
	public void addListener(OnDataPackageReceivedListener listener) {
		onDataRecievedListeners.add(listener);
	}
	
	public void removeListener(OnDataPackageReceivedListener listener) {
		onDataRecievedListeners.remove(listener);
	}
	
	public interface OnDataPackageReceivedListener {
		void onDataPackageReceived(int length, String IPSource, String IPDestination, int sourcePort, int destinationPort, long timeStamp);
	}
	
	/*System.out.println("ps_recv: " + ps.getNumPacketsReceived());
	System.out.println("ps_drop: " + ps.getNumPacketsDropped());
	System.out.println("ps_ifdrop: " + ps.getNumPacketsDroppedByIf());*/
}