package com.thad.rfid_orderpick.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_lib.UIRunnables.SetProgressRunnable;
import com.thad.rfid_lib.UIRunnables.SetTextRunnable;
import com.thad.rfid_orderpick.MobileClient;
import com.thad.rfid_orderpick.R;

import java.util.List;


/**
 * Created by theo on 1/26/18.
 */

public class UserInterfaceHandler {
    private static final String TAG = "|UIHandler|";

    private static int edit_index = -1;
    private boolean experimentRunning;

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

        experimentRunning = false;

        generateDeviceList();
    }

    //COMMANDS
    public void generateDeviceList(){
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
            float[] weights = new float[]{0.1f, 0.5f, 0.2f, 0.2f};
            for (int j = 0; j < fields.length; j++) {
                TextView textView = new TextView(mActivity);

                if (!fields[j].equals("name"))
                    textView.setGravity(Gravity.CENTER);

                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[j]));

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
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mActivity.getResources().getDimension(R.dimen.app_text_size));
                textView.setSingleLine();
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

        mActivity.runOnUiThread(new SetTextRunnable(batteries[index], text));
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
                //LinearLayout deviceList = mActivity.findViewById(R.id.deviceList);
                //LinearLayout experimentBar = mActivity.findViewById(R.id.experiment_bar);
                Button experimentButton = mActivity.findViewById(R.id.experiment_button);
                LinearLayout timer_layout = mActivity.findViewById(R.id.timer_layout);
                TextView username = mActivity.findViewById(R.id.username);
                ProgressBar progressBar = mActivity.findViewById(R.id.progressBar);
                username.setVisibility(View.GONE);
                if(experimentRunning) {
                    experimentButton.setText("START");
                    //deviceList.setVisibility(View.VISIBLE);
                    //experimentBar.setVisibility(View.GONE);
                    timer_layout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    experimentRunning = false;
                }else{
                    experimentButton.setText("STOP");
                    //deviceList.setVisibility(View.GONE);
                    //experimentBar.setVisibility(View.VISIBLE);
                    timer_layout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                    if(mClient.isStudyRunning()) {
                        username.setVisibility(View.VISIBLE);
                        username.setText(mClient.getStudyData().getActiveSubject().getName());
                    }
                    experimentRunning = true;
                }
            }
        });
    }
    public void onTrainingSelected(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView training_button = mActivity.findViewById(R.id.training_button);
                TextView testing_button = mActivity.findViewById(R.id.testing_button);
                training_button.setBackground(mActivity.getResources().getDrawable(R.drawable.red_underline));
                testing_button.setBackgroundColor(Color.BLACK);
            }
        });
    }
    public void onTestingSelected(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView training_button = mActivity.findViewById(R.id.training_button);
                TextView testing_button = mActivity.findViewById(R.id.testing_button);
                testing_button.setBackground(mActivity.getResources().getDrawable(R.drawable.red_underline));
                training_button.setBackgroundColor(Color.BLACK);
            }
        });
    }
    public void onPause(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView pausePlayButton = mActivity.findViewById(R.id.pause_play_button);
                pausePlayButton.setBackground(mActivity.getResources().getDrawable(R.drawable.white_play));
            }
        });
    }
    public void onResume(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView pausePlayButton = mActivity.findViewById(R.id.pause_play_button);
                pausePlayButton.setBackground(mActivity.getResources().getDrawable(R.drawable.white_pause));
            }
        });
    }
    //END OF EVENTS


    public void updateXBandNames(){
        String[] addrs = mClient.getAddresses();
        for(int i = 1 ; i < 3; i++) {
            Prefs.device_names[i] = Prefs.device_names[i]
                    .substring(0, Prefs.device_names[i].length() - 4) + addrs[i].substring(addrs[i].length() - 4);
            mActivity.runOnUiThread(new SetTextRunnable(nameViews[i], Prefs.device_names[i]));
        }
    }

    public void updateConnections(){
        boolean[] connStatus = mClient.getConnStatus();

        for(int i = 0 ; i < Prefs.NUM_DEVICES ; i++){
            String text = "Yes";
            if(!connStatus[i]) text = "No";
            mActivity.runOnUiThread(new SetTextRunnable(connections[i], text));
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

    public void updateProgress(float progress){
        ProgressBar progressBar = mActivity.findViewById(R.id.progressBar);
        mActivity.runOnUiThread(new SetProgressRunnable(progressBar, (int)(progress*100)));
    }


    public void mLog(String text){
        Log.d(TAG, text);
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



    //POP-UPS
    private void editDevicePopup(int device_index){
        LayoutInflater li = LayoutInflater.from(mActivity);
        View promptsView = li.inflate(R.layout.edit_device_popup, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

        alertDialogBuilder.setView(promptsView);

        TextView prompt_text = promptsView.findViewById(R.id.popup_device_prompt_text);
        prompt_text.setText("Edit "+Prefs.device_names[device_index]);

        String[] addrs = mClient.getAddresses();
        final EditText userInput = promptsView.findViewById(R.id.popup_device_input_text);
        userInput.setText(addrs[device_index]);

        edit_index = device_index;

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
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


    public void editLogPopup(){
        LayoutInflater li = LayoutInflater.from(mActivity);
        View promptsView = li.inflate(R.layout.edit_log_popup, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

        alertDialogBuilder.setView(promptsView);

        List<String> names = mClient.getSubjectNames();
        names.add(0, "- Select Subject -");
        final Spinner dropdown = promptsView.findViewById(R.id.subjects_dropdown);
        String[] namesArr = new String[names.size()];
        names.toArray(namesArr);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_dropdown_item, names);
        dropdown.setAdapter(adapter);

        final EditText userInput = promptsView.findViewById(R.id.user_input_name);
        if(mClient.getActiveSubject() != null)
            userInput.setText(mClient.getActiveSubject().getName());

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0)
                    userInput.setText(String.valueOf(dropdown.getSelectedItem()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String username = String.valueOf(userInput.getText());
                                if(username.length() != 0) {
                                    mClient.onSubjectSelected(username);
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                Log.d(TAG, "Dialog canceled.");
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


}
