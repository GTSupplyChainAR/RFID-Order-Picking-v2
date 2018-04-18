package com.thad.rfid_orderpick.Communications;

import android.util.Log;

import com.thad.rfid_lib.Decoder;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_orderpick.GlassClient;


/**
 * This Class handles the communications between Mobile App and glass.
 * It decodes the messages received on Bluetooth and populates the data structures.
 */

//TO DO
// Check for errors in Picking Data format

public class CommunicationHandler {
    private static final String TAG = "|CommunicationHandler|";

    private GlassClient mClient;

    private ServerBluetooth mServer;

    public CommunicationHandler(GlassClient client){
        mClient = client;
        mServer = new ServerBluetooth(this);
        mServer.setAddress(Prefs.PHONE_ADDRESS, Prefs.GLASS_UUID);

        mServer.listen();
    }

    public void shutdown(){
        mServer.disconnect();
    }

    public void sendTap(){mServer.sendMessage(Decoder.MSG_TAG.TAP, "");}


    public void onMessageReceived(Decoder.MSG_TAG msgTag, String msgString) {
        switch (msgTag){
            case SCAN:
                mClient.onNewScan(msgString);
                break;
            case START:
                Log.d(TAG, "Received order to start experiment!");
                mClient.startExperiment();
                break;
            case TRAIN:
                Log.d(TAG, "Received order to set data to Training!");
                mClient.setTraining();
                break;
            case TEST:
                Log.d(TAG, "Received order to set data to Testing!");
                mClient.setTesting();
                break;
            case STOP:
                Log.d(TAG, "Received order to stop experiment!");
                mClient.stopExperiment();
                break;
            case PAUSE:
                Log.d(TAG, "Received order to pause experiment.");
                mClient.pauseExperiment();
                break;
            case RESUME:
                Log.d(TAG, "Received order to resume experiment.");
                mClient.resumeExperiment();
                break;
        }
    }

    public void onConnectionLost(){
        mClient.onConnectionLost();
    }


    public void onConnected(){mClient.onConnected();}

    public void print(String msg){
        mClient.mLog(msg);
    }



}
