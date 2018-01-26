package com.thad.rfid_orderpick;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


/**
 * Created by theo on 1/26/18.
 */

public class UserInterfaceHandler {
    private static final String TAG = "RFID|UIHandler";

    public static String[] device_names = new String[]{"Google Glass", "XBand #0000", "XBand #0000"};
    private static int edit_index = -1;

    private MobileMainActivity mActivity;
    private static TextView mobileLog;

    private TextView[] batteries, connections, nameViews;

    private static String mText;

    public UserInterfaceHandler(MobileMainActivity context){
        mActivity = context;

        mobileLog = (TextView)mActivity.findViewById(R.id.mobileLog);

        nameViews = new TextView[device_names.length];
        batteries = new TextView[device_names.length];
        connections = new TextView[device_names.length];
    }

    public void setup(){
        //Populate Device List
        for(int i = 0 ; i < device_names.length ; i++) {
            LinearLayout deviceLayout = new LinearLayout(mActivity);
            deviceLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // Width of TextView
                    LinearLayout.LayoutParams.WRAP_CONTENT)); // Height of TextView);
            deviceLayout.setOrientation(LinearLayout.HORIZONTAL);
            int padding_px = dp_to_pixels(3);
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
                        textView.setText(device_names[i]);
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
                padding_px = dp_to_pixels(7);
                textView.setPadding(padding_px,padding_px,padding_px,padding_px);
                textView.setTextColor(ContextCompat.getColor(mActivity, R.color.black));
                textView.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.light_gray));

                deviceLayout.addView(textView);
            }

            deviceLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLog("Editing device "+device_names[v.getId()]);
                    editDevicePopup(v.getId());
                }
            });
            LinearLayout container = (LinearLayout)mActivity.findViewById(R.id.deviceList);
            container.addView(deviceLayout);
        }

        updateXBandNames();
    }

    public void update_battery(int index, float val){
        if(val >= 100) val = 100;
        else if (val <= 0) val = 0;
        String perc_val = Math.round(val)+"%";
        batteries[index].setText(perc_val);
    }

    private void editDevicePopup(int index){

        LayoutInflater li = LayoutInflater.from(mActivity);
        View promptsView = li.inflate(R.layout.edit_device_popup, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

        alertDialogBuilder.setView(promptsView);

        TextView prompt_text = promptsView.findViewById(R.id.prompt_text);
        prompt_text.setText("Edit "+device_names[index]);

        final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);
        userInput.setText(mActivity.getAddress(index));

        edit_index = index;

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                String answer = String.valueOf(userInput.getText());
                                if (!answer.equals(mActivity.getAddress(edit_index))){
                                    mLog("New address for " + device_names[edit_index] + " is " + answer);
                                    mActivity.editAddress(edit_index, answer);
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
        String xband_addr = mActivity.getAddress(1);
        device_names[1] = device_names[1].substring(0,device_names[1].length()-4)+xband_addr.substring(xband_addr.length()-4);
        nameViews[1].setText(device_names[1]);
        xband_addr = mActivity.getAddress(2);
        device_names[2] = device_names[2].substring(0,device_names[2].length()-4)+xband_addr.substring(xband_addr.length()-4);
        nameViews[2].setText(device_names[2]);
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
                String currentText = (String) mobileLog.getText();
                mobileLog.setText(currentText+mText);
                ScrollView mobileLogContainer = (ScrollView)mActivity.findViewById(R.id.mobileLogScrollView);
                mobileLogContainer.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    //HELPER FUNCTIONS
    private int dp_to_pixels(float dp){
        return (int) ((dp)*mActivity.getResources().getDisplayMetrics().density +0.5f);
    }
}
