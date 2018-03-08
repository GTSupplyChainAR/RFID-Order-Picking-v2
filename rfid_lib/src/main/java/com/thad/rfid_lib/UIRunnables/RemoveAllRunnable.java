package com.thad.rfid_lib.UIRunnables;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by theo on 3/5/18.
 */

public class RemoveAllRunnable implements Runnable{
    private ViewGroup viewGroup;

    public RemoveAllRunnable(ViewGroup viewGroup){
        this.viewGroup = viewGroup;
    }
    @Override
    public void run() {
        viewGroup.removeAllViews();
    }
}
