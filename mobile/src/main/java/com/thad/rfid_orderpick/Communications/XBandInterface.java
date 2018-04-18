package com.thad.rfid_orderpick.Communications;

import android.util.Log;

import com.thad.rfid_lib.Static.Prefs;

import de.ubimax.xbandtest.xband.XBandConnectionHandler;
import de.ubimax.xbandtest.xband.XBandEventListener;
import de.ubimax.xbandtest.xband.XBandIMUData;

/**
 * Created by theo on 1/25/18.
 */

public class XBandInterface implements XBandEventListener {
    private static final String TAG = "|XBandInterface|";
    private static int xbandCount = 0;

    private Long lastIMUtimestamp;

    private CommunicationHandler mCommHandler;

    private XBandConnectionHandler xBandHandler;

    private String bluetooth_address;
    private int index;

    private String lastTag;


    public XBandInterface(CommunicationHandler commHandler) {
        mCommHandler = commHandler;
        index = xbandCount;
        xbandCount++;

        if(XBandConnectionHandler.checkDeviceBLESupport()){
            xBandHandler = new XBandConnectionHandler(mCommHandler.getContext());
            xBandHandler.setActivateIMUService(false);
            xBandHandler.setLastConfigMessage(xBandHandler.createConfigurationProperty());
            xBandHandler.registerEventListener(this);
            xBandHandler.setTagTimeoutInSeconds(Prefs.RFID_TIMEOUT);
            xBandHandler.setInitialReaderPower(Prefs.RFID_POWER);
        }else{
            Log.e(TAG, "Device does not support Bluetooth. Failed to create Xband Handlers.");
        }
    }

    public void setAddress(String address){
        bluetooth_address = address;
    }

    public void connect(){
        if (xBandHandler != null) {
            xBandHandler.connectDevice(bluetooth_address);
        }
    }

    public void disconnect(){
        if (xBandHandler != null) {
            xBandHandler.disconnectDevice(bluetooth_address);
        }
    }

    public boolean isConnected(){
        /*Log.d(TAG, "XBand "+index
                +", isConnected-"+xBandHandler.isConnected()
                +", isConnecting-"+xBandHandler.isConnecting()
                +", isSearching-"+xBandHandler.isSearching());*/
        return xBandHandler.isConnected();
    }

    @Override
    public void onNewRFIDScan(String s) {
        lastTag = s;
    }

    @Override
    public void onNewScanRSSI(short i) {
        if(lastTag == null) return;

        mCommHandler.onNewRFIDScan(lastTag, (int)i);
    }

    @Override
    public void onNewBatteryStatus(double v) {
        mCommHandler.onXBandBatteryUpdate(index, v);
    }

    @Override
    public void onTagWriteResponse(String s) {}

    @Override
    public void onNewIMUEntry(XBandIMUData xBandIMUData) {
        if(lastIMUtimestamp == null)
            lastIMUtimestamp = System.currentTimeMillis();


        long curr = System.currentTimeMillis() - lastIMUtimestamp;
        Log.d(TAG, curr+" || XBand "+index+" has new data - "+
        "Accelerometer ("+xBandIMUData.getAccelerometerX()+", "+xBandIMUData.getAccelerometerY() +", "+xBandIMUData.getAccelerometerZ());
        lastIMUtimestamp = System.currentTimeMillis();
    }
}
