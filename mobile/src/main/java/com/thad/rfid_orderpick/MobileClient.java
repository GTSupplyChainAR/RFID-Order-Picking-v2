package com.thad.rfid_orderpick;

import android.content.Context;
import android.view.ViewGroup;

import com.thad.rfid_lib.Experiment.Experiment;
import com.thad.rfid_lib.Experiment.ExperimentListener;
import com.thad.rfid_lib.Experiment.ExperimentLog;
import com.thad.rfid_lib.FileIO;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_orderpick.Communications.CommunicationHandler;
import com.thad.rfid_orderpick.Log.StudyHandler;
import com.thad.rfid_orderpick.UI.UserInterfaceHandler;

import java.util.List;

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

        mCommHandler = new CommunicationHandler(this);
        mUI = new UserInterfaceHandler(this);
        mExperiment = new Experiment(this);

        mUI.startUpdating();

        mExperiment.setData(mFileIO.loadWarehouseData(),
                mFileIO.loadPickingData());

        if(Prefs.QUICK_START){
            mCommHandler.connect();
        }
    }


    //COMMANDS
    public void editAddress(int device_index, String new_address){
        mCommHandler.editAddress(device_index, new_address);
    }
    public void shutdown(){
        mCommHandler.disconnect();
    }
    //END OF COMMANDS


    public void onConnect(){mCommHandler.connect();}
    public void onDisconnect(){mCommHandler.disconnect();}
    public void onExperimentClicked(){
        if(mExperiment.isActive())
            stopExperiment();
        else
            startExperiment();
    }
    public void stopExperiment(){
        boolean canStop = mExperiment.stop();
        if(canStop) {
            mCommHandler.stopExperiment();
            mUI.onExperimentToggled();
        }
    }
    private void startExperiment(){
        boolean canStart = mExperiment.start();
        if(canStart){
            mCommHandler.startExperiment();
            mUI.onExperimentToggled();
        }
    }
    //END OF USER COMMANDS

    public void onGlassTapped(){
        mLog("Glass Tapped.");
        mExperiment.errorFixed();
    }

    public void onLogClicked(){
        mUI.editLogPopup();
    }


    //EVENT LISTENERS
    public void onNewRFIDScan(String scan, int strength){
        mLog("New RFID Scan: "+scan+", Strength: "+strength);
        mExperiment.onNewScan(scan);
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

    public void onSubjectCreated(String username){ mStudyHandler.onSubjectCreated(username);}
    public void onSubjectSelected(String username){ mStudyHandler.onSubjectSelected(username);}
    public List<String> getSubjectNames(){ return mStudyHandler.getSubjectNames(); }

    //GETTERS
    public String[] getAddresses(){return mCommHandler.getAddresses();}
    public boolean[] getConnStatus(){return mCommHandler.getConnStatus();}
    public Long getExperimentTime(){return mExperiment.getElapsedTime();}
    //END OF GETTERS


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
