package com.thad.rfid_orderpick.Util;

import android.util.Log;

import com.thad.rfid_orderpick.MobileMainActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import de.ubimax.xbandtest.xband.XBandConnectionHandler;
import de.ubimax.xbandtest.xband.XBandEventListener;
import de.ubimax.xbandtest.xband.XBandIMUData;

/**
 * Created by theo on 1/25/18.
 */

public class XBandInterface implements XBandEventListener {
    private static final String TAG = "RFID|XBandInterface";

    private MobileMainActivity mMain;

    private XBandConnectionHandler xBandConnection = null;

    private String xband_address;
    private int index;

    private String mTag;

    public XBandInterface(MobileMainActivity context, String address, int index) {
        mMain = context;
        xband_address = address;
        this.index = index;

        if(XBandConnectionHandler.checkDeviceBLESupport()){
            xBandConnection = new XBandConnectionHandler(mMain.getApplicationContext());
            xBandConnection.setActivateIMUService(false);
            xBandConnection.setLastConfigMessage(xBandConnection.createConfigurationProperty());
            xBandConnection.registerEventListener(this);
            xBandConnection.setTagTimeoutInSeconds("2");
            //CHARU INITIAL POWER RFID
            xBandConnection.setInitialReaderPower((byte) 2);
            //xBandConnection.setInitialReaderPower((byte) 1);
        }
    }

    public void connect(){
        if (xBandConnection != null) {
            xBandConnection.connectDevice(xband_address);
        }
    }

    public void disconnect(){
        if (xBandConnection != null) {
            xBandConnection.disconnectDevice(xband_address);
        }
    }

    public boolean isConnected(){
        return xBandConnection.isConnected();
    }

    @Override
    public void onNewRFIDScan(String s) {
        mTag = s;
        //mMain.onNewRFIDScan(s);
    }

    @Override
    public void onNewScanRSSI(short i) {
        mMain.mLog("onNewScan -> "+ mTag + " with strength of: " + i);
    }

    @Override
    public void onNewBatteryStatus(double v) {
        mMain.update_battery(index, (float) v);
    }

    @Override
    public void onTagWriteResponse(String s) {
        mMain.mLog("Tag Write Response -> "+s);
    }

    @Override
    public void onNewIMUEntry(XBandIMUData xBandIMUData) {

    }
}
