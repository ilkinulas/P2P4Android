package com.appsonfire.p2p.util;

import android.util.Log;

public class Logger {
	public static final String TAG = "p2p4Android";

	public static final void d(String s) {
		Log.d(TAG, s);
	}

	public static final void i(String s) {
		Log.i(TAG, s);
	}

	public static final void w(String s) {
		Log.w(TAG, s);
	}

	public static final void w(String s, Exception e) {
		Log.w(TAG, s, e);
	}

	public static final void e(String s) {
		Log.e(TAG, s);
	}

	public static final void e(String s, Exception e) {
		Log.e(TAG, s, e);
	}

}
