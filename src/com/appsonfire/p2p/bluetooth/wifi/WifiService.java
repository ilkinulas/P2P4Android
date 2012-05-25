package com.appsonfire.p2p.bluetooth.wifi;

import com.appsonfire.p2p.ClientConnectionCallback;
import com.appsonfire.p2p.Message;
import com.appsonfire.p2p.NetworkListener;
import com.appsonfire.p2p.NetworkService;
import com.appsonfire.p2p.ServerReadyCallback;

public class WifiService implements NetworkService {

	@Override
	public void registerNetworkListener(NetworkListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startServer(ServerReadyCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startClient(String serverAddress, ClientConnectionCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isServer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendMessageToServer(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessageToServer(Message message, long timeout) throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessageToClient(String destination, Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessageToClient(String destination, Message message, long timeout) throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessageToClients(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessageToClients(Message message, String exclude) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessageToClients(Message message, long timeout) throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessageToClients(Message message, long timeout, String exclude) throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendAck(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMyNetworkAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
