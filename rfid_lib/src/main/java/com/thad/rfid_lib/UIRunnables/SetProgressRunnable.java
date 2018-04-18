package com.thad.rfid_lib.UIRunnables;

import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by theo on 3/5/18.
 */

public class SetProgressRunnable implements Runnable{
    private View view;
    private int progress;

    public SetProgressRunnable(View view, int progress){
        this.view = view;
        this.progress = progress;
    }
    @Override
    public void run() {
        if(!(view instanceof ProgressBar))
            return;
        ((ProgressBar) view).setProgress(progress);
    }
}
