package com.thad.rfid_orderpick;

import java.util.ArrayList;

/**
 * This class represents a n to 1 Picking Task.
 */

public class PickingTask {
    private static int total_tasks = 0;
    public int id = 0;
    private ArrayList<PickingOrder> orders;

    public PickingTask(){
        orders = new ArrayList<PickingOrder>();
        id = total_tasks;
        total_tasks += 1;
    }

    public void addOrder(PickingOrder pickingOrder){
        pickingOrder.setPickingTask(this);
        orders.add(pickingOrder);
    }

    public PickingOrder getOrder(int index){
        return orders.get(index);
    }
    public int get_num_orders(){
        return orders.size();
    }
}
