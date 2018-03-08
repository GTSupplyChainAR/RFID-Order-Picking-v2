package com.thad.rfid_orderpick.Communications;

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

public class ServerCommThread {
    private static final String TAG = "|ServerCommThread|";

    private ServerBluetooth server;

    private final BluetoothSocket socket;
    private InputStream connectedInputStream;
    private OutputStream connectedOutputStream;

    private byte[] buffer; // mmBuffer store for the stream

    public ServerCommThread(ServerBluetooth server, BluetoothSocket socket) {
        this.server = server;
        this.socket = socket;
    }

    public void start(){
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            Log.d(TAG, "I/O Streams created.");
            Log.d(TAG, "You are connected!");
            connectedInputStream = inputStream;
            connectedOutputStream = outputStream;
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating server streams.");
        }
        run();
    }

    public void run() {
        buffer = new byte[1024];
        int numBytes; // bytes returned from read()
        byte[] data = null;
        int current_bytes = 0;
        int total_bytes = 0;

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                while((numBytes = connectedInputStream.read(buffer)) != 1) {
                    //Log.d(TAG, "New packet of "+numBytes +" bytes.");
                    if (data == null) {
                        byte[] header = new byte[Decoder.HEADER_MSG_LENGTH_SIZE];
                        System.arraycopy(buffer, 0, header, 0, Decoder.HEADER_MSG_LENGTH_SIZE);
                        ByteBuffer wrapped = ByteBuffer.wrap(header);
                        total_bytes = wrapped.getInt();
                        Log.d(TAG, "Header -> "+total_bytes +" bytes.");
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
                    //Log.d(TAG, "Data -> "+data.length);

                    if(current_bytes >= total_bytes) {
                        break;
                    }
                }
                Log.d(TAG, "Received a total of "+total_bytes +" bytes.");

                server.onBytesReceived(data);
                total_bytes = 0;
                current_bytes = 0;
                data = null;
            } catch (IOException e) {
                Log.e(TAG, "Input stream was disconnected.");
                try {
                    connectedInputStream.close();
                } catch (IOException e1) {
                    Log.e(TAG, "Could not close the stream.");
                }
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            connectedOutputStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data.");
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            connectedOutputStream.close();
            connectedInputStream.close();
            socket.close();
            Log.d(TAG, "Connection closed");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not close connection");
        }
    }

}
