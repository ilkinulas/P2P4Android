package com.appsonfire.p2p.bluetooth;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.appsonfire.p2p.Message;
import com.appsonfire.p2p.NetworkListener;
import com.appsonfire.p2p.SeqIdGenerator;
import com.appsonfire.p2p.ServerReadyCallback;
import com.appsonfire.p2p.util.Logger;

public class BluetoothServer extends Thread {

	private boolean running = true;
	private BluetoothAdapter bluetoothAdapter;
	private NetworkListener networkListener;
	private Map<String, BluetoothSocketHandler> clientSocketHandlers = new ConcurrentHashMap<String, BluetoothSocketHandler>();
	private ServerReadyCallback serverReadyCallback;
	private BluetoothServerSocket serverSocket;
	
	public BluetoothServer(ServerReadyCallback callback) {
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.serverReadyCallback = callback;
		Logger.d("New Bluetooth Server instance created.");
	}
	
	public void registerNetworkListener(NetworkListener listener) {
		this.networkListener = listener;
		Collection<BluetoothSocketHandler> handlers = this.clientSocketHandlers.values();
		for (BluetoothSocketHandler handler : handlers) {
			handler.setNetworkListener(this.networkListener);
		}
		Logger.d(listener + " registered as NetworkListener (server)");
	}
	

	@Override
	public void run() {
		boolean notifyAcceptingConnections = true;
		try {
			Logger.i("Bluetooth Server started.");
			for (UUID uuid : BluetoothService.UUIDs) {
				if (! running) {
					break;
				}
				serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(BluetoothService.BLUETOOTH_SERVICE_NAME, uuid);
				if (notifyAcceptingConnections) {
					//before accepting  first client connection notify serverReadyCallback.
					serverReadyCallback.acceptingConnections();
					notifyAcceptingConnections = false;
				}
				Logger.d("Server socket created UUID " + uuid);
				BluetoothSocket bluetoothSocket = serverSocket.accept();
				serverSocket.close();

				BluetoothSocketHandler socketHandler = new BluetoothSocketHandler(bluetoothSocket);

				String destination = bluetoothSocket.getRemoteDevice().getAddress();
				this.clientSocketHandlers.put(destination, socketHandler);
				Logger.d("Client address " + destination + " saved.");
				if (this.networkListener != null) {
					socketHandler.setNetworkListener(this.networkListener);
				}
			
				socketHandler.start();
			}
			Logger.w("MAX number of clients reached. MAX = " + BluetoothService.UUIDs.length);
		} catch (Exception e) {
			Logger.e("BluetoothServer failed to listen for incomming connections.", e);
			serverReadyCallback.initFailure();
		}
		Logger.i("BluetoothServer exiting...");
	}
	
	
	/**
	 * Waits for an acknowledgement. If ack is not received until timeout miliseconds calling thread is interrupted. 
	 * @param destination
	 * @param message
	 * @param timeout in miliseconds
	 */
	public void sendMessage(String destination, Message message, long timeout)  throws InterruptedException {
		BluetoothSocketHandler clientHandler = this.clientSocketHandlers.get(destination);
		if (! message.isAck()) {
			message.setSequenceId(SeqIdGenerator.nextSeqId());
		}
		if (clientHandler == null) {
			Logger.w("Client socket handler for destination " + destination + " is null");
			return;
		}
		Logger.d("Sending message " + message + " to " + destination);
		clientHandler.sendMessage(message, timeout);
	}
	
	public void sendMessage(String destination, Message message) {
		BluetoothSocketHandler clientHandler = this.clientSocketHandlers.get(destination);
		if (! message.isAck()) {
			message.setSequenceId(SeqIdGenerator.nextSeqId());
		}
		if (clientHandler == null) {
			Logger.w("Client socket handler for destination " + destination + " is null");
			return;
		}
		Logger.d("Sending message " + message + " to " + destination);
		clientHandler.sendMessage(message);
	}
	
	public void sendMessage(Message message) {
		sendMessage(message, null);
	}
	
	public void sendMessage(Message message, String exclude) {
		Set<Entry<String, BluetoothSocketHandler>> entrySet = this.clientSocketHandlers.entrySet();
		for (Entry<String, BluetoothSocketHandler> entry: entrySet) {
			String client = entry.getKey();
			if ( ! client.equals(exclude)) {
				sendMessage(client, message);
			}
		}
	}
	
	public void sendMessage(Message message, long timeout)  throws InterruptedException{
		sendMessage(message, timeout, null);
	}
	
	public void sendMessage(Message message, long timeout, String exclude)  throws InterruptedException {
		Set<Entry<String, BluetoothSocketHandler>> entrySet = this.clientSocketHandlers.entrySet();
		for (Entry<String, BluetoothSocketHandler> entry: entrySet) {
			String client = entry.getKey();
			if (client.equals(exclude)) {
				continue;
			}
			sendMessage(client, message, timeout);
		}
	}
	
	public void terminate() {
		Logger.i("Terminating BluetoothServer.");
		this.running = false;
		Set<Entry<String, BluetoothSocketHandler>> entrySet = this.clientSocketHandlers.entrySet();
		for (Entry<String, BluetoothSocketHandler> entry : entrySet) {
			entry.getValue().terminate();
		}
		this.clientSocketHandlers.clear();
		if (this.serverSocket != null) {
			try {
				this.serverSocket.close();
			} catch (IOException e) {
				Logger.e("Exception caught while closing server socket", e);
			}
		}
	}

}
