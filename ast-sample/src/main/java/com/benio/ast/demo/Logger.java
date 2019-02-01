package com.benio.ast.demo;

import android.util.Log;

public class Logger {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Logger";

    public static void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
