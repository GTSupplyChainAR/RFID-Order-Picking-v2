package com.thad.rfid_orderpick;

import com.thad.rfid_orderpick.Util.CommunicationHandler;
import com.thad.rfid_orderpick.Util.GlassBluetoothInterface;
import com.thad.rfid_orderpick.Util.UserInterfaceHandler;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;


public class GlassMainActivity extends Activity{
    private final static String TAG = "MainActivity";


    private UserInterfaceHandler mUI;

    private GlassBluetoothInterface btInterface;
    private CommunicationHandler mCommHandler;
    public GlassBrain mBrain;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.experiment_activity);

        mUI = new UserInterfaceHandler(this);

        setupBluetooth();

        mCommHandler = new CommunicationHandler(this);
        mBrain = new GlassBrain(mUI);
    }

    private void setupBluetooth() {
        btInterface = new GlassBluetoothInterface(this);
        btInterface.acceptConnection();
    }

    public void onBytesRecieved(final byte[] bytes, int num_bytes) {
        String msg = new String(bytes, 0, num_bytes);

        mCommHandler.decodeMessage(msg);
    }

    public void mLog(String msg){
        mUI.mLog(msg);
    }
}
