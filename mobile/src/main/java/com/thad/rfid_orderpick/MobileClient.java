package com.thad.rfid_orderpick;

import android.content.Context;
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

import java.util.List;

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

        if(Prefs.QUICK_START && !Prefs.RUN_OFFLINE){
            mCommHandler.connect();
        }
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
            startExperiment(true);
    }
    public void stopExperiment(){
        boolean canStop = mExperiment.stop();
        if(canStop) {
            if(!Prefs.RUN_OFFLINE)
                mCommHandler.stopExperiment();
            mUI.onExperimentToggled();
            mUI.onTrainingSelected();
        }
    }
    private void startExperiment(boolean isTraining){
        boolean canStart;
        if(isTraining)
            canStart = mExperiment.startTraining();
        else
            canStart = mExperiment.startTesting();

        if(canStart){
            if(!Prefs.RUN_OFFLINE)
                mCommHandler.startExperiment(isTraining);
            mUI.onExperimentToggled();
            mUI.onResume();
        }
    }
    public void pauseExperiment(){
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
        stopExperiment();
        mUI.onTrainingSelected();
        startExperiment(true);
    }
    public void onTestingClicked(){
        stopExperiment();
        mUI.onTestingSelected();
        startExperiment(false);
    }
    public void onPausePlayClicked(){
        if(mExperiment.isPaused()){
            resumeExperiment();
        } else {
            pauseExperiment();
        }
    }


    //EVENT LISTENERS
    public void onNewRFIDScan(String scan, int strength){
        //mLog("New RFID Scan: "+scan+", Strength: "+strength);
        mExperiment.onNewScan(scan);
        mUI.updateProgress(mExperiment.getProgress());
        if(!Prefs.RUN_OFFLINE)
            mCommHandler.sendScan(scan);
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
    public void onFakeScan(String scan){onNewRFIDScan(scan, 255);}
    public boolean isGlass(){return false;}
    public void playSound(Utils.SOUNDS sound){}

}
