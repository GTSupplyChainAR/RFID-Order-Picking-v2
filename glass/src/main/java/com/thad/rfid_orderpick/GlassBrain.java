package com.thad.rfid_orderpick;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.google.android.glass.media.Sounds;
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

    private static PickingOrder activeOrder;
    private static ArrayList<PickingTask> pickingTasks;
    private boolean pickDataReady = false, experimentDataReady = false, experimentRunning = false;
    private boolean errorMode = false, cartMode = false;

    private ExperimentThread experimentThread;
    private AudioManager mAudio;

    public GlassBrain(Context context, UserInterfaceHandler ui){
        mAudio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mUI = ui;
    }

    public boolean startExperiment(){
        mUI.mLog("Starting Experiment...");

        boolean isReady = checkData();
        if(!isReady) return false;

        experimentThread = new ExperimentThread();
        experimentThread.start();
        mUI.startExperiment();

        experimentRunning = true;

        mUI.newOrder(activeOrder);

        return true;
    }

    public boolean stopExperiment(){
        mUI.mLog("Stopping Experiment...");
        experimentThread.stopRunning();
        experimentRunning = false;

        mUI.stopExperiment();

        return true;
    }

    private PickingOrder getNextOrder(){
        PickingTask activeTask = pickingTasks.get(activeOrder.getTaskID());
        int order_index = activeTask.getOrderIndex(activeOrder.getID());

        int next_order_index = order_index + 1;
        if(next_order_index >= activeTask.getOrderCount()){
            next_order_index = 0;
            int task_index = getTaskIndex(activeTask.getID());
            int next_task_index = task_index + 1;
            if(next_task_index >= pickingTasks.size()){
                return null;
            }else{
                activeTask = pickingTasks.get(next_task_index);
            }
        }

        return activeTask.getOrder(next_order_index);
    }

    private boolean checkData(){
        if(experimentDataReady && pickDataReady) {
            return true;
        }
        mUI.mLog("Data not set properly.");
        return false;
    }


    //ACTIVE
    public void onNewScan(String tag){
        if(!experimentRunning || errorMode) return;
        mUI.mLog("New Scan: "+tag);

        if(tag.charAt(0) == 'C'){
            Log.d(TAG, "It's a Cart bin.");
            if(!cartMode || !tag.equals(activeOrder.getReceiveBinTag())){
                Log.d(TAG, "ERROR MODE.");
                //PROBLEM
                cartMode = true; errorMode = true;

                //WRONG RECEIVE BIN
                mUI.setErrorMode(activeOrder, tag);
                mAudio.playSoundEffect(Sounds.DISALLOWED);
            }else{
                Log.d(TAG, "TASK COMPLETED.");
                cartMode = false;
                //CORRECT RECEIVE BIN
                activeOrder = getNextOrder();
                mUI.newOrder(activeOrder);
                mAudio.playSoundEffect(Sounds.SUCCESS);
                //Check if it is last
            }
        }else{
            if(cartMode) {
                Log.d(TAG, "RACK BIN WHILE ON CART MODE.");
                mAudio.playSoundEffect(Sounds.DISALLOWED);

            }else if(activeOrder.hasTag(tag)){
                Log.d(TAG, "PROPER TAG.");
                activeOrder.remove(tag);
                mUI.clearBin(tag);

                if(activeOrder.getItemCount() == 0){
                    Log.d(TAG, "LAST ONE.");
                    cartMode = true;
                    mUI.setCartMode(activeOrder.getReceiveBinTag(), activeOrder.getScannedCount());
                }
                mAudio.playSoundEffect(Sounds.TAP);
            //}else {
            }else if (!activeOrder.hadTag(tag)) {
                Log.d(TAG, "WRONG TAG.");
                //WRONG RACK BIN
                mUI.wrongBin(tag);
                mAudio.playSoundEffect(Sounds.DISALLOWED);
            }
        }
    }

    public void onTap(){
        if(!errorMode) return;

        cartMode = false;
        errorMode = false;

        activeOrder = getNextOrder();
        mUI.newOrder(activeOrder);
    }


    //I/Os
    public void setExperimentData(ExperimentData experimentData){
        this.experimentData = experimentData;
        experimentDataReady = true;
        int[] dims = experimentData.getDimensions();
        Log.d(TAG, "Experiment Dimensions: "+dims[0] + ", " + dims[1]);

        mUI.setData(experimentData);
    }
    public void setPickingTasks(ArrayList<PickingTask> pickingTasks){
        this.pickingTasks = pickingTasks;
        activeOrder = pickingTasks.get(0).getOrder(0);
        pickDataReady = true;
        mUI.mLog("Received "+pickingTasks.size()+" picking Tasks.");
        if(experimentDataReady){
            mUI.mLog("Ready to START.");
        }
    }

    public ExperimentData getExperimentData(){
        return experimentData;
    }
    private int getTaskIndex(int id){
        for(int i = 0 ; i < pickingTasks.size() ; i++){
            if(id == pickingTasks.get(i).getID()){
                return i;
            }
        }
        return -1;
    }


    //THREAD AND RUNNABLES
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
