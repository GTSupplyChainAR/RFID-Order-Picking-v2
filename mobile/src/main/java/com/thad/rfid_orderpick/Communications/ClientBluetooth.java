package com.thad.rfid_orderpick.Communications;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.thad.rfid_lib.Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class ClientBluetooth {
    private static final String TAG = "|ClientBluetooth|";


    private enum CONN_STATES {CONNECTED, DISCONNECTED, CONNECTING}
    private CONN_STATES state;



    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mDevice;
    private String bluetoothAddress, deviceUUID;

    private ConnectThread connectThread;
    private CommunicationThread communicationThread;

    private CommunicationHandler mCommHandler;


    public ClientBluetooth(CommunicationHandler commHandler) {
        Log.i(TAG, "Creating new ClientBluetooth instance.");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mCommHandler = commHandler;
        state = CONN_STATES.DISCONNECTED;
        //glassDevice = getGlassDevice();
    }

    public void setAddress(String addrs, String uuid){
        bluetoothAddress = addrs;
        deviceUUID = uuid;
    }


    //LIFECYCLE
    public void connect() {
        mDevice = getBluetoothDevice();
        if(mDevice == null)
           return;

        Log.i(TAG, "Connecting with Device.");
        connectThread = new ConnectThread();
        state = CONN_STATES.CONNECTING;
        connectThread.start();
    }

    public void disconnect() {
        Log.i(TAG, "Disconnecting.");
        if (connectThread != null)
            connectThread.cancel();

        if (communicationThread != null)
            communicationThread.cancel();

        connectThread = null;
        communicationThread = null;
        state = CONN_STATES.DISCONNECTED;
    }

    public boolean isConnected(){
        return state == CONN_STATES.CONNECTED;
    }
    //END OF LIFECYCLE


    //ACTIONS
    private void initiateCommunication(BluetoothSocket socket) {
        communicationThread = new CommunicationThread(socket);
        Log.d(TAG, "Starting the connected thread.");
        communicationThread.start();
    }

    private BluetoothDevice getBluetoothDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.i(TAG, "Searching all "+pairedDevices.size()+" bluetooth devices.");
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().toUpperCase().equals(bluetoothAddress)) {
                Log.i(TAG, "Device found.");
                return device;
            }
        }
        return null;
    }

    public void sendMessage(Decoder.MSG_TAG msg_tag, String data){
        if(!isConnected())
            return;
        byte[] encodedMSG = Decoder.encodeMSG(msg_tag, data);
        Log.d(TAG, "Sending a total of "+encodedMSG.length+" bytes.");
        Log.d(TAG, "Message Tag -> "+msg_tag.toString());
        communicationThread.write(encodedMSG);
    }

    //END OF ACTIONS



    //THREADS

    private class ConnectThread extends Thread {
        private BluetoothSocket clientSocket;

        ConnectThread() {
            BluetoothSocket socket = null;
            try {
                socket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(deviceUUID));
                Log.i(TAG, "Socket generated.");
            } catch (IOException e) {
                state = CONN_STATES.DISCONNECTED;
                Log.e(TAG, "Client Socket's create() method failed", e);
            }
            clientSocket = socket;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                Log.d(TAG, "Attempting to connect.");
                clientSocket.connect();
            } catch (IOException connectException) {
                try {
                    Log.e(TAG, "First attempt to connect failed.");
                    clientSocket = (BluetoothSocket) mDevice.getClass().getMethod(
                                "createRfcommSocket", new Class[]{int.class}).invoke(mDevice, 1);
                    clientSocket.connect();
                    Log.i(TAG, "Second succeeded.");
                } catch (Exception e) {
                    Log.e(TAG, "Second failed as well. Disconnecting.", e);
                }
                state = CONN_STATES.DISCONNECTED;
            }
            // The connection attempt succeeded. Perform work associated with the connection in a separate thread.
            Log.d(TAG, "Mobile socket connected.");
            initiateCommunication(clientSocket);
        }

        // Closes the client socket and causes the thread to finish.
        void cancel() {
            try {
                clientSocket.close();
                Log.i(TAG, "Client socket closed.");
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket.", e);
            }
            state = CONN_STATES.DISCONNECTED;
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
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating client streams");
            }

            connectedOutputStream = outputStream;
            connectedInputStream = inputStream;

            if(connectedInputStream != null && connectedOutputStream != null) {
                Log.i(TAG, "I/O streams created.");
                state = CONN_STATES.CONNECTED;
            }else
                state = CONN_STATES.DISCONNECTED;
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
                    state = CONN_STATES.DISCONNECTED;
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
                state = CONN_STATES.DISCONNECTED;
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
            state = CONN_STATES.DISCONNECTED;
        }
    }

    //END OF THREADS
}
