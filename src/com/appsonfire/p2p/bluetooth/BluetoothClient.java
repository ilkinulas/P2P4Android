package com.appsonfire.p2p.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.appsonfire.p2p.ClientConnectionCallback;
import com.appsonfire.p2p.Message;
import com.appsonfire.p2p.NetworkListener;
import com.appsonfire.p2p.SeqIdGenerator;
import com.appsonfire.p2p.util.Logger;

public class BluetoothClient {

	private BluetoothAdapter bluetoothAdapter;
	private final String serverAddress;
	private NetworkListener networkListener;
	private BluetoothSocketHandler socketHandler;
	private final ClientConnectionCallback connectionEstablishedCallback; 
	
	public BluetoothClient(String serverAddress, ClientConnectionCallback connectionEstablishedCallback) {
		this.serverAddress = serverAddress;
		this.connectionEstablishedCallback = connectionEstablishedCallback;
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public void registerNetworkListener(NetworkListener listener) {
		this.networkListener = listener;
		if (this.socketHandler != null) {
			this.socketHandler.setNetworkListener(this.networkListener);
		}
		Logger.d(listener + " registered as NetworkListener (client)");
	}
	
	public void start() {
		BluetoothSocket socket =  connect();
		if (socket == null) {
			if (connectionEstablishedCallback != null) {
				connectionEstablishedCallback.connectionFailed();
			}
			return;
		}

		this.socketHandler = new BluetoothSocketHandler(socket);
		this.socketHandler.setNetworkListener(this.networkListener);
		this.socketHandler.start();
		this.connectionEstablishedCallback.connectionEstablished();
	}

	public void sendMessage(Message message) {
		if ( ! message.isAck()) {
			message.setSequenceId(SeqIdGenerator.nextSeqId());
		}
		Logger.d("Sending message " + message + " to server");
		this.socketHandler.sendMessage(message);
	}

	public void sendMessage(Message message, long timeout) throws InterruptedException {
		if ( ! message.isAck()) {
			message.setSequenceId(SeqIdGenerator.nextSeqId());
		}
		Logger.d("Sending message " + message + " to server");
		this.socketHandler.sendMessage(message, timeout);
	}
	
	private BluetoothSocket connect() {
		BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(serverAddress);
		for (int i = 0; i < BluetoothService.UUIDs.length; i++) {
			BluetoothSocket bluetoothSocket = null;
			UUID uuid = BluetoothService.UUIDs[i];
			try {
				bluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(uuid);
			} catch (IOException e) {
				Logger.w("IOException caught while executing createRfcommSocketToServiceRecord");
				doSleep(3000L);
				continue;
			}
			try {
				bluetoothSocket.connect();
			} catch (IOException e) {
				Logger.w("IOException caught while connecting bluetooth socket. UUID = " + uuid , e);
				doSleep(500L);
				continue;
			}
			return bluetoothSocket;
		}
		Logger.w("Can not connect to server@" + this.serverAddress);
		return null;
	}

	private static void doSleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException interruptedException) {
			Logger.e("Sleep interrupted. " , interruptedException);
		}
	}

	public void terminate() {
		Logger.i("Terminating BluetoothClient.");
		if (this.socketHandler != null ) {
			this.socketHandler.terminate();
		}
	}
}
