package com.thad.rfid_orderpick;

import android.util.Log;

import java.util.ArrayList;

/**
 * This class keeps track of the experiment.
 */

public class MobileBrain {
    private static final String TAG = "MobileBrain";

    private static MobileMainActivity mMain;

    private static ExperimentData experimentData;

    private static int active_task, active_order;
    private static ArrayList<PickingTask> pickingTasks;


    private boolean pickDataReady = false, experimentDataReady = false, experimentRunning = false;


    public MobileBrain(MobileMainActivity activity){
        mMain = activity;
    }


    public void setExperimentData(ExperimentData expData){
        experimentData = expData;
        experimentDataReady = true;
    }

    public void setPickData(ArrayList<PickingTask> pickingTasks){
        this.pickingTasks = pickingTasks;
        pickDataReady = true;
    }

    public boolean startExperiment(){
        boolean isReady = checkRestrictions();
        //if(!isReady) return false;

        experimentRunning = true;
        mMain.mUI.startExperiment(experimentData);


        active_task = 0;
        active_order = -1;

        mMain.mUI.newOrder(nextOrder());

        return true;
    }

    public void stopExperiment(){

        //mMain.mUI.newOrder(nextOrder());
        mMain.mUI.stopExperiment();
        experimentRunning = false;
    }

    public boolean isExperimentRunning(){
        return experimentRunning;
    }

    public PickingOrder nextOrder(){
        active_order += 1;
        if(active_order >= pickingTasks.get(active_task).get_num_orders()){
            active_order = 0;
            active_task += 1;
        }
        if(active_task >= pickingTasks.size())
            active_task = 0;

        return pickingTasks.get(active_task).getOrder(active_order);
    }

    private boolean checkRestrictions(){
        boolean[] states = mMain.checkConnections();
        boolean connectionsReady = states[0] && states[1] && states[2];

        boolean dataReady = experimentDataReady && pickDataReady;

        return dataReady && connectionsReady;
    }

    public void onNewRFIDScan(String scan){
        //CHECK SCAN AGAINST RACK RFID TAGS
        //mMain.mLog("RFID Scan -> "+scan);
    }


}
