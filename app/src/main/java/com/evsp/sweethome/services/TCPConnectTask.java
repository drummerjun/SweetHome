package com.evsp.sweethome.services;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.evsp.sweethome.Constants;
import com.evsp.sweethome.SweetHome;

public class TCPConnectTask extends AsyncTask<String, String, TCPClient> {
    private static final String TAG = "TCPTask";
    private Application app;
    private Activity activity;
    private String error;

    public TCPConnectTask(Activity activity, Application app) {
        this.activity = activity;
        this.app = app;
    }

    @Override
    protected TCPClient doInBackground(String... message) {
        TCPClient client;

        SharedPreferences pref = activity.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        String server = pref.getString(Constants.KEY_SERVER, "");
        int port = pref.getInt(Constants.KEY_PORT, 5491);

        client = ((SweetHome)app).getClient();
        if(client == null) {
            client = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    publishProgress(message);
                }
            });
        }
        ((SweetHome)app).setClient(client);
        client.connect(server, port);
        return client;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        String action;
        super.onProgressUpdate(values);
        if(!values[0].isEmpty()) {
            if(values[0].equals(Constants.MESSAGE_CONNECTED)) {
                action = Constants.ACTION_LOGIN_FEEDBACK;
            } else {
                action = Constants.ACTION_READ_MESSAGE;
            }
            sendFeedback(action, values[0]);
        }
    }

    @Override
    protected void onPostExecute(TCPClient client) {
        super.onPostExecute(client);
        if(!client.isConnected()) {
            ((SweetHome)app).setConnected(false);
            error = client.getErrorMessage();
            sendFeedback(Constants.ACTION_LOGIN_FEEDBACK, Constants.MESSAGE_ERROR);
        }
    }

    private void sendFeedback(String action, String message) {
        Intent intent = new Intent(action);
        intent.putExtra(Constants.MESSAGE, message);
        if(message.equals(Constants.MESSAGE_ERROR)) {
            intent.putExtra(Constants.ERROR, error);
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
    }
}