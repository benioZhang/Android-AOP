package com.benio.ast.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.benio.ast.TrackEvent;

/**
 * 测试几种常见的打Log的方式
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @TrackEvent("main_onCreate_event")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: ");
    }

    @TrackEvent("main_onStart_event")
    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) {
            Log.d(TAG, "onStart: ");
            System.out.println("onStart:");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d("onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "onDestroy: ");
        System.out.println("onDestroy:");
    }
}
