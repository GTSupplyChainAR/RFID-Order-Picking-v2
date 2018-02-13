package com.thad.rfid_orderpick;

import java.util.HashMap;

/**
 * Created by theo on 1/30/18.
 */

public class PickingOrder {
    private int id;

    private HashMap<String, Integer> items;
    private HashMap<String, Integer> bkup_items;
    private String receiveBin;
    private int item_count, scanned_item_count;
    private PickingTask pickingTask;

    public PickingOrder(int id){
        items = new HashMap<String, Integer>();
        bkup_items = new HashMap<String, Integer>();
        item_count = 0;
        scanned_item_count = 0;
        this.id = id;
    }

    public void add(String tag, int val){
        items.put(tag, val);
        bkup_items.put(tag, val);
        item_count += val;
    }

    public void remove(String tag){
        item_count -= items.get(tag);
        scanned_item_count += items.get(tag);
        items.remove(tag);
    }

    public void setReceiveBin(String tag){
        receiveBin = tag;
    }
    public void setPickingTask(PickingTask pickingTask){
        this.pickingTask = pickingTask;
    }

    public HashMap<String,Integer> getItems(){
        return items;
    }
    public HashMap<String,Integer> getBackupItems(){
        return bkup_items;
    }
    public int getItemCount(){
        return item_count;
    }
    public String getReceiveBinTag(){
        return receiveBin;
    }
    public int getTaskID(){
        return pickingTask.getID();
    }
    public int getID() {return id;}
    public int getBinItemCount(String tag) {return items.get(tag);}
    public int getScannedCount() {return scanned_item_count; }


    public boolean hasTag(String tag){
        return items.containsKey(tag);
    }
    public boolean hadTag(String tag){ return bkup_items.containsKey(tag);}
}
