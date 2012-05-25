#P2P4Android
## Peer to peer messaging framework for Android.

* This is an android library project.
* Sends and receives text messages.
* Messages are decoded/encoded with json.
* [Okey Mini](https://play.google.com/store/apps/details?id=com.appsonfire.okey) multiplayer bluetooth game uses this library.  

Bluetooth server start example:
```java
P2PBluetooth p2p = new P2PBluetooth();
p2p.startBluetoothServer(this, new NetworkServiceCallback() {
	
	@Override
	public void onSuccess(NetworkService networkService) {
		//Register a network listener for receiving messages from peers.
		networkService.registerNetworkListener(this);
		
		//You can send messages to peers through networkService.
	}
	
	@Override
	public void onFailure(int reason) {
		/*
		 * For a list of failure reasons see 
		 * https://github.com/ilkinulas/P2P4Android/blob/master/src/com/appsonfire/p2p/NetworkServiceCallback.java
		 */
	}
});
```

Bluetooth client start example:
```java
P2PBluetooth p2p = new P2PBluetooth();
p2p.startBluetoothClient(this, new NetworkServiceCallback() {
	
	@Override
	public void onSuccess(NetworkService networkService) {
		//Register a network listener for receiving messages from peers.
		networkService.registerNetworkListener(this);
		
		//You can send messages to peers through networkService.
	}
	
	@Override
	public void onFailure(int reason) {
		/*
		 * For a list of failure reasons see 
		 * https://github.com/ilkinulas/P2P4Android/blob/master/src/com/appsonfire/p2p/NetworkServiceCallback.java
		 */
	}
});