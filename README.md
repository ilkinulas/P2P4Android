#P2P4Android
## Peer to peer messaging framework for Android.

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