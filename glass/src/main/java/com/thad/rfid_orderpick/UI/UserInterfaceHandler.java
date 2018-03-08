package com.thad.rfid_orderpick.UI;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thad.rfid_lib.Data.PickingOrder;
import com.thad.rfid_orderpick.GlassClient;
import com.thad.rfid_orderpick.GlassMainActivity;
import com.thad.rfid_orderpick.R;

/**
 * This Class is responsible for the behavior of all UI elements.
 * It decides when to show the log, the experiment UI etc.
 */

public class UserInterfaceHandler {
    private static final String TAG = "UserInterfaceHandler";


    private static TextView glassLog;


    private Activity mActivity;
    private GlassClient mClient;

    public UserInterfaceHandler(GlassClient client){
        mActivity = (Activity)client.getContext();
        mClient = client;

        glassLog = (TextView)(mActivity).findViewById(R.id.glassLog);
    }


    public void onExperimentToggled(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup log_container = (ViewGroup)mActivity.findViewById(R.id.glassLogLayout);
                if(log_container.getVisibility() == View.GONE) {
                    Log.d(TAG, "Show log.");
                    log_container.setVisibility(View.VISIBLE);
                }else{
                    Log.d(TAG, "Hiding log.");
                    log_container.setVisibility(View.GONE);
                }
            }
        });
    }


    public ViewGroup getExperimentContainer(){
        return (ViewGroup)mActivity.findViewById(R.id.experiment_container);
    }
    public void mLog(String text){
        mLogRaw(text+"\n> ");
    }
    public void mLogRaw(String text){
        Log.d(TAG, text);
        mActivity.runOnUiThread(new AddToLogRunnable(text));
    }

    //RUNNABLES & THREADS
    private class AddToLogRunnable implements Runnable{
        String text;

        public AddToLogRunnable(String text){this.text = text;}

        @Override
        public void run() {
            String currentText = (String)glassLog.getText();
            glassLog.setText(currentText+text);
            ScrollView mobileLogContainer = (ScrollView)mActivity.findViewById(R.id.glassLogScrollView);
            mobileLogContainer.fullScroll(View.FOCUS_DOWN);
        }
    }

}
