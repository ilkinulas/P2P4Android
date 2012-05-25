package com.appsonfire.p2p.bluetooth.wifi;


public interface WifiDiscoveryServerListener {

	public void onException(Exception e);

	public void onDiscoveryMessageReceived();

}
