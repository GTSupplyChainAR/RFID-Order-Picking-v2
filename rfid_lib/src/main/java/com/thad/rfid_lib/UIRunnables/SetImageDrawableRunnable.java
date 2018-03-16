package com.thad.rfid_lib.UIRunnables;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;

/**
 * Created by theo on 3/13/18.
 */

public class SetImageDrawableRunnable implements Runnable {
    private ImageView imageView;
    private LayerDrawable layerDrawable;

    public SetImageDrawableRunnable(ImageView imageView, Drawable[] layers, int[][] paddings){
        this.imageView = imageView;

        layerDrawable = new LayerDrawable(layers);
        for(int i = 0; i < layers.length ; i++)
            layerDrawable.setLayerInset(i, paddings[i][0], paddings[i][1], paddings[i][2], paddings[i][3]);
    }
    public SetImageDrawableRunnable(ImageView imageView, Drawable drawable){
        this.imageView = imageView;
        layerDrawable = (LayerDrawable)drawable;
    }

    @Override
    public void run() {
        imageView.setBackground(layerDrawable);
    }
}
