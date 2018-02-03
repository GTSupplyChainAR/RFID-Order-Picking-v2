package com.thad.rfid_orderpick;

import android.util.Log;

import com.thad.rfid_orderpick.Util.UserInterfaceHandler;

import java.util.ArrayList;

/**
 * Responsible for running the whole RFID experiment.
 * Inputs -> Rack dimensions, TAGs and Pick Data
 */

public class GlassBrain{
    private static final String TAG = "GlassBrain";

    private static UserInterfaceHandler mUI;

    private static ExperimentData experimentData;

    private static int activeTask = 0;
    private static ArrayList<PickingTask> pickingTasks;
    private boolean pickDataReady = false, experimentDataReady = false, experimentRunning = false;

    private ExperimentThread experimentThread;

    public GlassBrain(UserInterfaceHandler ui){
        mUI = ui;
    }

    public boolean startExperiment(){
        mUI.mLog("Starting Experiment...");

        boolean isReady = checkData();
        if(!isReady) return false;

        experimentThread = new ExperimentThread();
        experimentThread.start();

        experimentRunning = true;

        return true;
    }

    public boolean stopExperiment(){
        mUI.mLog("Stopping Experiment...");
        experimentThread.stopRunning();
        experimentRunning = false;
        return true;
    }

    private boolean checkData(){
        if(experimentDataReady && pickDataReady) {
            return true;
        }
        mUI.mLog("Data not set properly.");
        return false;
    }

    public void newScan(String tag){
        mUI.mLog("New Scan: "+tag);
    }

    public void setExperimentData(ExperimentData experimentData){
        this.experimentData = experimentData;
        experimentDataReady = true;
        int[] dims = experimentData.getDimensions();
        Log.d(TAG, "Experiment Dimensions: "+dims[0] + ", " + dims[1]);
    }
    public void setPickingTasks(ArrayList<PickingTask> pickingTasks){
        this.pickingTasks = pickingTasks;
        pickDataReady = true;
        Log.d(TAG, "Received "+pickingTasks.size()+" picking Tasks.");
    }


    private class ExperimentThread extends Thread{
        private boolean flag;

        ExperimentThread(){

        }

        public void run(){
            flag = true;
            while(flag){

            }
        }

        public void stopRunning(){
            flag = false;
        }
    }
}
