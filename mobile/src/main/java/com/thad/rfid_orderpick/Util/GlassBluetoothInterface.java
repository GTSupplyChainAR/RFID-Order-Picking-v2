package com.thad.rfid_orderpick.Util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


import com.thad.rfid_orderpick.MobileMainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;


public class GlassBluetoothInterface{
    private static final String TAG = "GlassBluetoothInterface";

    //Bytes used to encode the length of the rest of the message
    //Do not change without changing the same value on the Glass
    private static final int BYTE_HEADER_SIZE = 10;

    private enum STATES {CONNECTED, DISCONNECTED, CONNECTING}
    private STATES state;

    private static String GLASS_MAC_ADDRESS = "F8:8F:CA:12:E0:A3";//"f4:f5:e8:12:02:4d";

    private static final String GLASS_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice glassDevice;
    private static GlassBluetoothInterface glassInterface;

    private ConnectThread connectThread;
    private CommunicationThread communicationThread;

    private MobileMainActivity mMain;


    public GlassBluetoothInterface(MobileMainActivity activity) {
        Log.d(TAG, "Creating new GlassBluetoothInterface instance.");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mMain = activity;
        state = STATES.DISCONNECTED;
        //glassDevice = getGlassDevice();
    }

    private BluetoothDevice getGlassDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.d(TAG, "Searching all "+pairedDevices.size()+" bluetooth devices.");
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().toUpperCase().equals(GLASS_MAC_ADDRESS)) {
                Log.d(TAG, "Glass found.");
                return device;
            }
        }
        return null;
    }

    public void setGlassAddress(String addr){
        GLASS_MAC_ADDRESS = addr;
    }

    public void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (communicationThread != null) {
            communicationThread.cancel();
            communicationThread = null;
        }
        state = STATES.DISCONNECTED;
    }


    public boolean sendString(String msg) {
        if(communicationThread == null)
            return false;
        byte[] msg_bytes = msg.getBytes();
        int msg_length = msg_bytes.length;

        ByteBuffer dbuf = ByteBuffer.allocate(BYTE_HEADER_SIZE);
        dbuf.putInt(msg_length);
        byte[] msg_length_bytes = dbuf.array();

        byte[] encoded_msg = new byte[BYTE_HEADER_SIZE+msg_length];
        System.arraycopy(msg_length_bytes, 0, encoded_msg, 0, BYTE_HEADER_SIZE);
        System.arraycopy(msg_bytes, 0, encoded_msg, BYTE_HEADER_SIZE, msg_length);

        communicationThread.write(encoded_msg);

        return true;
    }


    public boolean isConnected(){
        return state == STATES.CONNECTED;
    }


    public void connect() {
        if (glassDevice == null){
            glassDevice = getGlassDevice();
            if(glassDevice == null)
                return;
        }

        Log.d(TAG, "Connecting with Glass.");
        connectThread = new ConnectThread();
        state = STATES.CONNECTING;
        connectThread.run();
    }

    private void initiateCommunication(BluetoothSocket socket) {
        communicationThread = new CommunicationThread(socket);
        Log.d(TAG, "Starting the connected thread.");
        communicationThread.start();
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket clientSocket;

        ConnectThread() {
            BluetoothSocket socket = null;
            try {
                socket = glassDevice.createRfcommSocketToServiceRecord(UUID.fromString(GLASS_UUID));
                Log.d(TAG, "Socket generated.");
            } catch (IOException e) {
                state = STATES.DISCONNECTED;
                Log.e(TAG, "Client Socket's create() method failed", e);
            }
            clientSocket = socket;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                clientSocket.connect();
                Log.d(TAG, "Mobile socket connected.");
            } catch (IOException connectException) {
                try {
                    clientSocket = (BluetoothSocket) glassDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(glassDevice, 1);
                    clientSocket.connect();
                    Log.e(TAG, "Client socket closed in run()", connectException);
                } catch (Exception e) {
                    Log.e(TAG, "Could not close the client socket", e);
                }
                state = STATES.DISCONNECTED;
            }
            // The connection attempt succeeded. Perform work associated with the connection in a separate thread.
            initiateCommunication(clientSocket);
        }

        // Closes the client socket and causes the thread to finish.
        void cancel() {
            try {
                clientSocket.close();
                Log.d(TAG, "Client socket closed");
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
            state = STATES.DISCONNECTED;
        }
    }

    private class CommunicationThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream connectedInputStream;
        private OutputStream connectedOutputStream;
        private byte[] buffer; // mmBuffer store for the stream

        CommunicationThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                Log.d(TAG, "Error occurred when creating client input stream");
            }
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "Error occurred when creating client output stream");
            }

            connectedOutputStream = outputStream;
            connectedInputStream = inputStream;

            if(connectedInputStream != null && connectedOutputStream != null) {
                Log.d(TAG, "I/O streams created.");
                state = STATES.CONNECTED;
            }else
                state = STATES.DISCONNECTED;

        }

        public void run() {
            buffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = connectedInputStream.read(buffer);

                    Log.d(TAG, "Just read "+numBytes+" bytes from the input stream.");
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected");
                    cancel();
                    state = STATES.DISCONNECTED;
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                connectedOutputStream.write(bytes);
                Log.d(TAG, "Just sent "+bytes.length+" bytes to the output stream.");
            } catch (IOException e) {
                Log.d(TAG, "Error occurred when sending data");
                state = STATES.DISCONNECTED;

            }
        }

        public void cancel() {
            try {
                connectedInputStream.close();
                connectedOutputStream.close();
                socket.close();
                Log.d(TAG, "Client closed");
            } catch (IOException e) {
                Log.d(TAG, "Failed to close the client");
            }
            state = STATES.DISCONNECTED;
        }
    }

}
