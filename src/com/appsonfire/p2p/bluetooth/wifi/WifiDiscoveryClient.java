package com.appsonfire.p2p.bluetooth.wifi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

public class WifiDiscoveryClient {

	private DatagramPacket requestDatagramPacket;
	private byte[] responseData = new byte[1024];
	private DatagramPacket responseDatagramPacket = new DatagramPacket(this.responseData, this.responseData.length);

	public void discoverServer(Context context, WifiDiscoveryClientListener listener) {
		startAsyncDiscovery(context, listener);
	}

	private void startAsyncDiscovery(final Context context, final WifiDiscoveryClientListener listener) {

		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					DatagramSocket socket = new DatagramSocket(WifiDiscoveryServer.DISCOVERY_PORT);
					socket.setBroadcast(true);
					InetAddress broadcastAddress = InetAddress.getByAddress(getBroadcastIPAddressRaw(context));

					final byte[] out = WifiDiscoveryServer.REQUEST_ID;
					requestDatagramPacket = new DatagramPacket(out, out.length, broadcastAddress, WifiDiscoveryServer.DISCOVERY_PORT);

					socket.send(requestDatagramPacket);

					socket.receive(responseDatagramPacket);
					final byte[] discoveryResponseData = new byte[responseDatagramPacket.getLength()];
					System.arraycopy(responseDatagramPacket.getData(),responseDatagramPacket.getOffset(), discoveryResponseData, 0, responseDatagramPacket.getLength());
					listener.onDiscover(String.valueOf(discoveryResponseData));
				} catch (Exception e) {
					listener.onException(e);
				}
			}
		}).start();
	}

	public static byte[] getBroadcastIPAddressRaw(final Context context) {
		// TODO what if wifi manager is null...
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifiManager.getDhcpInfo();
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		final byte[] broadcastIP = new byte[4];
		for (int k = 0; k < 4; k++) {
			broadcastIP[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		}
		return broadcastIP;
	}
}
