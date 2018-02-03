package com.thad.rfid_orderpick.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.thad.rfid_orderpick.R;

/**
 * This Layout Class represents the main UI of the Experiment View.
 */

public class ExperimentView extends RelativeLayout {


    public ExperimentView(Context context) {
        super(context);
        init();
    }

    public ExperimentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExperimentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.experiment_view, this, true);
    }

}