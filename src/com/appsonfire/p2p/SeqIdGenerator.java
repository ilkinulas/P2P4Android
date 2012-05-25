package com.appsonfire.p2p;

import java.util.concurrent.atomic.AtomicLong;

public class SeqIdGenerator {
	private static final AtomicLong seqId = new AtomicLong(1);
	
	public static final long nextSeqId() {
		return seqId.getAndIncrement();
	}
}
