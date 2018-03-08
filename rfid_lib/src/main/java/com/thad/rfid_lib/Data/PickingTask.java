package com.thad.rfid_lib.Data;

import java.util.ArrayList;

/**
 * This class represents a n to 1 Picking Task.
 */

public class PickingTask {
    private int id = 0;
    private boolean isTraining = false;

    private ArrayList<PickingOrder> orders;

    public PickingTask(){
        orders = new ArrayList<PickingOrder>();
    }
    public PickingTask(int id, boolean isTraining){
        this();
        this.id = id;
        this.isTraining = isTraining;
    }

    public void add(PickingOrder pickingOrder){
        pickingOrder.setPickingTask(this);
        orders.add(pickingOrder);
    }

    public boolean isTraining(){return isTraining;}

    public ArrayList<PickingOrder> getOrders(){return orders;}
    public PickingOrder getOrder(int index){
        return orders.get(index);
    }
    public PickingOrder getLastOrder(){return orders.get(orders.size()-1);}
    public PickingOrder getOrderByID(int id){
        for(PickingOrder order: orders){
            if(order.getID() == id)
                return order;
        }
        return null;
    }
    public PickingOrder getNextOrder(PickingOrder order){
        for(int i = 0 ; i < orders.size() ; i++){
            if(orders.get(i).getID() == order.getID()){
                return orders.get((i+1)%orders.size());
            }
        }
        return null;
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

    public void setID(int id){this.id = id;}
}
