package com.thad.rfid_orderpick;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MobileMainActivity extends AppCompatActivity{
    private static final String TAG = "RFID|MainActivity";
    private static final String PREFS_NAME = "PREFS";

    UserInterfaceHandler mUI;

    private static String glass_bluetooth_addrs;

    XBandInterface[] mXBandInterface;
    private static String[] xband_bluetooth_addrs = new String[2];
    private int sel_xband = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_main);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        glass_bluetooth_addrs = settings.getString("glass_bluetooth_addrs", "F8:8F:CA:12:E0:A3");
        xband_bluetooth_addrs[0] = settings.getString("xband_bluetooth_addrs1", "B#E076D0916795");
        xband_bluetooth_addrs[1] = settings.getString("xband_bluetooth_addrs2", "B#E076D09162D7");


        mUI = new UserInterfaceHandler(this);
        mUI.setup(); //populate device list

        requestRequiredPermissions();


        mLog("Initializing Application...");

        mXBandInterface = new XBandInterface[]{new XBandInterface(this, xband_bluetooth_addrs[0], 0),
                                                  new XBandInterface(this, xband_bluetooth_addrs[1], 1)};
    }

    public void onConnect(View view){
        //Disconnect everything.

        mLog("Connecting devices...");
        mLogRaw("1. Connecting "+mUI.device_names[0]+"...");
        //Connect Glass
        mLog(" done.");
        mLogRaw("2. Connecting "+mUI.device_names[1]+"...");
        //mXBandInterface[0].connect();
        mLog(" done.");
        mLogRaw("3. Connecting "+mUI.device_names[2]+"...");
        //mXBandInterface[1].connect();
        mLog(" done.");
    }
    public void onGetData(View view){
        mLogRaw("Receiving Picking Data from server...");
        mLog(" done.");
    }
    public void onSendData(View view){
        mLogRaw("Sending Picking Data to HwD...");
        mLog(" done.");
    }

    public void update_battery(int index, float v){
        mUI.update_battery(index+1, v);
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
