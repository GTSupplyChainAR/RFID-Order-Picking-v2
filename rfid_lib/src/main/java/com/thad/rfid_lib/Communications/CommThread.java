package com.thad.rfid_lib.Communications;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.thad.rfid_lib.Decoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by theo on 3/5/18.
 */

public class CommThread extends Thread{
    private static final String TAG = "|ServerCommThread|";

    private BluetoothListener btListener;

    private BluetoothSocket socket;
    private InputStream connectedInputStream;
    private OutputStream connectedOutputStream;

    private boolean isRunning;

    private byte[] buffer; // mmBuffer store for the stream

    public CommThread(BluetoothListener btListener, BluetoothSocket socket) {
        this.btListener = btListener;
        this.socket = socket;
        init();
    }

    public void init(){
        try {
            connectedInputStream = new DataInputStream(socket.getInputStream());
            connectedOutputStream = new DataOutputStream(socket.getOutputStream());
            Log.d(TAG, "I/O Streams created.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to create the I/O streams.", e);
            cancel();
        }
    }

    public void run() {
        if(connectedInputStream == null)
            return;

        buffer = new byte[1024];
        int numBytes; // bytes returned from read()
        isRunning = true;
        btListener.onThreadConnected();

        Log.d(TAG, "Communication Thread is running...");

        // Keep listening to the InputStream until an exception occurs.
        while (isRunning) {
            try {
                while((numBytes = connectedInputStream.read(buffer)) != 1) {
                    int pointer = 0;
                    while(pointer < numBytes){
                        byte[] remainingBytes = new byte[numBytes-pointer];
                        System.arraycopy(buffer, pointer, remainingBytes, 0, numBytes-pointer);

                        int msgLength = Decoder.decodeMSGlength(remainingBytes);
                        byte[] msgData = new byte[msgLength];
                        System.arraycopy(remainingBytes, Decoder.HEADER_MSG_LENGTH_SIZE, msgData, 0, msgLength);

                        Log.d(TAG, "Decoding ["+pointer+" - "+(remainingBytes.length+pointer)+"] of "+numBytes+": Header:"+Decoder.HEADER_MSG_LENGTH_SIZE +"-Data:"+msgLength);
                        pointer += msgLength + Decoder.HEADER_MSG_LENGTH_SIZE;

                        btListener.onBytesReceived(msgData);
                    }
                    Log.d(TAG, "Full message decoded. "+numBytes);
                }
            } catch (IOException e) {
                Log.e(TAG, "Input stream was disconnected.");
                isRunning = false;
                cancel();
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            connectedOutputStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Failed to send data.");
            cancel();
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        Log.d(TAG, "Canceling communications thread.");
        isRunning = false;
        if(connectedInputStream != null){
            try{
                connectedInputStream.close();
                connectedInputStream = null;
                Log.d(TAG, "Closed Input Stream successfully.");
            }catch (IOException e){
                Log.e(TAG, "Failed to close Input Stream.", e);
            }
        }
        if(connectedOutputStream != null){
            try{
                connectedOutputStream.close();
                connectedOutputStream = null;
                Log.d(TAG, "Closed Output Stream successfully.");
            }catch (IOException e){
                Log.e(TAG, "Failed to close Output Stream.", e);
            }
        }
        if(socket != null){
            try {
                socket.close();
                socket = null;
                Log.d(TAG, "Closed Client Socket successfully.");
            } catch (IOException e) {
                Log.e(TAG, "Failed to close Client Socket.", e);
            }
        }
        btListener.onThreadDisconnect();
    }



    /*
    public void run() {
        if(connectedInputStream == null)
            return;

        buffer = new byte[1024];
        int numBytes; // bytes returned from read()
        byte[] data = null;
        int current_bytes = 0;
        int total_bytes = 0;
        isRunning = true;
        btListener.onThreadConnected();

        Log.d(TAG, "Communication Thread is running...");

        // Keep listening to the InputStream until an exception occurs.
        while (isRunning) {
            try {
                while((numBytes = connectedInputStream.read(buffer)) != 1) {
                    //Log.d(TAG, "New packet of "+numBytes +" bytes.");
                    if (data == null) {
                        total_bytes = Decoder.decodeMSGlength(buffer);
                        current_bytes = numBytes - Decoder.HEADER_MSG_LENGTH_SIZE;
                        data = new byte[current_bytes];
                        System.arraycopy(buffer, Decoder.HEADER_MSG_LENGTH_SIZE, data, 0, current_bytes);
                    }else{
                        byte[] new_data = new byte[current_bytes + numBytes];
                        System.arraycopy(data, 0, new_data, 0, current_bytes);
                        System.arraycopy(buffer, 0, new_data, current_bytes, numBytes);
                        data = new_data;
                        current_bytes += numBytes;
                    }

                    if(current_bytes >= total_bytes) {
                        break;
                    }
                }
                Log.d(TAG, "Received a total of "+total_bytes +" bytes.");

                btListener.onBytesReceived(data);
                total_bytes = 0;
                current_bytes = 0;
                data = null;
            } catch (IOException e) {
                Log.e(TAG, "Input stream was disconnected.");
                isRunning = false;
                cancel();
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            connectedOutputStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Failed to send data.");
            cancel();
        }
    }*/

}
