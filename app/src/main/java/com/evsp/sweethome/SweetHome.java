package com.evsp.sweethome;

import android.app.Application;

import com.evsp.sweethome.services.TCPClient;

public class SweetHome extends Application {
    private boolean isConnected = false;
    private TCPClient client;

    public void setConnected(boolean value) {
        isConnected = value;
    }

    public boolean getConnected() {
        return isConnected;
    }

    public void setClient(TCPClient client) {
        this.client = client;
        isConnected = true;
    }

    public TCPClient getClient() {
        return this.client;
    }
}
