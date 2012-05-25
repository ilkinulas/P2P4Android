package com.appsonfire.p2p;


//TODO complete javadoc documentation..
public interface NetworkService {
	public void registerNetworkListener(NetworkListener listener);

	public void startServer(ServerReadyCallback callback);

	public void startClient(String serverAddress, ClientConnectionCallback callback);

	public boolean isServer();

	public void sendMessageToServer(Message message);
	
	public void sendMessageToServer(Message message, long timeout) throws InterruptedException;

	public void sendMessageToClient(String destination, Message message);

	public void sendMessageToClient(String destination, Message message, long timeout) throws InterruptedException;
	
	public void sendMessageToClients(Message message);
	
	public void sendMessageToClients(Message message, String exclude);
	
	public void sendMessageToClients(Message message, long timeout) throws InterruptedException;
	
	public void sendMessageToClients(Message message, long timeout, String exclude) throws InterruptedException;
	
	public void sendAck(Message message);
	
	public void terminate();
	
	public String getMyNetworkAddress();
}
