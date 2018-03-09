package com.thad.rfid_orderpick;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.ViewGroup;

import com.google.android.glass.media.Sounds;
import com.thad.rfid_lib.Data.PickingData;
import com.thad.rfid_lib.Data.WarehouseData;
import com.thad.rfid_lib.Experiment;
import com.thad.rfid_lib.ExperimentListener;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_orderpick.Communications.CommunicationHandler;
import com.thad.rfid_orderpick.UI.UserInterfaceHandler;

/**
 * Responsible for running the whole RFID experiment.
 * Inputs -> Rack dimensions, TAGs and Pick Data
 */

public class GlassClient implements ExperimentListener {
    private static final String TAG = "|GlassClient|";

    private Context mContext;

    private UserInterfaceHandler mUI;
    private CommunicationHandler mCommHandler;
    private FileIO mFileIO;

    private Experiment mExperiment;

    public GlassClient(Context context){
        mContext = context;

        mUI = new UserInterfaceHandler(this);
        mCommHandler = new CommunicationHandler(this);
        mFileIO = new FileIO(this);

        mExperiment = new Experiment(this);


        mExperiment.setData(mFileIO.loadWarehouseData(),
                            mFileIO.loadPickingData());
    }

    public void shutdown(){
        mCommHandler.shutdown();
    }


    public void setPickingData(PickingData pickingData){
        mExperiment.setData(pickingData);
    }
    public void setWarehouseData(WarehouseData warehouseData){
        mExperiment.setData(warehouseData);
    }

    public void toggleExperiment(){
        if(mExperiment.isActive()){
            boolean canStop = mExperiment.stop();
            if(canStop) {
                Log.d(TAG, "Stopping Experiment.");
                mUI.onExperimentToggled();
            }
        }else{
            boolean canStart = mExperiment.start();
            if(canStart){
                Log.d(TAG, "Starting Experiment.");
                mUI.onExperimentToggled();
            }
        }
    }

    public void onTap(){
        mExperiment.errorFixed();
    }

    public void onNewScan(String tag){
        mLog("New scan -> "+tag);
        mExperiment.onNewScan(tag);
    }

    @Override
    public void mLog(String msg){
        mUI.mLog(msg);
    }
    public Context getContext(){return mContext;}
    public ViewGroup getExperimentContainer() {
        return mUI.getExperimentContainer();
    }
    public boolean isGlass(){return true;}
    public void onFakeScan(String scan){onNewScan(scan);}
    public void playSound(Utils.SOUNDS sound){
        AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        switch (sound){
            case CLICK:
                audio.playSoundEffect(Sounds.TAP);
                break;
            case SUCCESS:
                audio.playSoundEffect(Sounds.SUCCESS);
                break;
            case ERROR:
                audio.playSoundEffect(Sounds.DISALLOWED);
                break;
        }
    }
}
