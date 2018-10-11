package com.thad.rfid_lib.Static;

/**
 * Created by theo on 2/27/18.
 */

public class Prefs {
    public static String[] device_names = new String[]{"Google Glass", "XBand #6795", "XBand #62D7"};
    public static final int NUM_DEVICES = 3;
    public static final boolean QUICK_START = false;
    public static final boolean RUN_OFFLINE = false;
    public static final int SPEED = 1;
    public static final boolean IS_DEMO = true;

    //DEFAULT ADDRESSES
    //public static final String PHONE_ADDRESS = "3C:BB:FD:27:A0:1E";
    public static final String PHONE_ADDRESS = "B8:D7:AF:98:EB:6D"; //Galaxy Note 8
    public static final String XBAND1_ADDRESS = "B#E076D0916795";
    public static final String XBAND2_ADDRESS = "B#E076D09162D7";
    public static final String GLASS_ADDRESS = "F8:8F:CA:12:E0:A3";
    public static final String GLASS_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    //DEMO
    public static final int[] DEMO_RACK_ROWS = {1, 2};
    public static final int[] DEMO_RACK_COLS = {1, 2, 3};
    public static final int[] DEMO_CART_ROWS = {1};
    public static final int[] DEMO_CART_COLS = {1, 2};

    //XBAND SETTINGS
    public static final String RFID_TIMEOUT = "2";
    public static final byte RFID_POWER = 2;

    //LAYOUTS - EXPERIMENT VIEW
    public static int SCREEN_WIDTH;
    public static final float SCREEN_RATIO = 360f/640f;
    public static final float RACK_SCREEN_PERCENTAGE = .5f;
    public static final float RACK_TAG_PERCENTAGE = 0.15f;
    public static final float TITLE_CART_PERCENTAGE = 0.8f;
    public static final int ITEM_IMG_SIZE = 70;

    public static final float FADED_RACKS_PERCENTAGE = .15f;
    public static final int FADED_GLOW_DURATION = 3000; // in milliseconds
    public static final boolean REVERSED_FADED_RACKS = false;

    //Percentages of cell height
    public static final float TEXT_SIZE_TAG = 0.15f;
    public static final float TEXT_SIZE_TAG_GLASS = 0.3f;
    public static final float TEXT_SIZE_CELL = 0.21f;
    public static final float TEXT_SIZE_CELL_GLASS = 0.37f;
    public static final float CART_TEXT_BOTTOM_OFFSET = 0.03f;

    //ID of logs and subjects
    public static final int ID_DIGIT_NUM = 4;

    //FILENAMES
    public static final String EXPERIMENT_LOGS_FOLDER = "experiment_logs";
    public static final String RUN_LOGS_FOLDER = "run_logs";
    public static final String USER_STUDY_FILENAME = "user_study.json";
    public static final String WAREHOUSE_DATA_FILENAME = "warehouse.json";
    public static final String PICK_DATA_TESTING_FILENAME = "tasks-pick-by-hud_rfid-testing.json";
    public static final String PICK_DATA_TRAINING_FILENAME = "tasks-pick-by-hud_rfid-training.json";
}
