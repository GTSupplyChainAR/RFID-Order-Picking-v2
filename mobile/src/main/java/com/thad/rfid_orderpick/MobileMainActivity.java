package com.thad.rfid_orderpick;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.thad.rfid_orderpick.Util.FileIO;
import com.thad.rfid_orderpick.Util.GlassBluetoothInterface;
import com.thad.rfid_orderpick.Util.UserInterfaceHandler;
import com.thad.rfid_orderpick.Util.XBandInterface;


public class MobileMainActivity extends AppCompatActivity{
    private static final String TAG = "RFID|MainActivity";

    private enum MSG_CODES { DATA, SCAN, GAME, SPLIT }

    private static final boolean QUICK_START = false;
    private static final String PREFS_NAME = "PREFS";

    public static UserInterfaceHandler mUI;

    private static String glass_bluetooth_addrs;

    private static GlassBluetoothInterface mGlassInterface;
    private static XBandInterface[] mXBandInterface;
    private static String[] xband_bluetooth_addrs = new String[2];

    private static FileIO mFileIO;
    public static MobileBrain mBrain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        glass_bluetooth_addrs = settings.getString("glass_bluetooth_addrs", "F8:8F:CA:12:E0:A3");
        xband_bluetooth_addrs[0] = settings.getString("xband_bluetooth_addrs1", "B#E076D0916795");
        xband_bluetooth_addrs[1] = settings.getString("xband_bluetooth_addrs2", "B#E076D09162D7");


        mUI = new UserInterfaceHandler(this);
        mUI.setup(); //populate device list
        mUI.startUpdating();

        requestRequiredPermissions();

        mLog("Initializing Application...");
        mFileIO = new FileIO(this);
        mBrain = new MobileBrain(this);

        mGlassInterface = new GlassBluetoothInterface(this);
        mGlassInterface.setGlassAddress(glass_bluetooth_addrs);
        mXBandInterface = new XBandInterface[]{new XBandInterface(this, xband_bluetooth_addrs[0], 0),
                                                  new XBandInterface(this, xband_bluetooth_addrs[1], 1)};

        if(QUICK_START){
            reconnect();
            syncData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    public void onConnect(View view){
        reconnect();
    }
    public void reconnect(){
        boolean[] states = checkConnections();


        mLog("Reconnecting with "+mUI.device_names[0]+"...");
        mGlassInterface = new GlassBluetoothInterface(this);
        mGlassInterface.connect();

        for(int i = 0 ; i < 2 ; i++){
            if(!states[i+1]) {
                //mLog("Attempting to connect with "+mUI.device_names[i+1]+"...");
                //mXBandInterface[i] = new XBandInterface(this, xband_bluetooth_addrs[i], i);
                //mXBandInterface[i].connect();
            }
        }
    }

    public void onSyncData(View view){
        syncData();
    }
    public void syncData(){
        boolean hasExperimentData = mFileIO.loadExperimentData();
        boolean hasPickData = mFileIO.loadPickData();

        if(hasExperimentData && hasPickData){
            mLog("Loaded data from memory.");
            boolean[] states = checkConnections();
            if(states[0]){
                String msg = MSG_CODES.DATA.toString() + mFileIO.getExperimentJSON() + MSG_CODES.SPLIT.toString() + mFileIO.getPickDataJSON();
                mGlassInterface.sendString(msg);
                mLog("Data sent to Glass.");
            }else{
                mLog("Failed to send data to Glass.");
            }
        }else{
            mLog("Did not find data in memory, failed to get from server");
        }
    }

    public void onExperimentClicked(View view){

        if(!mBrain.isExperimentRunning()){
            boolean startedExperiment = mBrain.startExperiment();

            mGlassInterface.sendString(MSG_CODES.GAME.toString()+"START");

            if(startedExperiment){
                mLog("Initiating Experiment.");
            }else{
                mLog("Experiment is not yet ready to start.");
            }
        }else{
            mBrain.stopExperiment();
            mGlassInterface.sendString(MSG_CODES.GAME.toString()+"STOP");
            mLog("Stopping the Experiment.");
        }
    }

    public void onNewRFIDScan(String scan){
        mLog("New RFID scan -> "+scan);
        mGlassInterface.sendString(MSG_CODES.SCAN.toString()+scan);
    }

    public void update_battery(int index, float v){
        mUI.update_battery(index+1, v);
    }

    public void update_connections(){
        boolean[] states = checkConnections();
        for(int i = 0 ; i < 3 ; i++){
            mUI.update_connection(i, states[i]);
        }
    }

    public boolean[] checkConnections() {
        boolean[] states = {false, false, false};
        if (mGlassInterface != null && mGlassInterface.isConnected()){
            states[0] = true;
        }
        if(mXBandInterface != null && mXBandInterface.length == 2) {
            states[1] =  mXBandInterface[0].isConnected();
            states[2] =  mXBandInterface[1].isConnected();
        }
        //Log.d(TAG, "Connection States -> "+states[0]+", "+states[1]+", "+states[2]);
        return states;
    }



    public void editAddress(int index, String new_address){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String pref_name = "";
        switch (index){
            case 0:
                pref_name = "glass_bluetooth_addrs";
                glass_bluetooth_addrs = new_address;
                break;
            case 1:
                pref_name = "xband_bluetooth_addrs1";
                xband_bluetooth_addrs[0] = new_address;
                break;
            case 2:
                pref_name = "xband_bluetooth_addrs2";
                xband_bluetooth_addrs[1] = new_address;
                break;
        }
        editor.putString(pref_name, new_address);
        editor.commit();
        mUI.updateXBandNames();
    }

    public String getAddress(int index){
        switch(index){
            case 0: return glass_bluetooth_addrs;
            case 1: return xband_bluetooth_addrs[0];
            case 2: return xband_bluetooth_addrs[1];
        }
        mLog("There is no address for a device with index of "+index);
        return null;
    }

    public void mLog(String str){mUI.mLog(str);}
    public void mLogRaw(String str){mUI.mLogRaw(str);}


    private void requestRequiredPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_PRIVILEGED)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED}, 0);
        }
    }
}
