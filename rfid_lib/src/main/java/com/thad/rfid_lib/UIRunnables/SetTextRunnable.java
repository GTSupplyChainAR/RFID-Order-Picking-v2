package com.thad.rfid_lib.UIRunnables;

import android.view.View;
import android.widget.TextView;

/**
 * Created by theo on 3/5/18.
 */

public class SetTextRunnable implements Runnable{
    private View view;
    private String text;

    public SetTextRunnable(View view, String text){
        this.view = view;
        this.text = text;
    }
    @Override
    public void run() {
        if(view instanceof TextView)
            ((TextView)view).setText(text);
    }
}
