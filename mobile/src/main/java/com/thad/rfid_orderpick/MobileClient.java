package com.thad.rfid_orderpick;

import android.content.Context;
import android.view.ViewGroup;

import com.thad.rfid_lib.Experiment;
import com.thad.rfid_lib.ExperimentListener;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_orderpick.Communications.CommunicationHandler;
import com.thad.rfid_orderpick.UI.FileIO;
import com.thad.rfid_orderpick.UI.UserInterfaceHandler;

/**
 * This class keeps track of the experiment.
 */

public class MobileClient implements ExperimentListener{
    private static final String TAG = "|AndroidClient|";



    private static Context mContext;

    public static UserInterfaceHandler mUI;
    private static CommunicationHandler mCommHandler;
    private static FileIO mFileIO;

    private static Experiment mExperiment;


    public MobileClient(Context context){
        mContext = context;
        init();

    }
    private void init(){
        mFileIO = new FileIO(this);

        mCommHandler = new CommunicationHandler(this);
        mUI = new UserInterfaceHandler(this);
        mExperiment = new Experiment(this);

        mUI.startUpdating();

        if(Prefs.QUICK_START){
            mCommHandler.reconnect();
            mExperiment.setData(mFileIO.loadWarehouseData(),
                                mFileIO.loadPickingData());
        }
    }


    //COMMANDS
    public void editAddress(int device_index, String new_address){
        mCommHandler.editAddress(device_index, new_address);
    }
    public void shutdown(){
        mCommHandler.stopExperiment();
        try {
            wait(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mCommHandler.shutdown();
    }
    //END OF COMMANDS


    //USER COMMANDS
    public void onSyncData(){
        mExperiment.setData(mFileIO.loadWarehouseData(),
                mFileIO.loadPickingData());
        //Send to Glass
        mCommHandler.send(mExperiment);
    }
    public void onConnect(){mCommHandler.reconnect();}
    public void onExperimentClicked(){
        if(mExperiment.isActive()){
            boolean canStop = mExperiment.stop();
            if(canStop) {
                mCommHandler.stopExperiment();
                mUI.onExperimentToggled();
            }
        }else{
            boolean canStart = mExperiment.start();
            if(canStart){
                mCommHandler.startExperiment();
                mUI.onExperimentToggled();
            }
        }
    }
    //END OF USER COMMANDS


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


    //GETTERS
    public String[] getAddresses(){return mCommHandler.getAddresses();}
    public boolean[] getConnStatus(){return mCommHandler.getConnStatus();}
    //END OF GETTERS


    @Override
    public ViewGroup getExperimentContainer(){return mUI.getExperimentContainer();}
    public Context getContext(){return mContext;}
    public void mLog(String str){mUI.mLog(str);}
    public void onFakeScan(String scan){onNewRFIDScan(scan, 255);}
    public boolean isGlass(){return false;}
    public void playSound(Utils.SOUNDS sound){}

}
