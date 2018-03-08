package com.thad.rfid_orderpick.Communications;

import android.util.Log;

import com.thad.rfid_lib.Data.PickingData;
import com.thad.rfid_lib.Data.PickingOrder;
import com.thad.rfid_lib.Data.WarehouseData;
import com.thad.rfid_lib.Decoder;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_orderpick.GlassClient;

import org.json.JSONException;


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
        mServer.setDeviceToListen(Prefs.PHONE_ADDRESS);

        mServer.listen();
    }

    public void shutdown(){
        mServer.stop();
    }


    public void onMessageReceived(Decoder.MSG_TAG msgTag, String msgString) {
        switch (msgTag){
            case PICKDATA:
                try {
                    PickingData pickingData = Decoder.decodePickingDataJSON(msgString);
                    Log.d(TAG, "Received Picking Data successfully.");
                    mClient.setPickingData(pickingData);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to decode Picking Data.");
                    e.printStackTrace();
                }
                break;
            case WAREHOUSE:
                try {
                    WarehouseData warehouseData = Decoder.decodeWarehouseDataJSON(msgString);
                    Log.d(TAG, "Received Warehouse Data successfully.");
                    mClient.setWarehouseData(warehouseData);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to decode Warehouse Data.");
                    e.printStackTrace();
                }
                break;
            case SCAN:
                mClient.onNewScan(msgString);
                break;
            case START:
                mClient.toggleExperiment();
                break;
            case STOP:
                mClient.toggleExperiment();
                break;
        }
    }


    public void print(String msg){
        mClient.mLog(msg);
    }



}
