package com.thad.rfid_orderpick;

import java.util.HashMap;

/**
 * Created by theo on 1/30/18.
 */

public class PickingOrder {
    private HashMap<String, Integer> items;
    private String receiveBin;
    private int item_count;
    private PickingTask pickingTask;

    public PickingOrder(){
        items = new HashMap<String, Integer>();
        item_count = 0;
    }

    public void add(String tag, int val){
        items.put(tag, val);
        item_count += val;
    }

    public void setReceiveBin(String tag){
        receiveBin = tag;
    }

    public void setPickingTask(PickingTask pickingTask){
        this.pickingTask = pickingTask;
    }

    public int getTaskID(){
        return pickingTask.id;
    }

    public HashMap<String,Integer> getItems(){
        return items;
    }

    public int getItemCount(){
        return item_count;
    }

    public String getReceiveBinTag(){
        return receiveBin;
    }

    public int getTagCount(String tag){return items.get(tag);}

    public boolean hasTag(String tag){return items.containsKey(tag);}

    public void removeTag(String tag){
        item_count -= items.get(tag);
        items.remove(tag);
    }
}
