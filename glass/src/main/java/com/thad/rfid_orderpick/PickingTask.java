package com.thad.rfid_orderpick;

import java.util.ArrayList;

/**
 * This class represents a n to 1 Picking Task.
 */

public class PickingTask {
    private int id = 0;

    private ArrayList<PickingOrder> orders;

    public PickingTask(int id){
        orders = new ArrayList<PickingOrder>();
        this.id = id;
    }

    public void addOrder(PickingOrder pickingOrder){
        pickingOrder.setPickingTask(this);
        orders.add(pickingOrder);
    }

    public PickingOrder getOrder(int index){
        return orders.get(index);
    }
    public int getOrderCount(){
        return orders.size();
    }
    public int getID(){return id;}
    public int getOrderIndex(int id){
        for(int i = 0 ; i < orders.size() ; i++){
            if(id == orders.get(i).getID()){
                return i;
            }
        }
        return -1;
    }
}
