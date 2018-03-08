package com.thad.rfid_lib.UI;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.thad.rfid_lib.Experiment;
import com.thad.rfid_lib.R;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;

/**
 * Created by theo on 3/6/18.
 */

public class ExperimentViewOverlay extends LinearLayout{
    private static final String TAG = "|ExperimentViewOverlay|";

    //LAYOUT PARAMS
    private static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    private Activity activity;
    private Experiment experiment;

    public ExperimentViewOverlay(Experiment experiment) {
        super(experiment.getContext());
        this.activity = experiment.getActivity();
        this.experiment = experiment;
        init();
    }

    private void init(){
        int screenWidth = Utils.getScreenWidth(activity);
        int height_px = (int)(360f/640f*screenWidth);
        LayoutParams layoutParams = new LayoutParams(MP, height_px);
        this.setLayoutParams(layoutParams);
        this.setBackground(activity.getResources().getDrawable(R.drawable.black_vertical_fade));
    }



}
