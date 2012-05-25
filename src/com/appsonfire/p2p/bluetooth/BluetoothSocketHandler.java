package com.appsonfire.p2p.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.appsonfire.p2p.Message;
import com.appsonfire.p2p.NetworkListener;
import com.appsonfire.p2p.util.Logger;

public class BluetoothSocketHandler extends Thread {

	
	private final BluetoothSocket socket;
	private final String destinationAddress;
	private boolean running = true;
	private Message ackWaitingMessage = null;
	private Lock ackLock = new ReentrantLock(true);
	private Condition ackReceivedCondition = ackLock.newCondition();
	private NetworkListener networkListener;
	private BluetoothAdapter bluetoothAdapter;
	
	public BluetoothSocketHandler(BluetoothSocket socket) {
		super();
		this.socket = socket;
		this.destinationAddress = socket.getRemoteDevice().getAddress();
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		setName("BluetoothSocketHandler_"+socket.getRemoteDevice().getAddress());
	}
	
	
	public void setNetworkListener(NetworkListener networkListener) {
		this.networkListener = networkListener;
	}
	
	@Override
	public void run() {
		super.run();
		Logger.d("BluetoothSocketHandler is running for client " + this.destinationAddress); 
		try {
			InputStream inputStream = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			while (running) {
				String message = reader.readLine();
				Message decodedMessage = Message.decode(message);
				if (decodedMessage.isAck()) {
					notifyAckWaitingThreads(decodedMessage);
				} else {
					if (networkListener != null) {
						networkListener.messageReceived(decodedMessage);
					}
				}
			}
			Logger.i("BluetoothSocketHandler exiting...");
		} catch (IOException e) {
			Logger.e("Exception caught while listening client messages. " + this.destinationAddress, e);
			if (networkListener != null) {
				networkListener.connectionLost(this.destinationAddress);
			}
		}
		//TODO remove this socket handler from client socket handler list
		//clientSocketHandlers.remove(this.destinationAddress);
	}
	
	private void notifyAckWaitingThreads(Message ack) {
		if (this.ackWaitingMessage == null) {
			return;
		}
		if (ack.getSequenceId() == this.ackWaitingMessage.getSequenceId()) {
			this.ackWaitingMessage = null;
			try {
				ackLock.lock();
				ackReceivedCondition.signalAll();
			} finally {
				ackLock.unlock();
			}
		}
		
	}

	public void sendMessage(Message message, long timeout) throws InterruptedException {
		this.sendMessage(message);
		this.waitForAcknowledgement(message, timeout);
	}
	
	public void sendMessage(Message message) {
        OutputStream outStream;
		try {
			outStream = this.socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outStream);
            message.setSourceAddress(this.bluetoothAdapter.getAddress());
            writer.println(message.encode());
            writer.flush();	
		} catch (IOException e) {
			Log.w("Failed to send message  " + message + " to destination " + destinationAddress, e);
		}
	}
	
	public void terminate() {
		//TODO remove this socket handler from client socket handlers list
		//clientSocketHandlers.remove(this.destinationAddress);
		this.running = false;
		try {
			this.socket.close();
		} catch (IOException e) {
			Log.w("Exception caught while destroying BluetoothSocketHandler.", e);
		}
		this.interrupt();
	}

	public void waitForAcknowledgement(Message message, long timeout) throws InterruptedException {
		this.ackWaitingMessage = message;
		try {
			ackLock.lock();
			boolean ackReceived = this.ackReceivedCondition.await(timeout, TimeUnit.MILLISECONDS);
			if ( ! ackReceived) {
				throw new InterruptedException("Ack is not received for " + message + " in " + timeout + " ms.");
			}
		} finally {
			ackLock.unlock();
		}
	}
}
