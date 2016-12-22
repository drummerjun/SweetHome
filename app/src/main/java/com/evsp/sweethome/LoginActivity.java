package com.evsp.sweethome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.evsp.sweethome.adapters.InputPair;
import com.evsp.sweethome.services.TCPConnectTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

public class LoginActivity extends Activity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String PREF_ACTV = "actv_list";
    private static final String KEY_ARRAY = "array";

    private AutoCompleteTextView mServerActv;
    private EditText mPortEditText;
    private EditText mGatewayIDEditText;
    private String mServer;
    private int mPort;
    private ArrayList<InputPair> mInputList;
    private String[] mServerArray;
    private String[] mPortArray;

    private AlertDialog mDialog;
    private BroadcastReceiver receiver;

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        if(mDialog != null) {
            Log.d(TAG, "dismiss dialog");
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        mServerActv = (AutoCompleteTextView)findViewById(R.id.editText);
        mPortEditText = (EditText)findViewById(R.id.editText2);
        mGatewayIDEditText = (EditText)findViewById(R.id.editText3);
        Button scanButton = (Button)findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(LoginActivity.this).initiateScan();
            }
        });

        SharedPreferences pref = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        mServer = pref.getString(Constants.KEY_SERVER, "");
        if(!mServer.isEmpty()) {
            mServerActv.setText(mServer);
        }
        mPort = pref.getInt(Constants.KEY_PORT, -1);
        if(mPort > 0) {
            mPortEditText.setText(String.valueOf(mPort));
        }

        initServerArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, mServerArray);
        mServerActv.setThreshold(1);
        mServerActv.setAdapter(adapter);
        mServerActv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mServerActv.showDropDown();
                return false;
            }
        });
        mServerActv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPortEditText.setText(mPortArray[position]);
            }
        });

        Button connectButton = (Button)findViewById(R.id.button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Connect Button activated!");
                mServer = mServerActv.getText().toString();
                String port = mPortEditText.getText().toString();
                String id = mGatewayIDEditText.getText().toString();
                if(!mServer.isEmpty() && !port.isEmpty()) {  //login credentials check
                    SharedPreferences pref = getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Constants.KEY_SERVER, mServer).apply();
                    mPort = Integer.parseInt(port);
                    editor.putInt(Constants.KEY_PORT, mPort).apply();
                    updateServerArray(mServer, port);
                    Log.w(TAG, "saving server=" + mServer + "; saving port=" + mPort);
                    if(!id.isEmpty()) {
                        editor.putInt(Constants.KEY_ID, Integer.parseInt(id));
                    }
                    new TCPConnectTask(LoginActivity.this, getApplication()).execute("");
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_LOGIN_FEEDBACK);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "action=" + action);
                if(action.equals(Constants.ACTION_LOGIN_FEEDBACK)) {
                    String message = intent.getStringExtra(Constants.MESSAGE);
                    if(message.equals(Constants.MESSAGE_CONNECTED)) {
                        Log.d(TAG, "connected");
                        ((SweetHome)getApplication()).setConnected(true);
                        Intent i;
                        i = new Intent(LoginActivity.this, MainActivity.class);
                        gotoActivity(i);
                    } else if(message.equals(Constants.MESSAGE_ERROR)) {
                        String error = intent.getStringExtra(Constants.ERROR);
                        Log.d(TAG, "error: " + error);
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                                .setTitle(getResources().getString(R.string.fail))
                                .setMessage(error)
                                .setNegativeButton(getResources().getString(R.string.back), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setPositiveButton(getResources().getString(R.string.offline), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i;
                                        i = new Intent(LoginActivity.this, MainActivity.class);
                                        gotoActivity(i);
                                        //((SweetHome)getApplication()).setConnected(false);
                                    }
                                });
                        if(mDialog == null) {
                            Log.d(TAG, "creating new dialog");
                            mDialog = builder.create();
                        }
                        Log.d(TAG, "showing dialog");
                        mDialog.show();
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "scanresults=" + scanResult);
        mGatewayIDEditText.setText("");
        if (scanResult != null) {
            String results = scanResult.getContents();
            Log.d(TAG, "scanresults=" + results);
            mGatewayIDEditText.setText(results);
        }
    }

    private void gotoActivity(Intent i) {
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void initServerArray() {
        SharedPreferences pref = getSharedPreferences(PREF_ACTV, Context.MODE_PRIVATE);
        String savedJsonString = pref.getString(KEY_ARRAY, "");
        Gson gson = new Gson();
        if(savedJsonString.isEmpty()) {
            mInputList = new ArrayList<InputPair>();
        } else {
            Type type = new TypeToken<ArrayList<InputPair>>(){}.getType();
            mInputList = gson.fromJson(savedJsonString, type);
        }
        Iterator<InputPair> it = mInputList.iterator();
        int size = mInputList.size();
        mServerArray = new String[size];
        mPortArray = new String[size];
        int i = 0;
        while(it.hasNext()) {
            InputPair pair = it.next();
            mServerArray[i] = pair.getServer();
            mPortArray[i] = pair.getPort();
            i++;
        }
    }

    private void updateServerArray(String server, String port) {
        if(checkDuplicateEntry(server, port)) {
            return;
        }

        if(!server.isEmpty() && !port.isEmpty()) {
            mInputList.add(0, new InputPair(server, port));
        }

        Gson gson = new Gson();
        String savedJsonString = gson.toJson(mInputList);
        SharedPreferences pref = getSharedPreferences(PREF_ACTV, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_ARRAY, savedJsonString);
        editor.apply();
    }

    private boolean checkDuplicateEntry(String server, String port) {
        int size = mInputList.size();
        for(int i=0; i<size; i++) {
            if(server.compareTo(mServerArray[i]) == 0) {
                if(port.compareTo(mPortArray[i]) == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}