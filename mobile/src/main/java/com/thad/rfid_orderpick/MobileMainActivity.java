package com.thad.rfid_orderpick;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_orderpick.Communications.ClientBluetooth;
import com.thad.rfid_orderpick.Communications.XBandInterface;
import com.thad.rfid_orderpick.UI.UserInterfaceHandler;


public class MobileMainActivity extends AppCompatActivity{
    private static final String TAG = "|MainActivity|";

    private static final String PREFS_NAME = "PREFS";

    public static MobileClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String[] addrs = new String[3];
        addrs[0] = settings.getString("glass_bluetooth_addrs", "F8:8F:CA:12:E0:A3");
        addrs[1] = settings.getString("xband_bluetooth_addrs0", "B#E076D0916795");
        addrs[2] = settings.getString("xband_bluetooth_addrs1", "B#E076D09162D7");

        requestRequiredPermissions();
        Prefs.SCREEN_WIDTH = Utils.getScreenWidth(this);

        mClient = new MobileClient(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public void onDestroy(){
        mClient.shutdown();
        super.onDestroy();
    }


    private void requestRequiredPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 0);
        }
        /*
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_PRIVILEGED)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED}, 0);
        }*/
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }
    }


    public void onConnect(View view){Log.d(TAG, "onConnectClicked"); mClient.onConnect(); }
    public void onDisconnect(View view){ Log.d(TAG, "onDisconnectClicked"); mClient.onDisconnect(); }
    public void onExperimentClicked(View view){ Log.d(TAG, "onExperimentClicked"); mClient.onExperimentClicked(); }
    public void onLogClicked(View view){Log.d(TAG, "onLogClicked"); mClient.onLogClicked();}
    public void onTrainingClicked(View v){Log.d(TAG, "onTrainingClicked"); mClient.onTrainingClicked();}
    public void onTestingClicked(View v){Log.d(TAG, "onTestingClicked"); mClient.onTestingClicked();}




}
