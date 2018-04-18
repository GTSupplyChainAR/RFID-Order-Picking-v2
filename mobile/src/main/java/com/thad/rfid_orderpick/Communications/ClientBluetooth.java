package com.thad.rfid_orderpick.Communications;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.thad.rfid_lib.Communications.BluetoothListener;
import com.thad.rfid_lib.Communications.CommThread;
import com.thad.rfid_lib.Decoder;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


public class ClientBluetooth implements BluetoothListener {
    private static final String TAG = "|ClientBluetooth|";

    private CommunicationHandler mCommHandler;

    private CONN_STATES state;

    private BluetoothAdapter btAdapter;
    private BluetoothDevice mDevice;
    private BluetoothSocket btSocket;

    private String btAddress, deviceUUID;

    private ConnectThread connectThread;
    private CommThread commThread;


    public ClientBluetooth(CommunicationHandler commHandler) {
        Log.i(TAG, "Creating new ClientBluetooth instance.");
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mCommHandler = commHandler;
        state = CONN_STATES.DISCONNECTED;
    }

    public void setAddress(String addrs, String uuid) {
        btAddress = addrs;
        deviceUUID = uuid;
    }


    //LIFECYCLE
    public void connect() {
        mDevice = getBluetoothDevice();
        Log.d(TAG, "Attempting to connect via bluetooth.");
        if (mDevice == null) {
            Log.e(TAG, "Failed to find bluetooth device.");
            return;
        }
        if (connectThread != null) {
            Log.e(TAG, "A connect thread was already running. Cancelling...");
            connectThread.cancel();
            Log.d(TAG, "Canceled.");
        }
        connectThread = new ConnectThread();
        connectThread.start();
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting threads.");
        if (connectThread != null) {
            Log.d(TAG, "Cancelling the connect thread...");
            connectThread.cancel();
            Log.d(TAG, "Canceled.");
            connectThread = null;
        }
        if (commThread != null) {
            Log.d(TAG, "Cancelling the communication thread...");
            commThread.cancel();
            Log.d(TAG, "Canceled.");
            commThread = null;
        }
    }

    public boolean isConnected() {
        return state == CONN_STATES.CONNECTED;
    }
    //END OF LIFECYCLE


    //ACTIONS
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

    private BluetoothDevice getBluetoothDevice() {
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

    //END OF ACTIONS

    public void sendMessage(Decoder.MSG_TAG msg_tag, String data) {
        if (!isConnected())
            return;
        byte[] encodedMSG = Decoder.encodeMSG(msg_tag, data);
        Log.d(TAG, "Sending a total of " + encodedMSG.length + " bytes.");
        Log.d(TAG, "Message Tag -> " + msg_tag.toString());
        commThread.write(encodedMSG);
    }

    @Override
    public void onBytesReceived(byte[] data) {
        Log.d(TAG, "Received a total of " + data.length + " bytes.");
        Decoder.MSG_TAG msgTag = Decoder.decodeMSGtag(data);
        Log.d(TAG, "Message Tag -> " + msgTag.toString());
        String msgString = Decoder.decodeMSGtoString(data);
        mCommHandler.onMessageReceived(msgTag, msgString);
    }


    @Override
    public void onThreadConnected() {
        Log.i(TAG, "Communication Thread has connected.");
        state = CONN_STATES.CONNECTED;
    }
    public void onThreadDisconnect() {
        Log.i(TAG, "Communication Thread has disconnected.");
        state = CONN_STATES.DISCONNECTED;
    }


    //THREADS

    private class ConnectThread extends Thread {

        public ConnectThread() {
            try {
                btSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(deviceUUID));
                Log.i(TAG, "Client Socket generated.");
            } catch (IOException e) {
                Log.e(TAG, "Failed to create the client socket.", e);
            }
        }

        public void run() {
            btAdapter.cancelDiscovery();

            try {
                Log.d(TAG, "Attempting to connect with Server.");
                btSocket.connect();
                Log.d(TAG, "Connected. Initiating Communications");
                initiateCommunication(btSocket);
            } catch (Exception e) {
                Log.e(TAG, "Failed to connect with Server.", e);
                cancel();
            }
        }

        // Closes the client socket and causes the thread to finish.
        void cancel() {
            Log.d(TAG, "Canceling connected thread.");
            state = CONN_STATES.DISCONNECTED;
            if(btSocket != null){
                try{
                    btSocket.close();
                    btSocket = null;
                    Log.d(TAG, "Closed Client Socket successfully.");
                } catch (Exception e){
                    Log.e(TAG, "Failed to close Client Socket.", e);
                }
            }
        }
    }
}
