package com.thad.rfid_orderpick.Communications;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.thad.rfid_lib.Decoder;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_orderpick.MobileClient;

/**
 * Created by theo on 2/22/18.
 */

public class CommunicationHandler {
    //CONSTANTS
    private static final String TAG = "|CommHandler|";
    private static final String PREFS_NAME = "PREFS";
    private static final String[] PREFS_TAGS =
            new String[]{"glass_bluetooth_addrs", "xband_bluetooth_addrs1", "xband_bluetooth_addrs2"};
    //END OF CONSTANTS


    //VARIABLES
    private MobileClient mClient;

    private XBandInterface[] mXBands;
    private ClientBluetooth mGlassBT;

    private String glass_bluetooth_addrs;
    private String[] xband_bluetooth_addrs;

    private boolean[] connState = new boolean[]{false, false, false};
    //END OF VARIABLES

    //CONSTRUCTOR
    public CommunicationHandler(MobileClient client) {
        mClient = client;
        init();
    }

    private void init() {
        mGlassBT = new ClientBluetooth(this);
        mXBands = new XBandInterface[2];
        for (int i = 0; i < 2; i++)
            mXBands[i] = new XBandInterface(this);

        setup();
    }

    //COMMANDS
    public void setup() {
        //Load Addresses from Application Settings
        SharedPreferences settings = mClient.getContext().getSharedPreferences(PREFS_NAME, 0);
        glass_bluetooth_addrs = settings.getString(PREFS_TAGS[0], Prefs.GLASS_ADDRESS);
        xband_bluetooth_addrs = new String[2];
        xband_bluetooth_addrs[0] = settings.getString(PREFS_TAGS[1], Prefs.XBAND1_ADDRESS);
        xband_bluetooth_addrs[1] = settings.getString(PREFS_TAGS[2], Prefs.XBAND2_ADDRESS);

        //Set the bt addresses on the bluetooth handlers
        mGlassBT.setAddress(glass_bluetooth_addrs, Prefs.GLASS_UUID);
        for (int i = 0; i < 2; i++)
            mXBands[i].setAddress(xband_bluetooth_addrs[i]);
    }

    public void editAddress(int device_index, String new_address) {
        SharedPreferences settings = mClient.getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String pref_name = PREFS_TAGS[device_index];
        editor.putString(pref_name, new_address);
        editor.commit();
    }

    public void connect() {
        Log.d(TAG, "Attempting to connect.");

        if (!mGlassBT.isConnected())
            mGlassBT.connect();

        for (int i = 0; i < 2; i++) {
            if(!mXBands[i].isConnected())
                mXBands[i].connect();
        }
    }
    public void disconnect() {
        Log.d(TAG, "Disconnecting.");
        mGlassBT.disconnect();
        for (int i = 0; i < 2; i++) {
                mXBands[i].disconnect();
        }
    }

    //END OF COMMANDS

    public void sendScan(String scan){
        mGlassBT.sendMessage(Decoder.MSG_TAG.SCAN, scan);
    }
    public void startExperiment(){
        mGlassBT.sendMessage(Decoder.MSG_TAG.START, "");
    }
    public void setTraining(){mGlassBT.sendMessage(Decoder.MSG_TAG.TRAIN, "");}
    public void setTesting(){mGlassBT.sendMessage(Decoder.MSG_TAG.TEST, "");}
    public void stopExperiment(){
        mGlassBT.sendMessage(Decoder.MSG_TAG.STOP, "");
    }
    public void pauseExperiment(){ mGlassBT.sendMessage(Decoder.MSG_TAG.PAUSE, "");}
    public void resumeExperiment(){ mGlassBT.sendMessage(Decoder.MSG_TAG.RESUME, "");}

    //EVENT LISTENERS
    public void onNewRFIDScan(String scan, int strength) {
        mClient.onNewRFIDScan(scan, strength);
    }

    public void onXBandBatteryUpdate(int xband_index, double battery_level) {
        mClient.onBatteryUpdate(xband_index + 1, battery_level);
    }
    //END OF LISTENERS

    public boolean isConnected(boolean[] states){
        return states[0] && states[1] && states[2];
    }

    //GETTERS
    public Context getContext() {
        return mClient.getContext();
    }

    public String[] getAddresses() {
        return new String[]{glass_bluetooth_addrs,
                xband_bluetooth_addrs[0], xband_bluetooth_addrs[1]};
    }

    public boolean[] getConnStatus() {
        boolean[] new_connStates = new boolean[]{mGlassBT.isConnected(),
                mXBands[0].isConnected(), mXBands[1].isConnected()};
        for(int i = 0 ; i < new_connStates.length ; i++){
            if(connState[i] == new_connStates[i])
                continue;
            mClient.deviceConnUpdate(i, new_connStates[i]);
        }

        if(isConnected(connState) && !isConnected(new_connStates) && mClient.isExperimentActive()) {
            Log.d(TAG, "Pausing the experiment due to disconnection.");
            mClient.pauseExperiment();
        } else if(!isConnected(connState) && isConnected(new_connStates) && mClient.isExperimentPaused()) {
            Log.d(TAG, "Resuming the experiment after re-connecting.");
            mClient.resumeExperiment();
        }

        connState = new_connStates;
        return connState;
    }


    public void onMessageReceived(Decoder.MSG_TAG msgTag, String msgString) {
        switch (msgTag){
            case TAP:
                mClient.onGlassTapped();
                break;
            case STOP:
                mClient.stopExperiment();
                break;
        }
    }
    //END OF GETTERS
}
