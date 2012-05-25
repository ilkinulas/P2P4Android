package com.appsonfire.p2p.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;

import com.appsonfire.p2p.ClientConnectionCallback;
import com.appsonfire.p2p.NetworkServiceCallback;
import com.appsonfire.p2p.ServerReadyCallback;
import com.appsonfire.p2p.util.Logger;

public class P2PBluetooth {

	private boolean clientConnectionSuccess = false;
	private String savedBluetoothName;
	private static final String MAGIC_BT_NAME_PREFIX = "=p2p=";
	
	private BroadcastReceiver bluetoothStateBroadcastReceiver;
	private BroadcastReceiver bluetoothClientStateBroadcastReceiver;
	private BroadcastReceiver bluetoothScanmodeBroadcastReceiver;
	
	private BluetoothService bluetoothService;
	
	/**
	 * Enables bluetooth and makes the device discoverable.
	 * @param context android context. 
	 * @param callback Callback is used to receive async updates about server start
	 */
	public void startBluetoothServer(final Context context, final NetworkServiceCallback callback) {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			callback.onFailure(NetworkServiceCallback.REASON_BLUETOOTH_IS_NOT_AVAILABLE);
			return;
		}

		if (bluetoothAdapter.isEnabled()) {
			prepareServerBluetoothAdapter(context, callback, bluetoothAdapter);
		} else {
			bluetoothStateBroadcastReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
					switch (newState) {
					case BluetoothAdapter.STATE_TURNING_ON:
						Logger.i("Turning on bluetooth adapter. STATE_TURNING_ON ");
						break;
					case BluetoothAdapter.STATE_ON:
						Logger.i("Bluetooth adapter is enabled. STATE_ON ");
						doUnregisterReceiver(context, this);
						prepareServerBluetoothAdapter(context, callback, bluetoothAdapter);
						break;
					case BluetoothAdapter.STATE_TURNING_OFF:
					case BluetoothAdapter.STATE_OFF:
						doUnregisterReceiver(context, this);
						callback.onFailure(NetworkServiceCallback.REASON_BLUETOOTH_IS_NOT_ENABLED);
						break;
					default:
						Logger.w("Unexpected BluetoothAdapter state received. STATE : " + newState);
					}
				}
			};
			context.registerReceiver(bluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
			bluetoothAdapter.enable();
		}
	}

	private void prepareServerBluetoothAdapter(Context context, final NetworkServiceCallback callback, final BluetoothAdapter bluetoothAdapter) {
		changeBluetoothAdapterName(bluetoothAdapter);
		
		if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			startBluetoothServerService(callback);
			return;				
		}
		
		bluetoothScanmodeBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Logger.i("Scan mode broadcast receiver received action " + action);
				int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
				switch (scanMode) {
				case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
					Logger.i("Bluetooth scan mode is CONNECTABLE & DISCOVERABLE");
					doUnregisterReceiver(context, this);
					startBluetoothServerService(callback);
					break;
				default:
					Logger.w("Unexpected scan mode received : " + scanMode);
					doUnregisterReceiver(context, this);
					callback.onFailure(NetworkServiceCallback.REASON_BLUETOOTH_NOT_DISCOVERABLE);
					break;
				}
			}
		};
		
		context.registerReceiver(bluetoothScanmodeBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
		Intent i = new Intent();
		i.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
		context.startActivity(i);
		
		cancelDiscoverableActivityWorkaround(context, callback);
	}

	/**
	 * When the activity started by ACTION_REQUEST_DISCOVERABLE is canceled we can not receive an activity result.
	 * (Activity is not started with startActivityForResult)
	 * This method spawns a thread that checks every second if the bluetooth service is started. If the service is not started
	 * for 10 seconds then we assume that user has canceled activity.
	 * @param context
	 * @param callback
	 */
	private void cancelDiscoverableActivityWorkaround(final Context context, final NetworkServiceCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 10; i++) {
					if (bluetoothService != null && bluetoothService.isBluetoothServerStarted()) {
						Logger.d("cancelDiscoverableActivityWorkaround : Bluetooth service started.");
						return;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Logger.e("cancelDiscoverableActivityWorkaround : interrupted", e);
					}
				}
				Logger.d("cancelDiscoverableActivityWorkaround : BT is not discovarable");
				doUnregisterReceiver(context, bluetoothScanmodeBroadcastReceiver);
				callback.onFailure(NetworkServiceCallback.REASON_BLUETOOTH_NOT_DISCOVERABLE);
			}
		}).start();
	}

	private void startBluetoothServerService(final NetworkServiceCallback callback) {
		if (this.bluetoothService != null) {
			this.bluetoothService.terminate();
		}
		this.bluetoothService = new BluetoothService();
		this.bluetoothService.startServer(new ServerReadyCallback() {
			@Override
			public void initFailure() {
				callback.onFailure(NetworkServiceCallback.REASON_BLUETOOTH_CAN_NOT_CREATE_SERVER_SOCKET);
			}

			@Override
			public void acceptingConnections() {
				callback.onSuccess(bluetoothService);
			}
		});
	}
	
	/**
	 * changes bluetooth adapter name. Name is replaced with "MAGIC_PREFIX + Name"	 
	 * Name is restored while disabling bluetooth adapter.
	 * This magic prefix is used by clients to identify the bluetooth server.
	 * @param bluetoothAdapter
	 */
	private void changeBluetoothAdapterName(final BluetoothAdapter bluetoothAdapter) {
		Logger.d("Changing bluetooth adater name...");
		String btName = bluetoothAdapter.getName();
		if (btName == null) {
			Logger.w("Failed to read bluetooth adapter name. bluetoothAdapter.getName() returns null.");
			return;
		}
		
		if (btName.startsWith(MAGIC_BT_NAME_PREFIX)) {
			Logger.d("No need to change bluetooth adapter name. Name : " + btName);
		} else {
			savedBluetoothName = btName;
			bluetoothAdapter.setName(MAGIC_BT_NAME_PREFIX + btName);
			Logger.d("Bluetooth name has been changed. New name is " + bluetoothAdapter.getName());
		}
	}
	
	public void revertBluetoothAdapterName() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null) {
			return;
		}
		
		if (this.savedBluetoothName == null) {
			String name = adapter.getName();
			if (name != null && name.startsWith(MAGIC_BT_NAME_PREFIX)) {
				name = name.substring(MAGIC_BT_NAME_PREFIX.length(), name.length());
				if (adapter.setName(name)) {
					Logger.i("Bluetooth adapter name is changed to " + name);
				}	
			}
		} else {
			if (adapter.setName(this.savedBluetoothName)) {
				Logger.i("Bluetooth adapter name is set to initial value : " + this.savedBluetoothName);
			}
			this.savedBluetoothName = null;
		}
	}

	public void startBluetoothClient(Context context, final NetworkServiceCallback callback) {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			callback.onFailure(NetworkServiceCallback.REASON_BLUETOOTH_IS_NOT_AVAILABLE);
			return;
		}

		if (bluetoothAdapter.isEnabled()) {
			startBluetoothDiscovery(context, callback, bluetoothAdapter);
		} else {
			bluetoothClientStateBroadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
					switch (newState) {
					case BluetoothAdapter.STATE_TURNING_ON:
						Logger.i("Turning on bluetooth adapter. STATE_TURNING_ON ");
						break;
					case BluetoothAdapter.STATE_ON:
						doUnregisterReceiver(context, this);
						startBluetoothDiscovery(context, callback, bluetoothAdapter);
						break;
					case BluetoothAdapter.STATE_TURNING_OFF:
					case BluetoothAdapter.STATE_OFF:
						doUnregisterReceiver(context, this);
						callback.onFailure(NetworkServiceCallback.REASON_BLUETOOTH_IS_NOT_ENABLED);
						break;
					default:
						Logger.w("Unexpected BluetoothAdapter state received. STATE : " + newState);
					}
				}
			};
			
			context.registerReceiver(bluetoothClientStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
			bluetoothAdapter.enable();
		}
	}

	private void startBluetoothDiscovery(Context context, final NetworkServiceCallback callback,
			final BluetoothAdapter bluetoothAdapter) {
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		context.registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					Parcelable btParcel = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					BluetoothDevice btDevice = (BluetoothDevice) btParcel;
					String btDeviceName = btDevice.getName();
					Logger.d("BT Device found. " + btDevice + " name : " + btDeviceName);
					if (btDeviceName != null && btDeviceName.startsWith(MAGIC_BT_NAME_PREFIX)) {
						bluetoothAdapter.cancelDiscovery();
						doUnregisterReceiver(context, this);
						String btServerAddress = btDevice.getAddress();
						if (bluetoothService != null) {
							bluetoothService.terminate();
						}
						bluetoothService = new BluetoothService();
						bluetoothService.startClient(btServerAddress, new ClientConnectionCallback() {
							@Override
							public void connectionEstablished() {
								clientConnectionSuccess = true;
								callback.onSuccess(bluetoothService);
							}

							@Override
							public void connectionFailed() {
								callback.onFailure(NetworkServiceCallback.REASON_BLUETOOTH_CAN_NOT_CONNECT_TO_SERVER);
							}
						});
					}
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					Logger.d("Bluetooth device search completed. ");
					doUnregisterReceiver(context, this);
					bluetoothAdapter.cancelDiscovery();
					if ( ! clientConnectionSuccess) {
						callback.onFailure(NetworkServiceCallback.REASON_BLUETOOTH_CAN_NOT_FIND_SERVER);
					}
				}
			}
		}, filter);
		bluetoothAdapter.startDiscovery();
	}
	
	public boolean stopBluetoothService(Context context) {
		doUnregisterReceiver(context, bluetoothScanmodeBroadcastReceiver);
		doUnregisterReceiver(context, bluetoothStateBroadcastReceiver);
		doUnregisterReceiver(context, bluetoothClientStateBroadcastReceiver);
		revertBluetoothAdapterName();
		if (this.bluetoothService!=null) {
			this.bluetoothService.terminate();
		}
		disableBluetoothAdapter();
		return true;
		
	}
	
	public void disableBluetoothAdapter() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		Logger.i("Disabling bluetooth adapter...");
		if (adapter != null ) {
			if (adapter.isEnabled()) {
				adapter.disable();
			}
		}
	}
	
	private void doUnregisterReceiver(Context context, BroadcastReceiver receiver) {
		if (context != null) {
			try {
				context.unregisterReceiver(receiver);
			} catch (Exception e) {
				Logger.w("Failed to unregister " + receiver, e);
			}
		}
	}
}
