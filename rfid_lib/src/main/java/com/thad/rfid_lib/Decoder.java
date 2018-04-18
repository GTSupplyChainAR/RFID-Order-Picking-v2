package com.thad.rfid_lib;

import android.util.Log;

import com.thad.rfid_lib.Data.PickingData;
import com.thad.rfid_lib.Data.PickingOrder;
import com.thad.rfid_lib.Data.PickingTask;
import com.thad.rfid_lib.Data.ShelvingUnit;
import com.thad.rfid_lib.Data.WarehouseData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by theo on 2/22/18.
 */

public class Decoder {
    private static final String TAG = "|Decoder|";
    public static int HEADER_MSG_LENGTH_SIZE = 10;
    public static int HEADER_MSG_TAG_SIZE = 20;
    public static final String STRING_ENCODE = "UTF-8";

    public enum MSG_TAG {
        SCAN,
        START,
        TRAIN,
        TEST,
        STOP,
        PAUSE,
        RESUME,
        TAP
    }

    public static WarehouseData decodeWarehouseDataJSON(String warehouseDataJSON) throws JSONException {
        WarehouseData warehouseData = new WarehouseData();

        JSONObject jObj = new JSONObject(warehouseDataJSON);
        warehouseData.setVersion(jObj.getString("version"));

        JSONArray json_shelving_units = jObj.getJSONArray("shelving_units");
        for(int i = 0 ; i < json_shelving_units.length() ; i++){
            JSONObject json_unit_i = json_shelving_units.getJSONObject(i);
            String tag = json_unit_i.getString("shelving_unit_tag");
            int rows = json_unit_i.getInt("rows");
            int cols = json_unit_i.getInt("cols");
            boolean isCart = json_unit_i.getBoolean("isCart");

            ShelvingUnit shelvingUnit = new ShelvingUnit(tag, rows, cols, isCart);
            warehouseData.addShelvingUnit(shelvingUnit);
        }
        warehouseData.setJSON(warehouseDataJSON);
        return warehouseData;
    }

    public static PickingData decodePickingDataJSON(String pickingDataJSON) throws JSONException{
        PickingData pickingData = new PickingData();

        JSONObject jObj = new JSONObject(pickingDataJSON);

        pickingData.setVersion(jObj.getString("version"));
        JSONArray tasks = jObj.getJSONArray("tasks");

        for(int i = 0 ; i < tasks.length() ; i++){
            JSONObject json_task_i = tasks.getJSONObject(i);
            JSONArray json_orders = json_task_i.getJSONArray("orders");

            int task_i_id = json_task_i.getInt("taskId");
            //boolean isTraining = json_task_i.getBoolean("isTrainingTask");
            PickingTask task_i = new PickingTask(task_i_id);//, isTraining);

            for(int j = 0 ; j < json_orders.length() ; j++) {
                JSONObject json_order_j = json_orders.getJSONObject(j);
                int order_id = json_order_j.getInt("orderId");

                PickingOrder pickingOrder = new PickingOrder(order_id);
                pickingOrder.setReceiveBin(json_order_j.getString("receivingBinTag"));

                JSONArray sourceBins = json_order_j.getJSONArray("sourceBins");
                for(int k = 0 ; k < sourceBins.length() ; k++){
                    JSONObject sourceBin_k = sourceBins.getJSONObject(k);
                    pickingOrder.add(sourceBin_k.getString("binTag"),
                                        sourceBin_k.getInt("numItems"));
                }
                task_i.add(pickingOrder);
            }
            pickingData.add(task_i);
        }
        pickingData.setJSON(pickingDataJSON);
        return pickingData;
    }

    /*
    public static ExperimentData decodeExperimentDataJSON(String experimentDataJSON) throws JSONException {

        JSONObject jObj = new JSONObject(experimentDataJSON);

        JSONObject experiment = jObj.getJSONObject("experiment");

        int rows = Integer.parseInt(experiment.getString("rows"));
        int cols = Integer.parseInt(experiment.getString("cols"));
        int cart_bins = Integer.parseInt(experiment.getString("cart_bins"));
        String[] cart_tags = new String[cart_bins];
        String[][] rack_tags = new String[rows][cols];

        JSONArray rack = experiment.getJSONArray("rack");
        for(int i = 0 ; i < rack.length() ; i++){
            JSONObject rack_rows = rack.getJSONObject(i);
            JSONArray rack_row_i = rack_rows.getJSONArray("row");
            for(int j = 0 ; j < rack_row_i.length() ; j++) {
                JSONObject shelf_ij = rack_row_i.getJSONObject(j);
                String tag = shelf_ij.getString("col");
                rack_tags[i][j] = tag;
            }
        }

        JSONArray cart = experiment.getJSONArray("cart");
        for(int i = 0 ; i < cart.length() ; i++){
            JSONObject cart_bin = cart.getJSONObject(i);
            String tag = cart_bin.getString("bin");
            cart_tags[i] = tag;
        }

        return new ExperimentData(rack_tags, cart_tags);
    }

    public static PickingData decodePickingDataJSON(String pickingDataJSON) throws JSONException{
        PickingData pickingData = new PickingData();

        JSONObject jObj = new JSONObject(pickingDataJSON);

        JSONArray tasks = jObj.getJSONArray("tasks");

        for(int i = 0 ; i < tasks.length() ; i++){
            JSONObject task_i = tasks.getJSONObject(i);
            JSONArray orders = task_i.getJSONArray("orders");
            pickingData.add(new PickingTask());
            for(int j = 0 ; j < orders.length() ; j++) {
                PickingOrder pickingOrder = new PickingOrder();
                JSONObject order_j = orders.getJSONObject(j);
                String[] tags = Utils.getJSONStringKeys(order_j);
                for(int k = 0 ; k < tags.length ; k++){
                    if(tags[k].equals("target"))
                        pickingOrder.setReceiveBin(order_j.getString("target"));
                    else
                        pickingOrder.add(tags[k], order_j.getInt(tags[k]));
                }
                pickingData.add(pickingOrder);
            }
        }
        return pickingData;
    }
    //*/

    public static int decodeMSGlength(byte[] buffer){
        byte[] header = new byte[Decoder.HEADER_MSG_LENGTH_SIZE];
        System.arraycopy(buffer, 0, header, 0, Decoder.HEADER_MSG_LENGTH_SIZE);
        ByteBuffer wrapped = ByteBuffer.wrap(header);
        return wrapped.getInt();
    }

    public static MSG_TAG decodeMSGtag(byte[] msgBytes){
        byte[] tagBytes = new byte[HEADER_MSG_TAG_SIZE];
        System.arraycopy(msgBytes, 0, tagBytes, 0, HEADER_MSG_TAG_SIZE);

        String strTag = new String(tagBytes);

        int count = 0;
        for(int i = 0 ; i < strTag.length() ; i++){
            if(Character.getNumericValue(strTag.charAt(i)) == -1)
                break;
            count ++;
        }
        strTag = strTag.substring(0, count);

        return MSG_TAG.valueOf(strTag);
    }

    public static String decodeMSGtoString(byte[] msg){
        int n = msg.length - Decoder.HEADER_MSG_TAG_SIZE;
        byte[] dataWithoutTag = new byte[n];
        System.arraycopy(msg, Decoder.HEADER_MSG_TAG_SIZE, dataWithoutTag, 0, n);
        return new String(dataWithoutTag);
    }

    public static byte[] encodeMSG(MSG_TAG tag, String data){
        byte[] data_bytes;
        try{
            data_bytes = data.getBytes(STRING_ENCODE);
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
            Log.e(TAG, "Unsupported Encoding Exception. Tag = "+tag.toString()+", Msg = "+data);
            return null;
        }
        byte[] tag_bytes = getHeaderBytes(tag);
        int n = data_bytes.length + tag_bytes.length;
        byte[] msg_size_bytes = getHeaderBytes(n);

        byte[] msg = new byte[n+msg_size_bytes.length];
        System.arraycopy(msg_size_bytes, 0, msg, 0, msg_size_bytes.length);
        System.arraycopy(tag_bytes, 0, msg, msg_size_bytes.length, tag_bytes.length);
        System.arraycopy(data_bytes, 0, msg, msg_size_bytes.length+tag_bytes.length, data_bytes.length);

        return msg;
    }

    //Encode the headers into byte arrays
    private static byte[] getHeaderBytes(int n){
        ByteBuffer dbuf = ByteBuffer.allocate(HEADER_MSG_LENGTH_SIZE);
        dbuf.putInt(n);
        return dbuf.array();
    }
    private static byte[] getHeaderBytes(MSG_TAG tag){
        String str = tag.toString();
        byte[] bytes = new byte[HEADER_MSG_TAG_SIZE];

        try {
            byte[] raw_bytes = str.getBytes(STRING_ENCODE);

            if(raw_bytes.length <= bytes.length)
                System.arraycopy(raw_bytes, 0, bytes, 0, raw_bytes.length);
            else
                Log.e(TAG, "Tag larger than allowed.");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, "Unsupported Encoding Exception. Tag = "+str);
        }

        return bytes;
    }
}
