package com.appsonfire.p2p;

public interface NetworkListener {
	
	public void messageReceived(Message message);

	public void connectionLost(String endPoint);
	
}
