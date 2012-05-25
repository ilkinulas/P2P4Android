package com.appsonfire.p2p;

public interface NetworkServiceCallback {

	public static final int REASON_BLUETOOTH_IS_NOT_AVAILABLE = 1;

	public static final int REASON_BLUETOOTH_DISCOVERY_STARTED = 2;

	public static final int REASON_BLUETOOTH_DISCOVERY_FINISHED = 3;

	public static final int REASON_BLUETOOTH_IS_NOT_ENABLED = 4;

	public static final int REASON_BLUETOOTH_IS_ENABLED = 5;

	public static final int REASON_BLUETOOTH_DISCOVERABLE_MODE_IS_ON = 6;

	public static final int REASON_BLUETOOTH_NOT_DISCOVERABLE = 7;
	
	public static final int REASON_BLUETOOTH_CAN_NOT_CREATE_SERVER_SOCKET = 8;

	public static final int REASON_BLUETOOTH_CAN_NOT_CONNECT_TO_SERVER = 9;

	public static final int REASON_BLUETOOTH_CAN_NOT_FIND_SERVER = 10;
	
	public static final int REASON_CANCELED_BY_USER = 11;

	public void onSuccess(NetworkService networkService);

	public void onFailure(int reason);
}
