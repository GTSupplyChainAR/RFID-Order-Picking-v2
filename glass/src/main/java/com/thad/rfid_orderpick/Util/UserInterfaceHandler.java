package com.thad.rfid_orderpick.Util;

import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thad.rfid_orderpick.ExperimentData;
import com.thad.rfid_orderpick.GlassBrain;
import com.thad.rfid_orderpick.GlassMainActivity;
import com.thad.rfid_orderpick.PickingOrder;
import com.thad.rfid_orderpick.PickingTask;
import com.thad.rfid_orderpick.R;

import static android.view.View.GONE;

/**
 * This Class is responsible for the behavior of all UI elements.
 * It decides when to show the log, the experiment UI etc.
 */

public class UserInterfaceHandler {
    private static final String TAG = "UserInterfaceHandler";


    private static TextView glassLog;
    private static ExperimentView experimentView;


    private GlassMainActivity mActivity;
    private GlassBrain mBrain;

    public UserInterfaceHandler(GlassMainActivity activity){
        mActivity = activity;
        mBrain = mActivity.mBrain;

        experimentView = (ExperimentView)mActivity.findViewById(R.id.experiment_view);
        glassLog = (TextView)mActivity.findViewById(R.id.glassLog);

    }

    public void startExperiment(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                experimentView.setVisibility(View.VISIBLE);
                View logLayout = mActivity.findViewById(R.id.glassLogLayout);
                logLayout.setVisibility(View.GONE);
            }
        });
    }

    public void stopExperiment(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                experimentView.setVisibility(View.GONE);
                View logLayout = mActivity.findViewById(R.id.glassLogLayout);
                logLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    //ACTIVE
    public void clearBin(String tag){
        mActivity.runOnUiThread(new ExperimentRunnable(tag, "clear"));
    }
    public void wrongBin(String tag){
        mActivity.runOnUiThread(new ExperimentRunnable(tag, "wrong"));
    }
    public void newOrder(PickingOrder pickingOrder){
        mActivity.runOnUiThread(new ExperimentRunnable(pickingOrder));
    }
    public void setCartMode(String cart_tag, int num_items){
        mActivity.runOnUiThread(new ExperimentRunnable(cart_tag, num_items));
    }
    public void setErrorMode(PickingOrder pickingOrder, String tag){
        mActivity.runOnUiThread(new ExperimentRunnable(pickingOrder, tag));
    }

    // I/Os
    public void setData(ExperimentData experimentData){
        mActivity.runOnUiThread(new ExperimentRunnable(experimentData));
    }

    //Logs.
    public void mLog(String text){
        mLogRaw(text+"\n> ");
    }
    public void mLogRaw(String text){
        Log.d(TAG, text);
        mActivity.runOnUiThread(new AddToLogRunnable(text));
    }

    //HELPER FUNCTIONS
    private int dp_to_pixels(float dp){
        return (int) ((dp)*mActivity.getResources().getDisplayMetrics().density +0.5f);
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

    private class ExperimentRunnable implements Runnable{
        ExperimentData experimentData;
        String tag, tag_command, cart_tag;
        int num_items;
        PickingOrder pickingOrder;


        public ExperimentRunnable(ExperimentData experimentData){
            this.experimentData = experimentData;
        }
        public ExperimentRunnable(String tag, String tag_command){
            this.tag = tag;
            this.tag_command = tag_command;
        }
        public ExperimentRunnable(String cart_tag, int num_items){
            this.cart_tag = cart_tag;
            this.num_items = num_items;
        }
        public ExperimentRunnable(PickingOrder pickingOrder){
            this.pickingOrder = pickingOrder;
        }
        public ExperimentRunnable(PickingOrder pickingOrder, String tag){
            this.pickingOrder = pickingOrder;
            this.tag = tag;
        }

        @Override
        public void run() {
            if(experimentData != null)
                experimentView.createUI(experimentData);
            else if(pickingOrder != null && tag != null)
                experimentView.setErrorMode(pickingOrder, tag);
            else if(pickingOrder != null)
                experimentView.newOrder(pickingOrder);
            else if(tag != null && tag_command != null){
                if(tag_command.equals("clear"))
                    experimentView.clearBin(tag);
                else if (tag_command.equals("wrong")){
                    experimentView.wrongBin(tag);
                }
            }else if(cart_tag != null){
                experimentView.setCartMode(cart_tag, num_items);
            }
        }
    }
}
