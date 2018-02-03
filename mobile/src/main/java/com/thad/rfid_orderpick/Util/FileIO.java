package com.thad.rfid_orderpick.Util;

import android.util.Log;

import com.thad.rfid_orderpick.ExperimentData;
import com.thad.rfid_orderpick.MobileMainActivity;
import com.thad.rfid_orderpick.PickingOrder;
import com.thad.rfid_orderpick.PickingTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Responsible for reading JSON files and saving new Experiment data
 */

public class FileIO {
    private static final String TAG = "FileIO";

    private static final int experiment = 0;

    private static final String[] experimentFileName = {"experiment.json", "experiment_1_rack.json"};
    private static final String[] pickDataFileName = {"pick_tasks.json", "pick_tasks_1_rack.json"};

    private static String experiment_json, picKData_json;

    private static MobileMainActivity mMain;

    public FileIO(MobileMainActivity activity){
        mMain = activity;
    }

    public boolean loadExperimentData(){

        String data = null;
        try {
            data = loadJSON(experimentFileName[experiment]);
            experiment_json = data;
        } catch (Exception e) {
            Log.e(TAG, experimentFileName[experiment] +" file exception.");
            e.printStackTrace();
            return false;
        }

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
            Log.e(TAG, experimentFileName[experiment] +" JSON exception.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean loadPickData(){
        String data = null;
        try {
            data = loadJSON(pickDataFileName[experiment]);
            picKData_json = data;
        } catch (Exception e) {
            Log.e(TAG, pickDataFileName[experiment]+ " file exception.");
            e.printStackTrace();
            return false;
        }

        try {
            JSONObject jObj = new JSONObject(data);


            JSONArray tasks = jObj.getJSONArray("tasks");

            int task_index = -1;
            ArrayList<PickingTask> pickingTasks = new ArrayList<>();

            for(int i = 0 ; i < tasks.length() ; i++){
                JSONObject task_i = tasks.getJSONObject(i);
                JSONArray orders = task_i.getJSONArray("orders");
                pickingTasks.add(new PickingTask());
                task_index++;
                for(int j = 0 ; j < orders.length() ; j++) {
                    PickingOrder pickingOrder = new PickingOrder();
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

            mMain.mBrain.setPickData(pickingTasks);

        }catch (Exception e){
            Log.e(TAG, pickDataFileName[experiment]+ " JSON exception.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String loadJSON(String fname) throws IOException {
        String json = null;

        InputStream is = mMain.getAssets().open(fname);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");

        return json;
    }

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

    public String getPickDataJSON(){
        return picKData_json;
    }
    public String getExperimentJSON(){
        return experiment_json;
    }

}
