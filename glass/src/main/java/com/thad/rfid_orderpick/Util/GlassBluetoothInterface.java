package com.thad.rfid_orderpick.Util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.thad.rfid_orderpick.GlassMainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;


/**
 * This Class handles the bluetooth connection with the mobile app.
 * Once it receives a byte stream, it sends it to Communication Handler
 */

//TO DO
//1. getPhoneDevice(); Handle failed BT search.


public class GlassBluetoothInterface {
    private static final String TAG = "GlassBluetoothInterface";
    private static final int BYTE_HEADER_SIZE = 10;

    private static final String PHONE_ADDRESS = "3C:BB:FD:27:A0:1E";

    private static final String GLASS_UUID = "00001101-0000-1000-8000-00805f9b34fb";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice phoneDevice;

    private GlassMainActivity mMain;

    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;

    public GlassBluetoothInterface(GlassMainActivity activity) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mMain = activity;

        phoneDevice = getPhoneDevice();
    }

    private BluetoothDevice getPhoneDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        int count = 0;
        for (BluetoothDevice device : pairedDevices) {
            count++;
            //mMain.mLog(count+": "+device.getAddress().toUpperCase());
            if (device.getAddress().toUpperCase().equals(PHONE_ADDRESS)) {
                mMain.mLog("Phone found.");
                return device;
            }
        }
        mMain.mLog("Phone NOT found.");

        //TO DO
        //Allow the user to select the correct address through cards

        return null;
    }

    public void stop() {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
    }

    public void acceptConnection() {
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private void connect(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        AcceptThread() {
            BluetoothServerSocket socket = null;

            try {
                socket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(GLASS_UUID, UUID.fromString(GLASS_UUID));
                mMain.mLog("Socket created.");
            } catch (IOException e) {
                mMain.mLog("Socket failed.");
                e.printStackTrace();
            }

            serverSocket = socket;
        }

        public void run() {
            BluetoothSocket socket = null;

            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    mMain.mLog("Socket accept failed");
                }

                if (socket != null) {
                    mMain.mLog("Socket connected");
                    connect(socket);
                    return;
                }
            }
        }

        void cancel() {
            try {
                serverSocket.close();
                mMain.mLog("Socket closed");
            } catch (IOException e) {
                mMain.mLog("Socket could not close.");
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        //private final DataInputStream connectedInputStream;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;
        private byte[] buffer; // mmBuffer store for the stream

        ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                mMain.mLog("Error occurred when creating client input stream");
            }
            try {
                outputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                mMain.mLog("Error occurred when creating client output stream");
            }

            mMain.mLog("I/O Streams created.");
            connectedInputStream = inputStream;
            connectedOutputStream = outputStream;
        }

        public void run() {
            buffer = new byte[1024];
            int numBytes; // bytes returned from read()
            byte[] data = null;
            int current_bytes = 0;
            int total_bytes = 0;

            //*
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    while((numBytes = connectedInputStream.read(buffer)) != 1) {
                        Log.d(TAG, "New packet of "+numBytes +" bytes.");
                        if (data == null) {
                            byte[] header = new byte[BYTE_HEADER_SIZE];
                            System.arraycopy(buffer, 0, header, 0, BYTE_HEADER_SIZE);
                            ByteBuffer wrapped = ByteBuffer.wrap(header);
                            total_bytes = wrapped.getInt();
                            Log.d(TAG, "Header -> "+total_bytes +" bytes.");
                            current_bytes = numBytes - BYTE_HEADER_SIZE;
                            data = new byte[current_bytes];
                            System.arraycopy(buffer, BYTE_HEADER_SIZE, data, 0, current_bytes);
                        }else{
                            byte[] new_data = new byte[current_bytes + numBytes];
                            System.arraycopy(data, 0, new_data, 0, current_bytes);
                            System.arraycopy(buffer, 0, new_data, current_bytes, numBytes);
                            data = new_data;
                            current_bytes += numBytes;
                        }
                        Log.d(TAG, "Data -> "+data.length);

                        if(current_bytes >= total_bytes) {
                            break;
                        }
                    }
                    Log.d(TAG, "Received a total of "+total_bytes +" bytes.");

                    mMain.onBytesRecieved(data, total_bytes);
                    total_bytes = 0;
                    current_bytes = 0;
                    data = null;
                } catch (IOException e) {
                    mMain.mLog("Input stream was disconnected.");
                    try {
                        connectedInputStream.close();
                    } catch (IOException e1) {
                        mMain.mLog("Could not close the stream.");
                    }
                    break;
                }
            }
            //*/
        }

        public void write(byte[] bytes) {
            try {
                connectedOutputStream.write(bytes);
            } catch (IOException e) {
                mMain.mLog("Error occurred when sending data.");
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                connectedOutputStream.close();
                connectedInputStream.close();
                socket.close();
                mMain.mLog("Connection closed");
            } catch (IOException e) {
                mMain.mLog("Could not close connection");
            }
        }
    }
}
