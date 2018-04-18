package com.thad.rfid_orderpick.Communications;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.thad.rfid_lib.Communications.BluetoothListener;
import com.thad.rfid_lib.Communications.CommThread;
import com.thad.rfid_lib.Decoder;
import com.thad.rfid_lib.Static.Prefs;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


/**
 * This Class handles the bluetooth connection with the mobile app.
 * Once it receives a byte stream, it sends it to Communication Handler
 */

//TO DO
//1. setDeviceToListen(); Handle failed BT search.


public class ServerBluetooth implements BluetoothListener{
    private static final String TAG = "|ServerBluetooth|";

    private CONN_STATES state;

    private CommunicationHandler mCommHandler;

    private BluetoothAdapter btAdapter;
    private BluetoothDevice mDevice;
    private BluetoothSocket btSocket;
    private BluetoothServerSocket btServerSocket;

    private String btAddress, deviceUUID;

    private ListenThread listenThread;
    private CommThread commThread;


    public ServerBluetooth(CommunicationHandler communicationHandler){
        mCommHandler = communicationHandler;

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        state = CONN_STATES.DISCONNECTED;
    }

    public void setAddress(String addrs, String uuid) {
        btAddress = addrs;
        deviceUUID = uuid;
    }


    public void listen() {
        if (listenThread != null) {
            Log.e(TAG, "A listen thread was already running. Cancelling...");
            listenThread.cancel();
            Log.d(TAG, "Canceled.");
        }
        listenThread = new ListenThread();
        listenThread.start();
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting threads.");
        if (listenThread != null) {
            Log.d(TAG, "Cancelling the listen thread...");
            listenThread.cancel();
            Log.d(TAG, "Canceled.");
            listenThread = null;
        }
        if (commThread != null) {
            Log.d(TAG, "Cancelling the communication thread...");
            commThread.cancel();
            Log.d(TAG, "Canceled.");
            commThread = null;
        }
    }

    public boolean isConnected(){return state == CONN_STATES.CONNECTED;}

    private void initiateCommunication(BluetoothSocket socket) {
        if (commThread != null) {
            Log.e(TAG, "A communication thread was already running. Cancelling...");
            commThread.cancel();
            Log.d(TAG, "Canceled.");
        }
        Log.d(TAG, "Initiating the Communication thread.");
        commThread = new CommThread(this, socket);
        commThread.start();
    }

    @Override
    public void onBytesReceived(byte[] data){
        Log.d(TAG, "Received a total of "+data.length+" bytes.");
        Decoder.MSG_TAG msgTag = Decoder.decodeMSGtag(data);
        Log.d(TAG, "Message Tag -> "+msgTag.toString());
        String msgString = Decoder.decodeMSGtoString(data);
        mCommHandler.onMessageReceived(msgTag, msgString);
    }

    public void sendMessage(Decoder.MSG_TAG msg_tag, String data){
        byte[] encodedMSG = Decoder.encodeMSG(msg_tag, data);
        Log.d(TAG, "Sending a total of "+encodedMSG.length+" bytes.");
        Log.d(TAG, "Message Tag -> "+msg_tag.toString());
        commThread.write(encodedMSG);
    }

    @Override
    public void onThreadConnected() {
        mCommHandler.print("On Thread Connected!");
        state = CONN_STATES.CONNECTED;
        mCommHandler.onConnected();
    }
    public void onThreadDisconnect() {
        mCommHandler.print("on Thread Disconnected!");
        state = CONN_STATES.DISCONNECTED;
        mCommHandler.onConnectionLost();
    }

    public BluetoothDevice getBluetoothDevice() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        Log.i(TAG, "Searching all " + pairedDevices.size() + " bluetooth devices.");
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().toUpperCase().equals(btAddress)) {
                Log.i(TAG, "Device found.");
                return device;
            }
        }
        return null;
    }

    private class ListenThread extends Thread {
        private boolean isRunning;

        public ListenThread() {
            try {
                btServerSocket = btAdapter.listenUsingRfcommWithServiceRecord(
                        Prefs.GLASS_UUID, UUID.fromString(Prefs.GLASS_UUID));
            } catch (IOException e) {
                Log.e(TAG, "Socket failed.");
                cancel();
            }
        }

        public void run() {
            isRunning = true;

            while (isRunning) {
                try {
                    Log.d(TAG, "Listening for devices..");
                    btSocket = btServerSocket.accept(2000);
                } catch (Exception e) {
                    Log.e(TAG, "Timed-out (2000).");
                }

                if (btSocket != null) {
                    Log.d(TAG, "Initiating Communications with accepted client.");
                    initiateCommunication(btSocket);
                    isRunning = false;
                }
            }
            Log.d(TAG, "Listen thread exits the loop.");
            cancelServerSocket();
        }

        void cancel() {
            Log.d(TAG, "Canceling listening thread.");
            isRunning = false;
            state = CONN_STATES.DISCONNECTED;
            cancelServerSocket();
            if(btSocket != null){
                try{
                    btSocket.close();
                    btSocket = null;
                    Log.d(TAG, "Closed BT Socket successfully.");
                } catch (Exception e){
                    Log.e(TAG, "Failed to close BT Socket.", e);
                }
            }
        }

        void cancelServerSocket(){
            if(btServerSocket != null){
                try{
                    btServerSocket.close();
                    btServerSocket = null;
                    Log.d(TAG, "Closed Server Socket successfully.");
                } catch (Exception e){
                    Log.e(TAG, "Failed to close Server Socket.", e);
                }
            }
        }
    }
}
