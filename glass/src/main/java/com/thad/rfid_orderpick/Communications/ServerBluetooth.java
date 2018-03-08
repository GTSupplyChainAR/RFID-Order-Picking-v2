package com.thad.rfid_orderpick.Communications;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.thad.rfid_lib.Decoder;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_orderpick.Communications.CommunicationHandler;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


/**
 * This Class handles the bluetooth connection with the mobile app.
 * Once it receives a byte stream, it sends it to Communication Handler
 */

//TO DO
//1. setDeviceToListen(); Handle failed BT search.


public class ServerBluetooth {
    private static final String TAG = "|ServerBluetooth|";

    private CommunicationHandler mCommHandler;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice clientDevice;

    private ListenThread listenThread;
    private ServerCommThread commThread;


    public ServerBluetooth(CommunicationHandler communicationHandler){
        mCommHandler = communicationHandler;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    public void stop() {
        if (listenThread != null) {
            listenThread.cancel();
            listenThread = null;
        }
        if (commThread != null) {
            commThread.cancel();
            commThread = null;
        }
    }

    public void listen() {
        listenThread = new ListenThread();
        listenThread.start();
        mCommHandler.print("Awaiting connection...");
    }

    private void connect(BluetoothSocket socket) {
        commThread = new ServerCommThread(this, socket);
        mCommHandler.print("Connected!");
        commThread.start();
    }

    public void onBytesReceived(byte[] data){
        Log.d(TAG, "Received a total of "+data.length+" bytes.");
        Decoder.MSG_TAG msgTag = Decoder.decodeMSGTAG(data);
        Log.d(TAG, "Message Tag -> "+msgTag.toString());
        String msgString = Decoder.decodeMSGtoString(data);
        mCommHandler.onMessageReceived(msgTag, msgString);
    }



    public void setDeviceToListen(String mac_address) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        //int count = 0;
        for (BluetoothDevice device : pairedDevices) {
            //count++;
            //mMain.mLog(count+": "+device.getAddress().toUpperCase());
            if (device.getAddress().toUpperCase().equals(mac_address)) {
                mCommHandler.print("Phone found.");
                clientDevice = device;
                return;
            }
        }
        mCommHandler.print("Phone NOT found.");

        //TO DO
        //Allow the user to select the correct address through cards
    }

    private class ListenThread extends Thread {
        private boolean isRunning;
        private final BluetoothServerSocket serverSocket;

        public ListenThread() {
            BluetoothServerSocket socket = null;

            try {
                socket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        Prefs.GLASS_UUID, UUID.fromString(Prefs.GLASS_UUID));
                Log.d(TAG, "Socket created.");
            } catch (IOException e) {
                Log.e(TAG, "Socket failed.");
                e.printStackTrace();
            }

            Log.i(TAG, "Awaiting Connection...");
            serverSocket = socket;
        }

        public void run() {
            BluetoothSocket socket = null;
            isRunning = true;

            while (isRunning) {
                try {
                    Log.d(TAG, "Waiting to accept.");
                    socket = serverSocket.accept(2000);
                } catch (Exception e) {
                    Log.e(TAG, "Socket accept failed");
                }

                if (socket != null) {
                    Log.d(TAG, "Socket connected");
                    connect(socket);
                    return;
                }
            }
        }

        void cancel() {
            try {
                isRunning = false;
                serverSocket.close();
                Log.d(TAG, "Socket closed");
            } catch (IOException e) {
                Log.e(TAG, "Socket could not close.");
            }
        }
    }
}
