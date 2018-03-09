package com.thad.rfid_lib.Static;

/**
 * Created by theo on 2/27/18.
 */

public class Prefs {
    public static String[] device_names = new String[]{"Google Glass", "XBand #6795", "XBand #62D7"};
    public static final int NUM_DEVICES = 3;
    public static final boolean QUICK_START = true;

    //DEFAULT ADDRESSES
    public static final String PHONE_ADDRESS = "3C:BB:FD:27:A0:1E";
    public static final String GLASS_ADDRESS = "F8:8F:CA:12:E0:A3";
    public static final String XBAND1_ADDRESS = "B#E076D0916795";
    public static final String XBAND2_ADDRESS = "B#E076D09162D7";
    public static final String GLASS_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    //XBAND SETTINGS
    public static final String RFID_TIMEOUT = "2";
    public static final byte RFID_POWER = 2;

    //LAYOUTS - EXPERIMENT VIEW
    public static int SCREEN_WIDTH;
    public static final float SCREEN_RATIO = 360f/640f;
    public static final float RACK_SCREEN_PERCENTAGE = .5f;
    public static final float FADED_RACKS_PERCENTAGE = .15f;
    public static final float RACK_TAG_PERCENTAGE = 0.15f;
    public static final float TITLE_CART_PERCENTAGE = 0.8f;
    public static final int ITEM_IMG_SIZE = 70;
}
