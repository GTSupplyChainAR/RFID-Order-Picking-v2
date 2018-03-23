package com.thad.rfid_lib.Data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by theo on 1/30/18.
 */

public class PickingOrder {
    private PickingTask pickingTask;
    private int id;

    private HashMap<String, Integer> remaining_items;
    private HashMap<String, Integer> scanned_items;
    private String receiveBin;
    private int remaining_item_count, scanned_item_count;

    public PickingOrder(int id){this(); this.id = id;}
    public PickingOrder(){
        remaining_item_count = 0;
        remaining_items = new HashMap<String, Integer>();
        scanned_item_count = 0;
        scanned_items = new HashMap<String, Integer>();
    }

    public void add(String tag, int val){
        remaining_items.put(tag, val);
        remaining_item_count += val;
    }

    public void scan(String tag){
        if(!remaining_items.containsKey(tag))
            return;
        int bin_item_num = remaining_items.get(tag);
        remaining_item_count -= bin_item_num;
        scanned_item_count += bin_item_num;

        remaining_items.remove(tag);
        scanned_items.put(tag, bin_item_num);
    }

    public void setReceiveBin(String tag){
        receiveBin = tag;
    }
    public void setPickingTask(PickingTask pickingTask){
        this.pickingTask = pickingTask;
    }
    public void setID(int id){this.id = id;}

    public HashMap<String,Integer> getRemainingItems(){
        return remaining_items;
    }
    public HashMap<String,Integer> getScannedItems(){
        return scanned_items;
    }
    public int getRemainingCount(){
        return remaining_item_count;
    }
    public int getScannedCount(){
        return scanned_item_count;
    }
    public int getRemainingCountInShelvingUnit(ShelvingUnit shelvingUnit){
        return getCountInShelvingUnit(shelvingUnit, remaining_items);
    }
    public int getScannedCountInShelvingUnit(ShelvingUnit shelvingUnit){
        return getCountInShelvingUnit(shelvingUnit, scanned_items);
    }
    private int getCountInShelvingUnit(ShelvingUnit shelvingUnit, HashMap<String, Integer> items){
        int count = 0;
        Set<String> tags = items.keySet();
        for(String tag: tags){
            if(String.valueOf(tag.charAt(0)).equals(shelvingUnit.getTag()))
                count += items.get(tag);
        }
        return count;
    }

    public String getReceiveBinTag(){
        return receiveBin;
    }
    public int getTaskID(){
        return pickingTask.getID();
    }
    public PickingTask getTask(){return pickingTask;}
    public int getID() {return id;}
    public int getBinItemCount(String tag) {return remaining_items.get(tag);}

    public void reset(){
        remaining_items.putAll(scanned_items);
        scanned_items.clear();
    }

    public boolean hasTag(String tag){
        return remaining_items.containsKey(tag);
    }
    public boolean hadTag(String tag){ return scanned_items.containsKey(tag);}
}
