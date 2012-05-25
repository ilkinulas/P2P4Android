#P2P4Android
## Peer to peer messaging framework for Android.

```java
P2PBluetooth p2p = new P2PBluetooth();
p2p.startBluetoothServer(this, new NetworkServiceCallback() {
	
	@Override
	public void onSuccess(NetworkService networkService) {
		getOkeyApplication().setNetworkService(networkService);
		networkService.registerNetworkListener(ActivityServer.this);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressDialog.dismiss();
				makeToast(ActivityServer.this, getString(R.string.bt_server_ready));
				Button button = (Button) findViewById(R.id.start_bt_game_button);
				button.setEnabled(true);
			}
		});
	}
	
	@Override
	public void onFailure(int reason) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressDialog.dismiss();
				makeToast(ActivityServer.this, getString(R.string.bt_failed_to_start_server));
				//ActivityDashboard'a geri don.
				finish();
			}
		});
	}
});
```