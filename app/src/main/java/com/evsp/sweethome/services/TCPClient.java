package com.evsp.sweethome.services;

import android.util.Log;

import com.evsp.sweethome.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private boolean mConnected = false;
    private Socket socket;
    private String serverMessage;
    private String error;
    private String serverAddr = "";
    private int port = 8080;
    private static final String TAG = "TCP Client";

    PrintWriter out;
    BufferedReader in;

    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public void sendMessage(int gatewayID, String message){
        if (out != null && !out.checkError()) {
            String output;
            /*
            String gatewayIDString;
            if(gatewayID > -1) {
                gatewayIDString = String.valueOf(gatewayID);
            } else {
                gatewayIDString = "";
            }

            if(message.startsWith("AT#")) {
                output = message;
            } else if(message.startsWith(".")) {
                output = "." + gatewayIDString + message.substring(1);
            } else {
                output = "." + gatewayIDString + message;
            }
            */
            output = message;

            Log.e(TAG, "to_server=" + output);
            out.println(output);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
        if(socket != null) {
            try {
                if(!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect(String server, int port) {
        Log.e(TAG, "server=" + server + "; port=" + port);
        mConnected = false;
        try {
            InetAddress serverAddr = InetAddress.getByName(server);
            Log.e(TAG, "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, port);
            this.serverAddr = server;
            this.port = port;
            mRun = true;
            mConnected = true;
            mMessageListener.messageReceived(Constants.MESSAGE_CONNECTED);
            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                Log.e(TAG, "C: Sent.");
                Log.e(TAG, "C: Done.");
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();
                    if (serverMessage != null && mMessageListener != null) {
                        if(serverMessage.equals(Constants.MESSAGE_IN_USE)) {
                            mConnected = false;
                            error = serverMessage;
                            break;
                        }
                        Log.e(TAG, "serverMessage=" + serverMessage);
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
                }
                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
            } catch (Exception e) {
                Log.e(TAG, "S: Error", e);
                error = e.getMessage();
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "C: Error", e);
            mConnected = false;
            error = e.getMessage();
        }
    }
    //Declare interface. Method messageReceived(String message) must be implemented
    //in MainActivity within AsynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    public boolean isConnected() {
        return mConnected;
    }

    public String getErrorMessage() {
        return error;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public int getPort() {
        return port;
    }
}