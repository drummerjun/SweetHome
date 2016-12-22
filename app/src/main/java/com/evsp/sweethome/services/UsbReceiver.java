package com.evsp.sweethome.services;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.evsp.sweethome.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

public class UsbReceiver extends BroadcastReceiver {
    private static final String TAG = UsbReceiver.class.getSimpleName();
    private static final String PERMISSION = "com.evsp.sweethome.USB_PERMISSION";
    private static final int TARGET_VENDOR = 1659;
    private static final int TARGET_PRODUCT = 8963;
    
    private static UsbDevice deviceFound;
    private static UsbInterface usbInterfaceFound;
    private static UsbEndpoint endPointIn;
    private static UsbEndpoint endPointOut;
    private static UsbDeviceConnection usbDeviceConnection;

    private static USBHost host;
    private static Thread listener;

    private static InputStream InStream;
    private static OutputStream OutStream;
    private static InputStreamReader InReader;
    private static BufferedReader BufReader;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, action);
        Toast.makeText(context, action, Toast.LENGTH_SHORT).show();

        if(action.equals(PERMISSION)) {
            synchronized (this) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if(device != null) {
                        Log.d(TAG, "device " + device);
                        connectUsb(context);
                    }
                } else {
                    Log.d(TAG, "permission denied device " + device);
                }
            }
        } else if(action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            synchronized (this) {
                Log.d(TAG, "attached");
                deviceFound = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                connectUsb(context);
            }
        } else if(action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if(device != null) {
                if(device.equals(deviceFound)) {
                    Log.d(TAG, "detached");
                    releaseUsb(context);
                }
            }
        } else if(action.equals(Constants.ACTION_USB_MESSAGE)) {
            if(host != null) {
                //host.sendMessageToUSB(intent.getStringExtra(SweetHome.MESSAGE));
                String outputString = intent.getStringExtra(Constants.MESSAGE);
                byte[] output = outputString.getBytes();
                if(OutStream == null) {
                    OutStream = host.getOutputStream();
                }
                try {
                    OutStream.write(output, 0, output.length);
                    //OutStream.write(1);
                } catch (IOException e) {
                    Log.w(TAG, e.getMessage());
                } catch (NullPointerException e) {
                    Log.w(TAG, "OutStream null");
                }
            }
        }
    }

    private void connectUsb(final Context context){
        usbInterfaceFound = null;
        endPointOut = null;
        endPointIn = null;
        Log.d(TAG, "connectUsb");

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Search device for targetVendorID and targetProductID
                if(deviceFound == null){
                    Log.d(TAG, "device init");
                    UsbManager manager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
                    HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    while (deviceIterator.hasNext()) {
                        UsbDevice device = deviceIterator.next();
                        Log.d(TAG, "device vendor=" + device.getVendorId() + ", product=" + device.getProductId());
                        if(device.getVendorId() == TARGET_VENDOR){
                            if(device.getProductId() == TARGET_PRODUCT){
                                deviceFound = device;
                                Log.d(TAG, "found");
                                break;
                            }
                        }
                    }
                }

                if(deviceFound != null) {
                    //Search for UsbInterface with Endpoint of USB_ENDPOINT_XFER_BULK,
                    //and direction USB_DIR_OUT and USB_DIR_IN
                    Log.d(TAG, deviceFound.getInterfaceCount() + " interfaces");
                    for(int i=0; i < deviceFound.getInterfaceCount(); i++){
                        UsbInterface usbif = deviceFound.getInterface(i);
                        if(usbif.getInterfaceClass() == UsbConstants.USB_CLASS_HID) {
                            Log.d(TAG, "HID interface");
                        } else {
                            Log.d(TAG, "interface=" + usbif.getInterfaceClass());
                        }
                        UsbEndpoint tOut = null;
                        UsbEndpoint tIn = null;

                        int tEndpointCnt = usbif.getEndpointCount();
                        Log.d(TAG, "endpoint count=" + tEndpointCnt);
                        if(tEndpointCnt >= 2) {
                            for(int j=0; j<tEndpointCnt; j++) {
                                if(usbif.getEndpoint(j).getType() ==
                                        UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                    if(usbif.getEndpoint(j).getDirection() ==
                                            UsbConstants.USB_DIR_OUT) {
                                        tOut = usbif.getEndpoint(j);
                                    } else if(usbif.getEndpoint(j).getDirection() ==
                                            UsbConstants.USB_DIR_IN) {
                                        tIn = usbif.getEndpoint(j);
                                    }
                                }
                            }
                            if(tOut!=null && tIn!=null){
                                //This interface have both USB_DIR_OUT
                                //and USB_DIR_IN of USB_ENDPOINT_XFER_BULK
                                Log.d(TAG, "IO endpoints found");
                                usbInterfaceFound = usbif;
                                endPointOut = tOut;
                                endPointIn = tIn;
                            }
                        }
                    }
                }
                if(usbInterfaceFound != null){
                    Log.d(TAG, "usbInterface found");
                    setupUsbComm(context);
                }
            }
        }).start();
    }

    private boolean setupUsbComm(final Context context){
        boolean success = false;

        final UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        boolean permitToRead = manager.hasPermission(deviceFound);
        Log.d(TAG, "permitToRead " + permitToRead);

        if(permitToRead) {
            synchronized (this) {
                usbDeviceConnection = manager.openDevice(deviceFound);
                Log.d(TAG, "usbDeviceConnection=" + usbDeviceConnection);
                if (usbDeviceConnection != null) {
                    SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Constants.USB_CONNECTION, true).apply();

                    Log.d(TAG, "init usb host");
                    host = new USBHost(usbDeviceConnection, deviceFound);
                    host.reset();
                    if (host.init(usbInterfaceFound, endPointIn, endPointOut)) {
                        if (!host.getLineCoding()) {
                            return false;
                        }
                        if (!host.setLineCoding()) {
                            return false;
                        }
                    } else {
                        return false;
                    }

                    InStream = host.getInputStream();
                    OutStream = host.getOutputStream();
                    // Construct Readers
                    InReader = new InputStreamReader(InStream);
                    BufReader = new BufferedReader(InReader);

                    Log.d(TAG, "init success, start listening");
                    listener = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String message = "";
                            while (!Thread.currentThread().isInterrupted()) {
                                try {
                                    Thread.sleep(50);
                                    message = onRead(4096);
                                } catch (IOException e) {
                                    Log.w(TAG, e.getMessage());
                                } catch (InterruptedException e) {
                                    Log.d(TAG, "interrupted");
                                    return;
                                } finally {
                                    Log.d(TAG, "usb read:" + message);
                                    if (!message.isEmpty()) {
                                        Intent localIntent = new Intent(Constants.ACTION_READ_MESSAGE);
                                        localIntent.putExtra(Constants.MESSAGE, message);
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                                    }
                                }
                            }

                            /*
                            synchronized (this) {
                                String message = "";
                                while (!Thread.currentThread().isInterrupted()) {
                                    try {
                                        Thread.sleep(1000);
                                        message = host.readMessageFromUSB();
                                    } catch (InterruptedException e) {
                                        Log.d(TAG, "interrupted");
                                        return;
                                    } catch (IOException e) {
                                        //e.printStackTrace();
                                    } finally {
                                        if (!message.isEmpty()) {
                                            Intent localIntent = new Intent(SweetHome.ACTION_READ_MESSAGE);
                                            localIntent.putExtra(SweetHome.MESSAGE, message);
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                                        }
                                    }
                                }
                            }
                            */
                        }
                    });
                    listener.start();
                }
            }
        } else {
            Log.d(TAG, "request permission");
            PendingIntent i = PendingIntent.getBroadcast(context, 0, new Intent(PERMISSION), 0);
            manager.requestPermission(deviceFound, i);
        }
        return success;
    }

    private void releaseUsb(Context context){
        Log.d(TAG, "host=" + host);
        Log.d(TAG, "releasing usb");
        if(usbDeviceConnection != null) {
            if(usbInterfaceFound != null){
                Log.d(TAG, "releasing interface");
                usbDeviceConnection.releaseInterface(usbInterfaceFound);
                usbInterfaceFound = null;
            }
            Log.d(TAG, "closing usb connection");
            usbDeviceConnection.close();
            usbDeviceConnection = null;
        }
        Log.d(TAG, "deleting endpoints");
        deviceFound = null;
        endPointIn = null;
        endPointOut = null;
        if(listener != null) {
            Log.d(TAG, "exiting thread");
            listener.interrupt();
        }
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.USB_CONNECTION, false).apply();
    }

    private synchronized String onRead(int len) throws IOException {
        char[] chararray = new char[len];
        String ret = "";
        int b = 0;
        //b = BufReader.read();
        b = BufReader.read(chararray, 0, len);
        if(b >= 0) {
            ret = String.copyValueOf(chararray).trim();
        }
        return ret;

        /*
        //Try to read num bytes or until a timeout occurs
        int pos = 0;
        long endTimeMillis = System.currentTimeMillis() + 1000;
        while((pos != len) && System.currentTimeMillis() < endTimeMillis) {
            int b = 0;
            b = BufReader.read(chararray, pos, 1);
            if(b == 1) {
                pos++;
            }
        }
        ret = String.copyValueOf(chararray).trim();
        return ret;
        */
    }
}
