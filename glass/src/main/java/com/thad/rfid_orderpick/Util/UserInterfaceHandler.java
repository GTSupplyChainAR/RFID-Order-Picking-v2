package com.thad.rfid_orderpick.Util;

import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thad.rfid_orderpick.GlassMainActivity;
import com.thad.rfid_orderpick.R;

/**
 * This Class is responsible for the behavior of all UI elements.
 * It decides when to show the log, the experiment UI etc.
 */

public class UserInterfaceHandler {
    private static final String TAG = "UserInterfaceHandler";

    private static String mText;

    private static TextView glassLog;
    private static ExperimentView experimentView;


    GlassMainActivity mActivity;

    public UserInterfaceHandler(GlassMainActivity activity){
        mActivity = activity;

        experimentView = (ExperimentView)mActivity.findViewById(R.id.experiment_view);
        glassLog = (TextView)experimentView.findViewById(R.id.glassLog);
    }

    //Adds to log with automatic new line.
    public void mLog(String text){
        mLogRaw(text+"\n> ");
    }
    //Adds to log without new line
    public void mLogRaw(String text){
        Log.d(TAG, text);
        mText = text;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentText = (String) glassLog.getText();
                glassLog.setText(currentText+mText);
                ScrollView glassLogContainer = (ScrollView)mActivity.findViewById(R.id.glassLogScrollView);
                glassLogContainer.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    //HELPER FUNCTIONS
    private int dp_to_pixels(float dp){
        return (int) ((dp)*mActivity.getResources().getDisplayMetrics().density +0.5f);
    }

}
