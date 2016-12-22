package com.evsp.sweethome;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.evsp.sweethome.services.TCPConnectTask;

public class LoadingActivity extends Activity {
    private static final String TAG = LoadingActivity.class.getSimpleName();
    private BroadcastReceiver receiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_LOGIN_FEEDBACK);
        filter.addAction("com.evsp.sweethome.USB_PERMISSION");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent i;
                String action = intent.getAction();
                Log.d(TAG, "action=" + action);
                if(action.equals(Constants.ACTION_LOGIN_FEEDBACK)) {
                    String message = intent.getStringExtra(Constants.MESSAGE);
                    if(message.equals(Constants.MESSAGE_CONNECTED)) {
                        ((SweetHome)getApplication()).setConnected(true);
                        i = new Intent(LoadingActivity.this, MainActivity.class);
                    } else {
                        i = new Intent(LoadingActivity.this, LoginActivity.class);
                    }
                    gotoActivity(i);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    if(((SweetHome)getApplication()).getConnected()) {
                        gotoActivity(new Intent(LoadingActivity.this, MainActivity.class));
                    } else {
                        /*
                        Intent intent = new Intent(getApplicationContext(), com.ai.smarthomesdk.MainActivity.class);
                        gotoActivity(intent);
                        Utils.setClientId("test-client");
                        Utils.setClientSecret("test");
                        Utils.setListener(new ResponseListener() {
                            @Override
                            public void onResponse(AccessBean accessBean) {
                                String accessToken = accessBean.getAccess_token();
                                Log.d(TAG, "token=" + accessToken);
                                new TCPConnectTask(LoadingActivity.this, getApplication()).execute("");
                            }
                        });
                        */
                        new TCPConnectTask(LoadingActivity.this, getApplication()).execute("");
                    }
                    super.run();
                    sleep(1500);  //Delay of 1.5 seconds
                } catch (Exception e) {
                }
            }
        };
        welcomeThread.start();
    }

    private void gotoActivity(Intent i) {
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
