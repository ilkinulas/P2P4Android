package com.appsonfire.p2p;


public class Ack extends Message {

	public Ack(Message originalMessage) {
		this.ack = true;
		this.sequenceId = originalMessage.getSequenceId();
	}
	
}	
