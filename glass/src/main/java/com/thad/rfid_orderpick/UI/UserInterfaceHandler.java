package com.thad.rfid_orderpick.UI;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thad.rfid_lib.UIRunnables.SetTextRunnable;
import com.thad.rfid_orderpick.GlassClient;
import com.thad.rfid_orderpick.R;

/**
 * This Class is responsible for the behavior of all UI elements.
 * It decides when to show the log, the experiment UI etc.
 */

public class UserInterfaceHandler {
    private static final String TAG = "|UserInterfaceHandler|";


    private static TextView glassLog, userFriendlyLog;


    private Activity mActivity;
    private GlassClient mClient;

    public UserInterfaceHandler(GlassClient client){
        mActivity = (Activity)client.getContext();
        mClient = client;

        glassLog = (TextView)(mActivity).findViewById(R.id.glassLog);
        userFriendlyLog = (TextView)mActivity.findViewById(R.id.user_friendly_log);
        mActivity.runOnUiThread(new SetTextRunnable(userFriendlyLog, "AWAITING CONNECTION"));
    }


    public void onExperimentStopped(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup log_container = (ViewGroup)mActivity.findViewById(R.id.glassLogLayout);
                Log.d(TAG, "Show log.");
                //log_container.setVisibility(View.VISIBLE);
                userFriendlyLog.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onExperimentStarted(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup log_container = (ViewGroup)mActivity.findViewById(R.id.glassLogLayout);
                Log.d(TAG, "Hiding log.");
                //log_container.setVisibility(View.GONE);
                userFriendlyLog.setVisibility(View.GONE);
            }
        });
    }
    public void onConnected(){
        mActivity.runOnUiThread(new SetTextRunnable(userFriendlyLog, "Ready to START"));
    }

    public ViewGroup getExperimentContainer(){
        return (ViewGroup)mActivity.findViewById(R.id.experiment_view_container);
    }
    public void mLog(String text){
        Log.d(TAG, text);
        mLogRaw(text+"\n> ");
    }
    public void mLogRaw(String text){
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
            if(mobileLogContainer != null)
                mobileLogContainer.fullScroll(View.FOCUS_DOWN);
        }
    }

}
