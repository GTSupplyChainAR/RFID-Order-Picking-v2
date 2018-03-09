package com.thad.rfid_orderpick;

import android.util.Log;

import com.thad.rfid_lib.Data.PickingData;
import com.thad.rfid_lib.Data.WarehouseData;
import com.thad.rfid_lib.Decoder;
import com.thad.rfid_orderpick.MobileClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * Responsible for reading JSON files and saving new Experiment data
 */

public class FileIO {
    private static final String TAG = "|FileIO|";

    private static final String experimentDataFileName = "warehouse.json";
    private static final String pickDataFileName = "pick_tasks.json";


    private static MobileClient mClient;

    public FileIO(MobileClient client){
        mClient = client;
    }


    public WarehouseData loadWarehouseData(){
        try {
            String json_str = loadJSON(experimentDataFileName);
            return Decoder.decodeWarehouseDataJSON(json_str);
        } catch (Exception e) {
            Log.e(TAG, "Could not load Experiment Data.");
            e.printStackTrace();
        }
        return new WarehouseData();
    }

    public PickingData loadPickingData(){
        try {
            String json_str = loadJSON(pickDataFileName);
            return Decoder.decodePickingDataJSON(json_str);
        } catch (Exception e) {
            Log.e(TAG, "Could not load Picking Data.");
            e.printStackTrace();
        }
        return new PickingData();
    }


    private String loadJSON(String fname) throws IOException {
        InputStream is = mClient.getContext().getAssets().open(fname);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, Decoder.STRING_ENCODE);

        return json;
    }

}
