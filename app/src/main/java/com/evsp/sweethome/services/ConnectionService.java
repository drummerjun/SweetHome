package com.evsp.sweethome.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.evsp.sweethome.Constants;

public class ConnectionService extends IntentService {
    private static final String TAG = ConnectionService.class.getSimpleName();
    public static volatile boolean shouldContinue = true;

    public ConnectionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        Intent i = new Intent(Constants.ACTION_SEND_MESSAGE);
        i.putExtra(Constants.MESSAGE, "hi");
        i.putExtra(Constants.SAVED, true);
        while(shouldContinue) {
            Log.d(TAG, "in loop...");
            try {
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                Thread.sleep(120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "exit service");
    }
}