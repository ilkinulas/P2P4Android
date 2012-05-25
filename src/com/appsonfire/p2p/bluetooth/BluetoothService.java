package com.appsonfire.p2p.bluetooth;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;

import com.appsonfire.p2p.Ack;
import com.appsonfire.p2p.ClientConnectionCallback;
import com.appsonfire.p2p.Message;
import com.appsonfire.p2p.NetworkListener;
import com.appsonfire.p2p.NetworkService;
import com.appsonfire.p2p.ServerReadyCallback;
import com.appsonfire.p2p.util.Logger;

public class BluetoothService implements NetworkService {

	public static final int SERVICE_TYPE_CLIENT = 1;
	public static final int SERVICE_TYPE_SERVER = 2;
	public static final String BLUETOOTH_SERVICE_NAME = "com.appsonfire.multiplayergamefw.bluetoothservice";

	private int type;

	public static final UUID[] UUIDs = { 
			UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c661"),
			UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c662"),
			UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c663"),
			UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c664"),
			UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c665"),
			UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"),
			UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c667"), };

	private BluetoothClient bluetoothClient = null;
	private BluetoothServer bluetoothServer = null;
	private boolean bluetoothServerStarted;
	
	public boolean isBluetoothServerStarted() {
		return this.bluetoothServerStarted;
	}
	
	@Override
	public void registerNetworkListener(NetworkListener listener) {
		if (isServer()) {
			if (this.bluetoothServer != null) {
				this.bluetoothServer.registerNetworkListener(listener);
			}			
		} else {
			if (this.bluetoothClient != null) {
				this.bluetoothClient.registerNetworkListener(listener);
			}
		}
	}

	@Override
	public void sendMessageToServer(Message message) {
		bluetoothClient.sendMessage(message);
	}

	@Override
	public void sendMessageToServer(Message message, long timeout) throws InterruptedException {
		bluetoothClient.sendMessage(message, timeout);
	}

	@Override
	public void sendMessageToClient(String destination, Message message) {
		bluetoothServer.sendMessage(destination, message);
	}

	@Override
	public void sendMessageToClient(String destination, Message message, long timeout) throws InterruptedException {
		bluetoothServer.sendMessage(destination, message, timeout);
	}
	

	@Override
	public void sendMessageToClients(Message message) {
	 	bluetoothServer.sendMessage(message);
	}
	
	@Override
	public void sendMessageToClients(Message message, String exclude) {
	 	bluetoothServer.sendMessage(message, exclude);
	}

	@Override
	public void sendMessageToClients(Message message, long timeout) throws InterruptedException {
		bluetoothServer.sendMessage(message, timeout);
	}	

	@Override
	public void sendMessageToClients(Message message, long timeout, String exclude) throws InterruptedException {
		bluetoothServer.sendMessage(message, timeout, exclude);
	}	
	
	@Override
	public void startServer(ServerReadyCallback callback) {
		Logger.d("Starting bluetooth server service.");
		this.type = SERVICE_TYPE_SERVER;
		if (this.bluetoothServer != null) {
			this.bluetoothServer.terminate();
			this.bluetoothServer.interrupt();
			this.bluetoothServer = null;
		} else {
			Logger.d("Bluetooth server is null. Creating a new one.");
		}
		this.bluetoothServer = new BluetoothServer(callback);
		this.bluetoothServer.start();
		this.bluetoothServerStarted = true;
	}

	@Override
	public void startClient(String serverAddress, ClientConnectionCallback callback) {
		Logger.d("Starting bluetooth client service.");
		this.type = SERVICE_TYPE_CLIENT;
		if (this.bluetoothClient != null ) {
			this.bluetoothClient.terminate();
		}
		this.bluetoothClient = new BluetoothClient(serverAddress, callback);
		this.bluetoothClient.start();
	}

	@Override
	public boolean isServer() {
		return this.type == SERVICE_TYPE_SERVER;
	}

	@Override
	public void sendAck(Message message) {
		Logger.d("Sending ACK for " + message);
		Ack ackMessage = new Ack(message);
		if (isServer()) {
			bluetoothServer.sendMessage(message.getSourceAddress(), ackMessage);
		} else {
			bluetoothClient.sendMessage(ackMessage);
		}
	}

	@Override
	public void terminate() {
		if (this.bluetoothClient != null) {
			this.bluetoothClient.terminate();
			this.bluetoothClient = null;
		}
		if (this.bluetoothServer != null) {
			this.bluetoothServer.terminate();
			this.bluetoothServer = null;
		}
		this.bluetoothServerStarted = false;
	}

	@Override
	public String getMyNetworkAddress() {
		return BluetoothAdapter.getDefaultAdapter().getAddress();
	}
}
