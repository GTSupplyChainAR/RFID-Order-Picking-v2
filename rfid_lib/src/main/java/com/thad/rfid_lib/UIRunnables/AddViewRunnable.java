package com.thad.rfid_lib.UIRunnables;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by theo on 3/5/18.
 */

public class AddViewRunnable implements Runnable{
    private ViewGroup viewGroup;
    private View view;

    public AddViewRunnable(ViewGroup viewGroup, View view){
        this.viewGroup = viewGroup;
        this.view = view;
    }
    @Override
    public void run() {
        viewGroup.addView(view);
    }
}
