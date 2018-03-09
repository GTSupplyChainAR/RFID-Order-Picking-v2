package com.thad.rfid_orderpick.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thad.rfid_lib.Decoder;
import com.thad.rfid_lib.Experiment;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_lib.UIRunnables.SetTextRunnable;
import com.thad.rfid_orderpick.MobileClient;
import com.thad.rfid_orderpick.R;

import java.sql.Time;


/**
 * Created by theo on 1/26/18.
 */

public class UserInterfaceHandler {
    private static final String TAG = "|UIHandler|";

    private static int edit_index = -1;

    private Activity mActivity;
    private MobileClient mClient;

    private static TextView mobileLog;
    //private static ExperimentView experiment_view;
    private static TextView[] batteries, connections, nameViews;


    private static UpdateThread updateThread;



    public UserInterfaceHandler(MobileClient client){
        mClient = client;
        mActivity = (Activity)mClient.getContext();


        mobileLog = mActivity.findViewById(R.id.mobileLog);

        nameViews = new TextView[Prefs.NUM_DEVICES];
        batteries = new TextView[Prefs.NUM_DEVICES];
        connections = new TextView[Prefs.NUM_DEVICES];


        setup();
    }

    //COMMANDS
    public void setup(){
        //Populate Device List
        for(int i = 0 ; i < Prefs.NUM_DEVICES ; i++) {
            LinearLayout deviceLayout = new LinearLayout(mActivity);
            deviceLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // Width of TextView
                    LinearLayout.LayoutParams.WRAP_CONTENT)); // Height of TextView);
            deviceLayout.setOrientation(LinearLayout.HORIZONTAL);
            int padding_px = Utils.dp_to_pixels(mActivity, 3);
            deviceLayout.setPadding(padding_px,padding_px,padding_px,padding_px);
            deviceLayout.setId(i);

            String[] fields = new String[]{"no", "name", "conn", "battery"};
            for (int j = 0; j < fields.length; j++) {
                TextView textView = new TextView(mActivity);

                float weight = 2f;
                if (fields[j].equals("name")) weight = 1f;
                else textView.setGravity(Gravity.CENTER);

                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, // Width of TextView
                        LinearLayout.LayoutParams.WRAP_CONTENT, weight)); // Height of TextView);
                switch (fields[j]) {
                    case "no":
                        textView.setText("" + (i + 1));
                        break;
                    case "name":
                        textView.setText(Prefs.device_names[i]);
                        nameViews[i] = textView;
                        break;
                    case "conn":
                        textView.setText("No");
                        connections[i] = textView;
                        break;
                    case "battery":
                        textView.setText("100%");
                        batteries[i] = textView;
                        break;
                }
                padding_px = Utils.dp_to_pixels(mActivity, 7);
                textView.setPadding(padding_px,padding_px,padding_px,padding_px);
                textView.setTextColor(ContextCompat.getColor(mActivity, R.color.black));
                textView.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.dark_white));

                deviceLayout.addView(textView);
            }

            deviceLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLog("Editing device "+Prefs.device_names[v.getId()]);
                    editDevicePopup(v.getId());
                }
            });
            LinearLayout container = (LinearLayout)mActivity.findViewById(R.id.deviceList);
            container.addView(deviceLayout);
        }

    }

    public void updateBattery(int index, double val){
        if(index <= 0 || index >= Prefs.NUM_DEVICES) return;

        val *= 100;
        if(val >= 100) val = 100;
        else if (val <= 0) val = 0;
        String text = Math.round(val)+"%";

        mActivity.runOnUiThread(new UpdateTextRunnable(text, batteries[index]));
    }

    public void startUpdating(){
        updateThread = new UpdateThread();
        updateThread.start();
    }
    //END OF COMMANDS

    //EVENTS
    public void onExperimentToggled(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout deviceList = mActivity.findViewById(R.id.deviceList);
                Button experimentButton = mActivity.findViewById(R.id.experiment_button);
                TextView timer = mActivity.findViewById(R.id.timer);
                if(deviceList.getVisibility() == View.GONE) {
                    experimentButton.setText("START");
                    deviceList.setVisibility(View.VISIBLE);
                    timer.setVisibility(View.GONE);
                }else{
                    experimentButton.setText("STOP");
                    deviceList.setVisibility(View.GONE);
                    timer.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    //END OF EVENTS

    //PRIVATE
    private void editDevicePopup(int device_index){
        LayoutInflater li = LayoutInflater.from(mActivity);
        View promptsView = li.inflate(R.layout.edit_device_popup, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

        alertDialogBuilder.setView(promptsView);

        TextView prompt_text = promptsView.findViewById(R.id.prompt_text);
        prompt_text.setText("Edit "+Prefs.device_names[device_index]);

        String[] addrs = mClient.getAddresses();
        final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);
        userInput.setText(addrs[device_index]);

        edit_index = device_index;

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                String[] addrs = mClient.getAddresses();
                                String answer = String.valueOf(userInput.getText());
                                if (!answer.equals(addrs[edit_index])){
                                    mLog("New address for " + Prefs.device_names[edit_index] + " is " + answer);
                                    mClient.editAddress(edit_index, answer);
                                }
                                edit_index = -1;
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void updateXBandNames(){
        String[] addrs = mClient.getAddresses();
        for(int i = 1 ; i < 3; i++) {
            Prefs.device_names[i] = Prefs.device_names[i]
                    .substring(0, Prefs.device_names[i].length() - 4) + addrs[i].substring(addrs[i].length() - 4);
            mActivity.runOnUiThread(new UpdateTextRunnable(Prefs.device_names[i], nameViews[i]));
        }
    }

    public void updateConnections(){
        boolean[] connStatus = mClient.getConnStatus();

        for(int i = 0 ; i < Prefs.NUM_DEVICES ; i++){
            String text = "Yes";
            if(!connStatus[i]) text = "No";
            mActivity.runOnUiThread(new UpdateTextRunnable(text, connections[i]));
        }
    }

    public void updateTimer(){
        Long experimentElapsedTime = mClient.getExperimentTime();
        TextView timerView = mActivity.findViewById(R.id.timer);
        if(experimentElapsedTime == null){
            mActivity.runOnUiThread(new SetTextRunnable(timerView, "00:00:00"));
        }else {
            mActivity.runOnUiThread(new SetTextRunnable(timerView, Utils.formatTimestamp(experimentElapsedTime)));
        }
    }


    public void mLog(String text){
        Log.d(TAG, "LOG|| "+text);
        mLogRaw(text+"\n> ");
    }
    public void mLogRaw(String text){
        mActivity.runOnUiThread(new AddToLogRunnable(text));
    }



    //GETTERS
    public ViewGroup getExperimentContainer(){
        return mActivity.findViewById(R.id.experiment_view_container);
    }


    //REAL-TIME UPDATERS
    private class UpdateThread extends Thread{
        long update_freq;

        public UpdateThread(){this(500);}
        public UpdateThread(long update_freq){
            this.update_freq = update_freq;
        }

        public void run(){
            while(true) {
                updateConnections();
                updateXBandNames();
                updateTimer();

                try {
                    sleep(update_freq);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UpdateTextRunnable implements Runnable {
        String text;
        View view;

        public UpdateTextRunnable(String text, View view){
            this.text = text;
            this.view = view;
        }

        public void run(){
            if(view instanceof Button){
                Button button = (Button)view;
                button.setText(text);
            }else if (view instanceof TextView){
                TextView textView = (TextView)view;
                textView.setText(text);
            }
        }
    }

    private class AddToLogRunnable implements Runnable{
        String text;

        public AddToLogRunnable(String text){this.text = text;}

        @Override
        public void run() {
            String currentText = (String)mobileLog.getText();
            mobileLog.setText(currentText+text);
            ScrollView mobileLogContainer = (ScrollView)mActivity.findViewById(R.id.mobileLogScrollView);
            mobileLogContainer.fullScroll(View.FOCUS_DOWN);
        }
    }

}
