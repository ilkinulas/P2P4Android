package com.appsonfire.p2p.bluetooth.wifi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.appsonfire.p2p.util.Logger;

public class WifiDiscoveryServer extends Thread {

	public static final int DISCOVERY_PORT = 11211;
	public static final byte[] REQUEST_ID = "com.appsonfire.p2p".getBytes();

	private boolean running = true;

	private DatagramSocket datagramSocket;
	private final byte[] requestData = new byte[1024];
	private final DatagramPacket datagramPacket = new DatagramPacket(this.requestData, this.requestData.length);

	private final WifiDiscoveryServerListener listener;

	private final String ipAddress;

	public WifiDiscoveryServer(String ipAddress, WifiDiscoveryServerListener listener) {
		this.ipAddress = ipAddress;
		this.listener = listener;
	}

	@Override
	public void run() {
		super.run();
		try {
			datagramSocket = new DatagramSocket(DISCOVERY_PORT);
		} catch (SocketException e) {
			Logger.e("Failed to start wifi discovery server", e);
			if (listener != null) {
				listener.onException(e);
			}
			return;
		}
		while (running && !Thread.interrupted()) {
			try {
				this.datagramSocket.receive(this.datagramPacket);
				byte[] data = this.datagramPacket.getData();
				int aStart = 0;
				int bStart = this.datagramPacket.getOffset();
				boolean isDiscoveryMessage = true;
				for (int i = aStart, j = bStart; i < REQUEST_ID.length; i++, j++) {
					if (REQUEST_ID[i] != data[j]) {
						isDiscoveryMessage = false;
						break;
					}
				}
				if (isDiscoveryMessage) {
					byte[] response = this.ipAddress.getBytes();
					this.datagramSocket.send(new DatagramPacket(response, response.length, datagramPacket.getAddress(), datagramPacket.getPort()));
					listener.onDiscoveryMessageReceived();
				}
			} catch (IOException e) {
				if (listener != null) {
					Logger.e("Failed to reveice datagram packet", e);
					listener.onException(e);
				}
			}
		}
	}

	public void terminate() {
		running = false;
		if (this.datagramSocket != null) {
			this.datagramSocket.close();
		}
		interrupt();
	}
}
