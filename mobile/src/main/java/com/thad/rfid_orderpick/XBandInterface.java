package com.thad.rfid_orderpick;

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


    public XBandInterface(MobileMainActivity context, String address, int index) {
        mMain = context;
        xband_address = address;
        this.index = index;

        if(XBandConnectionHandler.checkDeviceBLESupport()){
            xBandConnection = new XBandConnectionHandler(mMain.getApplicationContext());
            xBandConnection.setActivateIMUService(false);
            xBandConnection.setLastConfigMessage(xBandConnection.createConfigurationProperty());
            xBandConnection.registerEventListener(this);
        }
    }

    public void connect(){
        if (xBandConnection == null) {
            mMain.mLog(" failed.");
            return;
        }
        xBandConnection.connectDevice(xband_address);
        mMain.mLog(" done.");
    }

    @Override
    public void onNewRFIDScan(String s) {
        mMain.mLog("onNewRFIDScan -> "+s);
    }

    @Override
    public void onNewScanRSSI(short i) {
        mMain.mLog("onNewRSSIScan -> "+i);
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
