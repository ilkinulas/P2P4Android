package com.appsonfire.p2p.bluetooth.wifi;


public interface WifiDiscoveryClientListener {

	public void onException(Exception e);

	public void onDiscover(String valueOf);

}
