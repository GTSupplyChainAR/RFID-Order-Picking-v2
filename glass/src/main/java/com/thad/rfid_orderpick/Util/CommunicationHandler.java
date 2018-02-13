package com.thad.rfid_orderpick.Util;

import android.util.Log;

import com.thad.rfid_orderpick.ExperimentData;
import com.thad.rfid_orderpick.GlassMainActivity;
import com.thad.rfid_orderpick.PickingOrder;
import com.thad.rfid_orderpick.PickingTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This Class handles the communications between Mobile App and glass.
 * It decodes the messages received on Bluetooth and populates the data structures.
 */

//TO DO
// Check for errors in Picking Data format

public class CommunicationHandler {
    private static final String TAG = "CommunicationHandler";

    private static final int CODE_LENGTH = 4;
    private static final String CODE_SPLIT = "SPLIT";
    private enum MSG_CODES { DATA, SCAN, GAME }

    private GlassMainActivity mMain;


    public CommunicationHandler(GlassMainActivity activity){
        mMain = activity;
    }


    public boolean decodeMessage(String message){
        if(message == null)
            return false;


        /*
        String[] words = message.split("_");
        String[] data = new String[words.length-1];
        System.arraycopy(words, 1, data, 0, words.length-1);

        if(words.length <= 1)
            return false;
        */

        String code_str = message.substring(0,CODE_LENGTH);
        String data = message.substring(CODE_LENGTH, message.length());

        MSG_CODES code = MSG_CODES.valueOf(code_str);
        switch (code){
            case DATA:
                decodeData(data);
                break;
            case SCAN:
                decodeScan(data);
                break;
            case GAME:
                if(data.equals("START"))
                    mMain.mBrain.startExperiment();
                else if (data.equals("STOP"))
                    mMain.mBrain.stopExperiment();
                break;
        }
        return true;
    }


    private boolean decodeData(String data) {
        String[] data_split = data.split(CODE_SPLIT);
        if (data_split.length != 2){
            Log.d(TAG, "Could not decode Data.");
            return false;
        }
        String experimentJSON = data_split[0];
        String pickdataJSON = data_split[1];

        decodeExperimentData_JSON(experimentJSON);
        decodePickData_JSON(pickdataJSON);
        Log.d(TAG, "Received JSON Pick data of "+data.length()+" bytes");
        return true;
    }

    private boolean decodeExperimentData_JSON(String data){
        try {
            JSONObject jObj = new JSONObject(data);

            JSONObject experiment = jObj.getJSONObject("experiment");

            int rows = Integer.parseInt(experiment.getString("rows"));
            int cols = Integer.parseInt(experiment.getString("cols"));

            JSONArray rack = experiment.getJSONArray("rack");

            String[][] rack_tags = new String[rows][cols];

            for(int i = 0 ; i < rack.length() ; i++){
                JSONObject rack_rows = rack.getJSONObject(i);
                JSONArray rack_row_i = rack_rows.getJSONArray("row");
                for(int j = 0 ; j < rack_row_i.length() ; j++) {
                    JSONObject shelf_ij = rack_row_i.getJSONObject(j);
                    String tag = shelf_ij.getString("col");
                    rack_tags[i][j] = tag;
                }
            }

            int cart_bins = Integer.parseInt(experiment.getString("cart_bins"));
            String[] cart_tags = new String[cart_bins];

            JSONArray cart = experiment.getJSONArray("cart");
            for(int i = 0 ; i < cart.length() ; i++){
                JSONObject cart_bin = cart.getJSONObject(i);
                String tag = cart_bin.getString("bin");
                cart_tags[i] = tag;
            }

            ExperimentData expData = new ExperimentData(rack_tags, cart_tags);

            mMain.mBrain.setExperimentData(expData);

        }catch (Exception e){
            Log.e(TAG, "ExperimentJSON exception.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean decodePickData_JSON(String data){
        try {
            JSONObject jObj = new JSONObject(data);


            JSONArray tasks = jObj.getJSONArray("tasks");

            int task_index = -1;
            ArrayList<PickingTask> pickingTasks = new ArrayList<PickingTask>();

            for(int i = 0 ; i < tasks.length() ; i++){
                JSONObject task_i = tasks.getJSONObject(i);
                JSONArray orders = task_i.getJSONArray("orders");
                pickingTasks.add(new PickingTask(i));
                task_index++;
                for(int j = 0 ; j < orders.length() ; j++) {
                    PickingOrder pickingOrder = new PickingOrder(j);
                    JSONObject order_j = orders.getJSONObject(j);
                    String[] tags = getStringKeys(order_j);
                    for(int k = 0 ; k < tags.length ; k++){
                        if(tags[k].equals("target")) continue;
                        pickingOrder.add(tags[k], order_j.getInt(tags[k]));
                    }
                    pickingOrder.setReceiveBin(order_j.getString("target"));
                    pickingTasks.get(task_index).addOrder(pickingOrder);
                }
            }

            mMain.mBrain.setPickingTasks(pickingTasks);

        }catch (Exception e){
            Log.e(TAG, "Pick data JSON exception.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean decodeScan(String scan){
        mMain.onNewScan(scan);
        return true;
    }

    /* OLD WAY- USING CODED STRINGS
    private boolean decodeTags(String[] words){
        //e.g. 4_6_A11_A12_A13_B11...

        if(words == null || words.length <= 3 )  return false;

        int rows = Integer.parseInt(words[0]), cols = Integer.parseInt(words[1]);

        String[] tags = new String[words.length-2];
        System.arraycopy(words, 2, tags, 0, words.length-2);

        if(tags.length <= rows*cols) return false;


        String[][] rack_tags = new String[rows][cols];
        String[] cart_tags = new String[tags.length - rows*cols];

        int cnt = 0;
        for(int i = 0 ; i < rows ; i++){
            for(int j = 0 ; j < cols ; j++){
                rack_tags[i][j] = tags[cnt];
                cnt++;
            }
        }
        for(int i = cnt ; i < tags.length ; i++){
            cart_tags[i-cnt] = tags[i];
        }

        mMain.mBrain.setTags(rack_tags, cart_tags);
        return true;
    }

    private boolean decodePickData(String[] words){
        //e.g. TASK_ORDER_A112_ORDER_B113_TASK_ORDER...

        //TO DO
        //Check for errors

        //Go through the words and generate the data structure
        ArrayList<PickingTask> pickingTasks = new ArrayList<PickingTask>();
        int task_index = -1, path_index = -1;
        for(int i = 0 ; i < words.length ; i++){
            if(words[i].equals("TASK")){
                pickingTasks.add(new PickingTask());
                task_index ++;
                path_index = -1;
            }else if(words[i].equals("ORDER")){
                pickingTasks.get(task_index).paths.add(new PickingPath());
                path_index ++;
            }else{
                String tag = words[i].substring(0,3);
                char digit = words[i].charAt(3);
                int num = Integer.parseInt(digit+"");

                pickingTasks.get(task_index).paths.get(path_index).bin_tasks.put(tag, num);
            }
        }

        mMain.mBrain.setPickingTasks(pickingTasks);

        mMain.mLog("Received and decoded "+pickingTasks.size() +" picking tasks.");
        return true;
    }

    private boolean decodeScan(String[] words){
        if(words == null || words.length != 1) {
            mMain.mLog("Cannot decode the Scan.");
            return false;
        }

        mMain.mBrain.newScan(words[0]);
        return true;
    }
    */


    private String[] getStringKeys(JSONObject json){
        Iterator<String> iter = json.keys();
        List<String> keysList = new ArrayList<String>();
        while(iter.hasNext()) {
            String key = iter.next();
            keysList.add(key);
        }
        String[] keysArray = keysList.toArray(new String[keysList.size()]);
        return keysArray;
    }

}
