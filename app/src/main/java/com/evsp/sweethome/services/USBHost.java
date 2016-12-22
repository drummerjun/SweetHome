package com.evsp.sweethome.services;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import com.google.common.base.Charsets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class USBHost {
    private static final String TAG = USBHost.class.getSimpleName();
    private static final int TIMEOUT = 1000;
    private static final int BAUDRATE = 19200;
    private static final int BUFFER_SIZE = 4096;
    private static final int REQ_SET_LINE_CODING = 0x20;
    private static final int REQ_GET_LINE_CODING = 0x21;
    private static final int REQ_SET_CONTROL_LINE_STATE = 0x22;
    private static final int WRITE = UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT;
    private static final int READ = UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_IN;

    private static final int SET_LINE_REQUEST_TYPE     = 0x21;
    private static final int SET_LINE_REQUEST          = 0x20;
    private static final int BREAK_REQUEST_TYPE        = 0x21;
    private static final int BREAK_REQUEST             = 0x23;
    private static final int BREAK_OFF                 = 0x0000;
    private static final int GET_LINE_REQUEST_TYPE     = 0xa1;
    private static final int GET_LINE_REQUEST          = 0x21;
    private static final int VENDOR_WRITE_REQUEST_TYPE = 0x40;
    private static final int VENDOR_WRITE_REQUEST      = 0x01;
    private static final int VENDOR_READ_REQUEST_TYPE  = 0xc0;
    private static final int VENDOR_READ_REQUEST       = 0x01;

    private UsbDeviceConnection connection;
    private UsbDevice device;
    private UsbEndpoint readEndpoint;
    private UsbEndpoint writeEndpoint;
    private int pl2303type = 0;

    public USBHost(UsbDeviceConnection connection, UsbDevice device) {
        this.connection = connection;
        this.device = device;
    }

    public boolean reset() {
        int result = connection.controlTransfer(
                UsbConstants.USB_DIR_OUT | UsbConstants.USB_TYPE_VENDOR,
                0x01 /* set request */,
                0x0000,
                0x0000,
                null,
                0,
                TIMEOUT);
        Log.d(TAG, "reset=" + result);
        return result >= 0;
    }

    public boolean init(UsbInterface usbif, UsbEndpoint epIn, UsbEndpoint epOut) {
        byte buf[] = new byte[4];
        if(epIn != null && epOut != null) {
            readEndpoint = epIn;
            writeEndpoint = epOut;
        } else {
            Log.d(TAG, "invalid endpoints");
            return false;
        }

        try {
            if(!connection.claimInterface(usbif, true)) {
                Log.d(TAG, "null interface");
                return false;
            }

            if(connection.getRawDescriptors()[7] == 64) {
                pl2303type = 1; //Type 1 = PL2303HX
            }

            if((connection.controlTransfer(VENDOR_READ_REQUEST_TYPE, VENDOR_READ_REQUEST, 0x8484, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(VENDOR_WRITE_REQUEST_TYPE, VENDOR_WRITE_REQUEST, 0x0404, 0, null, 0, TIMEOUT) < 0)
                    || (connection.controlTransfer(VENDOR_READ_REQUEST_TYPE, VENDOR_READ_REQUEST, 0x8484, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(VENDOR_READ_REQUEST_TYPE, VENDOR_READ_REQUEST, 0x8383, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(VENDOR_READ_REQUEST_TYPE, VENDOR_READ_REQUEST, 0x8484, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(VENDOR_WRITE_REQUEST_TYPE, VENDOR_WRITE_REQUEST, 0x0404, 1, null, 0, TIMEOUT) < 0)
                    || (connection.controlTransfer(VENDOR_READ_REQUEST_TYPE, VENDOR_READ_REQUEST, 0x8484, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(VENDOR_READ_REQUEST_TYPE, VENDOR_READ_REQUEST, 0x8383, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(VENDOR_WRITE_REQUEST_TYPE, VENDOR_WRITE_REQUEST, 0, 1, null, 0, TIMEOUT) < 0)
                    || (connection.controlTransfer(VENDOR_WRITE_REQUEST_TYPE, VENDOR_WRITE_REQUEST, 1, 0, null, 0, TIMEOUT) < 0)) {
                Log.d(TAG, "control transfer error");
                return false;
            }

            if (pl2303type == 1) {
                if(connection.controlTransfer(VENDOR_WRITE_REQUEST_TYPE, VENDOR_WRITE_REQUEST, 2, 0x44, null, 0, TIMEOUT) < 0) {
                    Log.d(TAG, "PL2303HX write error");
                    return false;
                }
            } else {
                if(connection.controlTransfer(VENDOR_WRITE_REQUEST_TYPE, VENDOR_WRITE_REQUEST, 2, 0x24, null, 0, TIMEOUT) < 0) {
                    Log.d(TAG, "PL2303 write error");
                    return false;
                }
            }

            /*
            if ((connection.controlTransfer(READ, 0x01, 0x8484, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(WRITE, 0x01, 0x0404, 0, null, 0, TIMEOUT) < 0)
                    || (connection.controlTransfer(READ, 0x01, 0x8484, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(READ, 0x01, 0x8383, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(READ, 0x01, 0x8484, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(WRITE, 0x01, 0x0404, 1, null, 0, TIMEOUT) < 0)
                    || (connection.controlTransfer(READ, 0x01, 0x8484, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(READ, 0x01, 0x8383, 0, buf, 1, TIMEOUT) < 0)
                    || (connection.controlTransfer(WRITE, 0x01, 0, 1, null, 0, TIMEOUT) < 0)
                    || (connection.controlTransfer(WRITE, 0x01, 1, 0, null, 0, TIMEOUT) < 0)
                    ) {
                Log.d(TAG, "io error");
                return false;
            }

            if((device.getDeviceClass() != 0x02) && (epOut.getMaxPacketSize() == 0x40)) {
                if (connection.controlTransfer(WRITE, 0x01, 2, 0x44, null, 0, TIMEOUT) < 0) {
                    Log.d(TAG, "PL2303HX write error");
                    return false;
                }
            } else {
                if (connection.controlTransfer(WRITE, 0x01, 2, 0x24, null, 0, TIMEOUT) < 0) {
                    Log.d(TAG, "PL2303 write error");
                    return false;
                }
            }

            if ((connection.controlTransfer(WRITE, 0x01, 8, 0, null, 0, TIMEOUT) < 0)
                    || (connection.controlTransfer(WRITE, 0x01, 9, 0, null, 0, TIMEOUT) < 0)
                    ) {
                Log.d(TAG, "write error");
                return false;
            }
            */
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "init success");
        return true;
    }

    public boolean getLineCoding() {
        /*
        byte response[] = new byte[7];
        if(connection.controlTransfer(0x21 | UsbConstants.USB_DIR_IN,
                REQ_GET_LINE_CODING, 0, 0, // bulk data interface number
                response, response.length, TIMEOUT) < 7) {
            Log.d(TAG, "GetLineCoding error");
            return false;
        }
        Log.d(TAG, "getLineCoding success");
        */
        return true;
    }

    public boolean setLineCoding() {
        int baudRate = BAUDRATE;
        byte stopBitsByte = 1;
        byte parityBitesByte = 0;
        byte dataBits = 8;

        if(baudRate > 1228800 && pl2303type == 0) {
            baudRate = 1228800;
        }

        byte[] req = {
                (byte) (baudRate & 0xff),
                (byte) ((baudRate >> 8) & 0xff),
                (byte) ((baudRate >> 16) & 0xff),
                (byte) ((baudRate >> 24) & 0xff),
                stopBitsByte,
                parityBitesByte,
                dataBits
        };

        /*
        if (connection.controlTransfer(0x21 | UsbConstants.USB_DIR_OUT,
                REQ_SET_LINE_CODING, 0, 0, // bulk data interface number
                req, req.length, TIMEOUT) < 0) {
            Log.d(TAG, "SetLineCoding error");
            return false;
        }
		// CRTSCTS=off
        if (connection.controlTransfer(
                UsbConstants.USB_DIR_OUT | UsbConstants.USB_TYPE_VENDOR,
                0x01, 0, 0, null, 0, TIMEOUT) < 0) {
            Log.d(TAG, "other error");
            return false;
        }
        Log.d(TAG, "setLineCoding success");
        */

        // Set new configuration on PL2303 only if settings have changed
        if(connection.controlTransfer(SET_LINE_REQUEST_TYPE,
                SET_LINE_REQUEST, 0, 0, req, req.length, TIMEOUT) < 0 ) {
            Log.d(TAG, "SetLineCoding error");
            return false;
        }

        // Disable BreakControl
        if(connection.controlTransfer(BREAK_REQUEST_TYPE,
                BREAK_REQUEST, BREAK_OFF, 0, null, 0, TIMEOUT) < 0) {
            Log.d(TAG, "disable BreakControl error");
        }

        // Disable FlowControl
        if(connection.controlTransfer(VENDOR_WRITE_REQUEST_TYPE,
                VENDOR_WRITE_REQUEST, 0, 0, null, 0, TIMEOUT) < 0) {
            Log.d(TAG, "disable FlowControl error");
        }
        return true;
    }

    public int sendMessageToUSB(String message) {
        synchronized (this) {
            int usbResult = -1;
            byte[] bytesOut = message.getBytes(Charsets.UTF_8);
            Log.e(TAG, "toUSB: " + message + " (" + bytesOut.length + ")");
            usbResult = connection.bulkTransfer(writeEndpoint, bytesOut, bytesOut.length, TIMEOUT);
            Log.e(TAG, "send result=" + usbResult);
            return usbResult;
        }
    }

    public String readMessageFromUSB() throws IOException {
        synchronized (this) {
            byte[] buffer = new byte[readEndpoint.getMaxPacketSize()];
            int byteCount = connection.bulkTransfer(readEndpoint, buffer, buffer.length, TIMEOUT);
            if (byteCount < 0) {
                Log.e(TAG, "read fail");
                throw new IOException();
            }
            String message = new String(buffer, Charsets.UTF_8).trim();
            Log.e(TAG, "fromUSB: " + message);
            return message;
        }
    }

    // create InputStream
    public InputStream getInputStream() {
        InputStream in = new InputStream() {
            // Blocking read
            @Override
            public int read() throws IOException {
                synchronized (this) {
                    Log.e(TAG, "blocking read");
                    int retVal= -1;
                    if(connection == null) {
                        throw new IOException("Connection closed");
                    }
                    if((readEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK)
                            && (readEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_INT)) {
                        throw new IOException("Not an Interrupt or Bulk-Endpoint");
                    }
                    if(readEndpoint.getDirection() != UsbConstants.USB_DIR_IN) {
                        throw new IOException("Not an Input-Endpoint");
                    }
                    Log.e(TAG, "sending UsbRequest");
                    ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    UsbRequest request = new UsbRequest();
                    if(!request.initialize(connection, readEndpoint)) {
                        throw new IOException("ReadRequest.initailize() failed");
                    }
                    if(!request.queue(readBuffer, 1)) {
                        throw new IOException("ReadRequest.queue() failed");
                    }
                    Log.e(TAG, "retrieving feedback");
                    UsbRequest retRequest = connection.requestWait();
                    if(retRequest == null) {
                        throw new IOException("ReadRequest failed");
                    }
                    if(retRequest == request) {
                        retVal = readBuffer.get();
                    }
                    Log.e(TAG, "ret=" + retVal);
                    return retVal;
                }
            }

            // Non-blocking read
            @Override
            public int read(byte[] buffer, int offset, int length) throws IOException, IndexOutOfBoundsException {
                synchronized (this) {
                    Log.e(TAG, "non-blocking read");
                    int len = 0;
                    if((offset < 0) || (length < 0) || ((offset + length) > buffer.length)) {
                        throw new IndexOutOfBoundsException();
                    }
                    if(connection == null) {
                        throw new IOException("Connection closed");
                    }
                    if(readEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        throw new IOException("Not a Bulk-Endpoint");
                    }
                    if(readEndpoint.getDirection() != UsbConstants.USB_DIR_IN) {
                        throw new IOException("Not an Input-Endpoint");
                    }
                    byte [] readBuffer = new byte[length];
                    len = connection.bulkTransfer(readEndpoint, readBuffer, length, TIMEOUT);
                    Log.e(TAG, "read bulkTransfer result=" + len);
                    if(len >= 0) {
                        System.arraycopy(readBuffer, 0, buffer, offset, len);
                    } else {
                        len = 0;
                    }
                    return len;
                }
            }
        };
        return in;
    }

    // create OutputStream
    public OutputStream getOutputStream() {
        OutputStream out = new OutputStream() {

            // Blocking write
            @Override
            public void write(int oneByte) throws IOException{
                synchronized (this) {
                    Log.w(TAG, "blocking write");
                    if(connection == null) {
                        throw new IOException("Connection closed");
                    }
                    if((writeEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK)
                            && (writeEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_INT)) {
                        throw new IOException("Not an Interrupt or Bulk-Endpoint");
                    }
                    if(writeEndpoint.getDirection() != UsbConstants.USB_DIR_OUT) {
                        throw new IOException("Not an Output-Endpoint");
                    }
                    Log.w(TAG, "submit UsbRequest");
                    ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    UsbRequest request = new UsbRequest();
                    if(!request.initialize(connection, writeEndpoint)) {
                        throw new IOException("WriteRequest.initailize() failed");
                    }
                    if(!request.queue(writeBuffer, 1)) {
                        throw new IOException("WriteRequest.queue() failed");
                    }
                    Log.w(TAG, "get return Request");
                    UsbRequest retRequest = connection.requestWait();
                    if(retRequest == null) {
                        throw new IOException("WriteRequest failed");
                    }
                    Log.w(TAG, "sent");
                }
            }

            // Non-blocking write
            @Override
            public void write (byte[] buffer, int offset, int count) throws IOException, IndexOutOfBoundsException {
                synchronized (this) {
                    Log.w(TAG, "non-blocking write");
                    if((offset < 0) || (count < 0) || ((offset + count) > buffer.length)) {
                        throw new IndexOutOfBoundsException();
                    }
                    if(connection == null) {
                        throw new IOException("Connection closed");
                    }
                    if(writeEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        throw new IOException("Not a Bulk-Endpoint");
                    }
                    if(writeEndpoint.getDirection() != UsbConstants.USB_DIR_OUT) {
                        throw new IOException("Not an Output-Endpoint");
                    }
                    byte [] writeBuffer = new byte[count];
                    System.arraycopy(buffer, offset, writeBuffer, 0, count);
                    int len = connection.bulkTransfer(writeEndpoint, writeBuffer, count, TIMEOUT);
                    Log.w(TAG, "send bulkTransfer ret=" + len + "==count=" + count);
                    if(len != count) {
                        throw new IOException ("BulkWrite failed - len="+ len +";count=" + count);
                    }
                }
            }
        };
        return out;
    }
}