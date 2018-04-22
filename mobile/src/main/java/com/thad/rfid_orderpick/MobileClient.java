package com.thad.rfid_orderpick;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.thad.rfid_lib.Experiment.Experiment;
import com.thad.rfid_lib.Experiment.ExperimentListener;
import com.thad.rfid_lib.Experiment.ExperimentLog;
import com.thad.rfid_lib.FileIO;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_orderpick.Communications.CommunicationHandler;
import com.thad.rfid_orderpick.Log.StudyData;
import com.thad.rfid_orderpick.Log.StudyHandler;
import com.thad.rfid_orderpick.Log.StudySubject;
import com.thad.rfid_orderpick.UI.UserInterfaceHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.security.auth.Subject;

/**
 * This class keeps track of the experiment.
 */

public class MobileClient implements ExperimentListener{
    private static final String TAG = "|AndroidClient|";


    private static Context mContext;

    public static UserInterfaceHandler mUI;
    private CommunicationHandler mCommHandler;
    private FileIO mFileIO;

    private Experiment mExperiment;
    private StudyHandler mStudyHandler;

    private boolean processingScan = false;

    public MobileClient(Context context){
        mContext = context;

        init();
    }
    private void init(){
        mFileIO = new FileIO(mContext);
        mStudyHandler = new StudyHandler(this);

        if(!Prefs.RUN_OFFLINE)
            mCommHandler = new CommunicationHandler(this);
        mUI = new UserInterfaceHandler(this);
        mExperiment = new Experiment(this);

        mUI.startUpdating();

        mExperiment.setData(mFileIO.loadWarehouseData(),
                mFileIO.loadPickingDataTraining(), mFileIO.loadPickingDataTesting());

        if(Prefs.QUICK_START && !Prefs.RUN_OFFLINE)
            mCommHandler.connect();
    }


    //COMMANDS
    public void editAddress(int device_index, String new_address){
        if(!Prefs.RUN_OFFLINE)
            mCommHandler.editAddress(device_index, new_address);
    }
    public void shutdown(){
        if(Prefs.RUN_OFFLINE)
            mCommHandler.disconnect();
    }
    //END OF COMMANDS


    public void onConnect(){
        if(!Prefs.RUN_OFFLINE)
            mCommHandler.connect();
    }
    public void onDisconnect(){
        if(!Prefs.RUN_OFFLINE)
            mCommHandler.disconnect();
    }
    public void onExperimentClicked(){
        if(mExperiment.isRunning())
            stopExperiment();
        else
            startExperiment();
    }
    public void stopExperiment(){
        boolean canStop = mExperiment.stop();
        if(canStop) {
            if(!Prefs.RUN_OFFLINE)
                mCommHandler.stopExperiment();
            mUI.onExperimentToggled();
            //mUI.onTrainingSelected();
        }
    }
    private void startExperiment(){
        boolean canStart = mExperiment.start();

        if(canStart){
            if(!Prefs.RUN_OFFLINE)
                mCommHandler.startExperiment();
            mUI.onExperimentToggled();
            mUI.onResume();
        }
    }
    public void pauseExperiment(){
        playErrorSound();

        mExperiment.pause();
        mUI.onPause();
        if(!Prefs.RUN_OFFLINE)
            mCommHandler.pauseExperiment();
    }
    public void resumeExperiment(){
        mExperiment.resume();
        mUI.onResume();
        if(!Prefs.RUN_OFFLINE)
            mCommHandler.resumeExperiment();
    }
    //END OF USER COMMANDS

    public void onGlassTapped(){
        mLog("Glass Tapped.");
        mExperiment.errorFixed();
    }

    public void onLogClicked(){
        mUI.editLogPopup();
    }
    public void onTrainingClicked(){
        if(mExperiment.isRunning())
            stopExperiment();
        mUI.onTrainingSelected();
        mCommHandler.setTraining();
        mExperiment.setTraining();
        //startExperiment(true);
    }
    public void onTestingClicked(){
        if(mExperiment.isRunning())
            stopExperiment();
        mUI.onTestingSelected();
        mCommHandler.setTesting();
        mExperiment.setTesting();
        //startExperiment(false);
    }
    public void onPausePlayClicked(){
        if(mExperiment.isPaused())
            resumeExperiment();
        else
            pauseExperiment();
    }


    //EVENT LISTENERS
    public void onNewRFIDScan(String scan, int strength){
        if(!processingScan) {
            processingScan = true;
        }else{
            long inTime = System.currentTimeMillis();
            long cutoff_time = 500;
            while(processingScan) {
                if(System.currentTimeMillis() - inTime >= cutoff_time)
                    return;
                continue;
            }
            processingScan = true;
        }

        if(!Prefs.RUN_OFFLINE)
            mCommHandler.sendScan(scan);
        mExperiment.onNewScan(scan);
        mUI.updateProgress(mExperiment.getProgress());

        processingScan = false;
    }
    public void onBatteryUpdate(int device_index, double battery_level) {
        mUI.updateBattery(device_index, battery_level);
    }
    public void deviceConnUpdate(int device_index, boolean state){
        String conn_statement = (state)?" has connected.":" has disconnected.";
        mLog(Prefs.device_names[device_index]+conn_statement);
    }
    //END OF LISTENERS

    public void onSubjectSelected(String username){ mStudyHandler.onSubjectSelected(username);}
    public List<String> getSubjectNames(){ return mStudyHandler.getSubjectNames(); }
    public StudySubject getActiveSubject(){return mStudyHandler.getActiveSubject();}

    //GETTERS
    public String[] getAddresses(){
        if(!Prefs.RUN_OFFLINE)
            return mCommHandler.getAddresses();
        else
            return new String[]{Prefs.GLASS_ADDRESS, Prefs.XBAND1_ADDRESS, Prefs.XBAND2_ADDRESS};
    }
    public boolean[] getConnStatus(){
        if(!Prefs.RUN_OFFLINE)
            return mCommHandler.getConnStatus();
        else
            return new boolean[]{false, false, false};
    }
    public Long getExperimentTime(){return mExperiment.getElapsedTime();}
    public StudyData getStudyData(){return mStudyHandler.getStudyData();}
    //END OF GETTERS

    public boolean isExperimentRunning(){return mExperiment.isRunning();}
    public boolean isExperimentActive(){return mExperiment.isActive();}
    public boolean isExperimentPaused(){return mExperiment.isPaused();}

    @Override
    public ViewGroup getExperimentContainer(){return mUI.getExperimentContainer();}
    public Context getContext(){return mContext;}
    public boolean isStudyRunning(){return mStudyHandler.isStudyRunning();}
    public ExperimentLog getExperimentLog(){return mStudyHandler.getExperimentLog();}
    public void autosave(){mStudyHandler.autosave();}
    public void mLog(String str){mUI.mLog(str);}
    public void onFakeScan(String scan){
        //Code to test concurrent scans.
        /*String[] all_tags = new String[]{"A11","A12","A13",
                "A21","A22","A23",
                "A31","A32","A33",
                "A41","A42","A43"};

        ScanThread[] threads = new ScanThread[all_tags.length];
        for(int i = 0 ; i < all_tags.length ; i++) {
            Random rand = new Random();
            int ri = rand.nextInt(all_tags.length);
            threads[i] = new ScanThread(all_tags[i]);
        }
        for(ScanThread thread : threads)
            thread.start();*/
        onNewRFIDScan(scan, 255);
    }
    public boolean isGlass(){return false;}
    public void playSound(Utils.SOUNDS sound){}

    private void playErrorSound(){
        try {
            Uri soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getContext(), soundURI);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class ScanThread extends Thread{
        String tag;
        public ScanThread(String tag){this.tag = tag;}
        public void run(){
            Log.d(TAG, "----- Starting thread "+tag);
            onNewRFIDScan(tag, 255);
        }
    }
}
