package com.thad.rfid_lib;

import android.content.Context;
import android.util.Log;

import com.thad.rfid_lib.Data.PickingData;
import com.thad.rfid_lib.Data.WarehouseData;
import com.thad.rfid_lib.Decoder;
import com.thad.rfid_lib.Static.Prefs;

import java.io.IOException;
import java.io.InputStream;

/**
 * Responsible for reading JSON files and saving new Experiment data
 */

public class FileIO {
    private static final String TAG = "|FileIO|";



    private Context context;

    public FileIO(Context context){
        this.context = context;
    }


    public WarehouseData loadWarehouseData(){
        try {
            String json_str = loadJSON(Prefs.WAREHOUSE_DATA_FILENAME);
            return Decoder.decodeWarehouseDataJSON(json_str);
        } catch (Exception e) {
            Log.e(TAG, "Could not load Experiment Data.");
            e.printStackTrace();
        }
        return new WarehouseData();
    }

    public PickingData loadPickingDataTesting(){
        PickingData testingData = loadPickingData(Prefs.PICK_DATA_TESTING_FILENAME);
        testingData.setIsTraining(false);
        return testingData;
    }
    public PickingData loadPickingDataTraining(){
        PickingData trainingData = loadPickingData(Prefs.PICK_DATA_TRAINING_FILENAME);
        trainingData.setIsTraining(true);
        return trainingData;
    }
    private PickingData loadPickingData(String filename){
        try {
            String json_str = loadJSON(filename);
            return Decoder.decodePickingDataJSON(json_str);
        } catch (Exception e) {
            Log.e(TAG, "Could not load Picking Data.");
            e.printStackTrace();
        }
        return new PickingData();
    }


    private String loadJSON(String fname) throws IOException {
        InputStream is = context.getAssets().open(fname);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, Decoder.STRING_ENCODE);

        return json;
    }

}
