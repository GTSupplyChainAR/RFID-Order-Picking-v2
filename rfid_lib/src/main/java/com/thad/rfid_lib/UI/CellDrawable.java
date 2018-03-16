package com.thad.rfid_lib.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import com.thad.rfid_lib.R;

/**
 * Created by theo on 3/4/18.
 */

public class CellDrawable {
    private static int border_width, fill_padding, border_radii;

    private enum STATES {EMPTY, FILLED, CROSSED, CHECKED}

    private Activity activity;

    private GradientDrawable borderDrawable, fillDrawable;
    private LayerDrawable layerDrawable;
    private Drawable redCross, redArrow, greenCheck;

    private STATES state;
    private int color;

    public CellDrawable(Context context, int color){
        this.activity = (Activity)context;
        this.color = color;

        border_width = (int) context.getResources().getDimension(R.dimen.cell_border_width);
        fill_padding = (int) context.getResources().getDimension(R.dimen.fill_padding_width);
        border_radii = (int) context.getResources().getDimension(R.dimen.corners);

        borderDrawable = new GradientDrawable();
        borderDrawable.setStroke(border_width, context.getResources().getColor(color));

        fillDrawable = new GradientDrawable();
        fillDrawable.setColor(context.getResources().getColor(color));

        setCornerRadii(border_radii);

        redArrow = context.getResources().getDrawable(R.drawable.arrow_red);
        redCross = context.getResources().getDrawable(R.drawable.red_cross);
        greenCheck = context.getResources().getDrawable(R.drawable.green_check);

        empty();
    }

    public void fill(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATES.FILLED;
                fillDrawable.setColor(activity.getResources().getColor(color));
                Drawable[] layers = {borderDrawable, fillDrawable};
                layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                layerDrawable.setLayerInset(1, fill_padding, fill_padding, fill_padding, fill_padding);
            }
        });
    }

    public void empty(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATES.EMPTY;
                Drawable[] layers = {borderDrawable};
                layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerInset(0, 0, 0, 0, 0);
            }
        });
    }

    public void toggleRedArrow(){
        if (state == STATES.CROSSED){
            empty();
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATES.CROSSED;
                Drawable[] layers = {borderDrawable, redArrow};
                layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                layerDrawable.setLayerInset(1, 0, 0, 0, 25);
            }
        });
    }

    public void toggleCross() {
        if (state == STATES.CROSSED){
            empty();
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATES.CROSSED;
                Drawable[] layers = {borderDrawable, redCross};
                layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                layerDrawable.setLayerInset(1, 25, 20, 25, 20);
            }
        });
    }

    public void addCheck(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATES.CHECKED;
                fillDrawable.setColor(activity.getResources().getColor(R.color.dark_gray));
                Drawable[] layers = {borderDrawable, fillDrawable};
                layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                layerDrawable.setLayerInset(1, fill_padding, fill_padding, fill_padding, fill_padding);
            }
        });
    }

    private void setCornerRadii(int corner_radius){
        borderDrawable.setCornerRadii(new float[]{corner_radius, corner_radius, corner_radius, corner_radius,
                corner_radius, corner_radius, corner_radius, corner_radius});
        fillDrawable.setCornerRadii(new float[]{corner_radius, corner_radius, corner_radius, corner_radius,
                corner_radius, corner_radius, corner_radius, corner_radius});
    }

    public Drawable getDrawable(){
        return layerDrawable;
    }
}
