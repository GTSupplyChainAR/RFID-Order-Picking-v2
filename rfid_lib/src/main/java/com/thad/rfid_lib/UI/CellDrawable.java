package com.thad.rfid_lib.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import com.thad.rfid_lib.R;
import com.thad.rfid_lib.Static.Utils;

/**
 * Created by theo on 3/4/18.
 */

public class CellDrawable {
    private static final String TAG = "|CellDrawable|";
    private static int border_width, fill_padding, border_radii;

    private enum STATES {EMPTY, FILLED, CROSSED, CHECKED}

    private Activity activity;
    private CellUI cellUI;

    private GradientDrawable borderDrawable, fillDrawable;
    private LayerDrawable layerDrawable;
    private Drawable redCross, redArrow, arrowColSymbol, greenCheck;

    private STATES state;
    private int color;
    boolean isLeftEdge = false, isRightEdge = false;

    public CellDrawable(Context context, CellUI cellUI) {
        this.activity = (Activity) context;
        this.cellUI = cellUI;
        this.color = cellUI.getColorId();

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

        int[] pos = Utils.tagToPos(cellUI.getTag());
        arrowColSymbol = context.getResources().getDrawable(R.drawable.symbol_col_default);

        isRightEdge = false; isLeftEdge = false;
        if (pos[1] == 0) {
            isLeftEdge = true;
            arrowColSymbol = context.getResources().getDrawable(R.drawable.symbol_col_left);
        } else if (pos[1] + 1 == cellUI.getExperiment().getWarehouseData().
                getUnitByTag(Utils.tagToLetter(cellUI.getTag()))
                .getDimensions()[1]) {
            isRightEdge = true;
            arrowColSymbol = context.getResources().getDrawable(R.drawable.symbol_col_right);
        }

        empty();
    }

    public void fill(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATES.FILLED;
                fillDrawable.setColor(activity.getResources().getColor(color));
                Drawable[] layers = {borderDrawable, fillDrawable, arrowColSymbol};
                layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                layerDrawable.setLayerInset(1, fill_padding, fill_padding, fill_padding, fill_padding);

                int w = cellUI.getDims()[0], h = cellUI.getDims()[1];
                arrowColSymbol.setAlpha(250);
                layerDrawable.setLayerInset(2, (int)(0.7f*w), (int)(0.6f* h), 0, 0);
                if(isLeftEdge)
                    layerDrawable.setLayerInset(2, 0, (int)(0.6f* h), (int)(0.7f*w), 0);
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
                int width_offset = (int)(cellUI.getDims()[0] * 0.6f / 2.f);
                int height_offset = (int)(width_offset*0.75);
                layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                layerDrawable.setLayerInset(1, width_offset, height_offset, width_offset, height_offset);
            }
        });
    }

    public void addCheck(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state = STATES.CHECKED;
                fillDrawable.setColor(activity.getResources().getColor(R.color.dark_gray));
                Drawable[] layers = {borderDrawable, fillDrawable, arrowColSymbol};
                layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerInset(0, 0, 0, 0, 0);
                layerDrawable.setLayerInset(1, fill_padding, fill_padding, fill_padding, fill_padding);

                arrowColSymbol.setAlpha(70);
                int w = cellUI.getDims()[0], h = cellUI.getDims()[1];
                layerDrawable.setLayerInset(2, (int)(0.7f*w), (int)(0.6f* h), 0, 0);
                if(isLeftEdge)
                    layerDrawable.setLayerInset(2, 0, (int)(0.6f* h), (int)(0.7f*w), 0);
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
