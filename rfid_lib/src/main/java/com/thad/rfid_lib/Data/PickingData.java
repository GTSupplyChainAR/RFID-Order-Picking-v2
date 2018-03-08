package com.thad.rfid_lib.Data;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by theo on 2/22/18.
 */

public class PickingData {
    private static final String TAG = "|PickingData|";
    private String json_string, version;

    private ArrayList<PickingTask> pickingTasks;
    private int numTrainingTasks;


    public PickingData(){
        pickingTasks = new ArrayList<PickingTask>();
    }


    public void add(PickingTask task){
        pickingTasks.add(task);
        if(task.isTraining())
            numTrainingTasks++;
    }

    public void reset(){
        for(PickingTask task : pickingTasks){
            for(PickingOrder order : task.getOrders()){
                order.reset();
            }
        }
    }


    public PickingTask getTask(int index){return pickingTasks.get(index);}
    public PickingTask getLastTask(){return pickingTasks.get(pickingTasks.size()-1);}
    public PickingTask getTaskByID(int id){
        for(PickingTask task: pickingTasks){
            if(task.getID() == id)
                return task;
        }
        return null;
    }
    public PickingTask getNextTask(PickingTask pickingTask){
        for(int i = 0 ; i < pickingTasks.size() ; i++){
            if(pickingTasks.get(i).getID() == pickingTask.getID()){
                return pickingTasks.get((i+1)%pickingTasks.size());
            }
        }
        return null;
    }
    public int getTaskCount(){return pickingTasks.size();}

    public PickingOrder getRandomOrder(){
        Random randomGen = new Random();

        int randTaskInd = randomGen.nextInt(pickingTasks.size());
        PickingTask randTask = pickingTasks.get(randTaskInd);

        int randOrderInd = randomGen.nextInt(randTask.getOrderCount());
        return randTask.getOrder(randOrderInd);
    }

    public void setJSON(String json_string){this.json_string = json_string;}
    public void setVersion(String version){this.version = version;}
    public String getJSON(){return json_string;}
    public String getVersion(){return version;}
    public String toString(){
        return "Tasks: "+numTrainingTasks+" training and "
                        +(pickingTasks.size()-numTrainingTasks)+" picking.";
    }
}
