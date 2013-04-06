package com.appsonfire.p2p.util;

import android.util.Log;

public class Logger {
	public static final String TAG = "p2p4Android";
	public static int logLevel = Log.INFO;
	

	public static final void d(String s) {
		if (Log.DEBUG >= logLevel) {
			Log.d(TAG, s);
		}
	}

	public static final void i(String s) {
		if (Log.INFO >= logLevel) {
			Log.i(TAG, s);
		}
	}

	public static final void w(String s) {
		if (Log.WARN >= logLevel) {
			Log.w(TAG, s);
		}
	}

	public static final void w(String s, Exception e) {
		if (Log.WARN >= logLevel) {
			Log.w(TAG, s, e);
		}		
	}

	public static final void e(String s) {
		if (Log.ERROR >= logLevel) {
			Log.e(TAG, s);
		}
	}

	public static final void e(String s, Exception e) {
		if (Log.ERROR >= logLevel) {
			Log.e(TAG, s, e);
		}
	}
}
