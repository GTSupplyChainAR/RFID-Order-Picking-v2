package com.thad.rfid_lib.UI;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;

import com.thad.rfid_lib.R;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.UIRunnables.SetImageDrawableRunnable;

/**
 * Created by theo on 3/13/18.
 */

public class FadedCell extends ImageView{
    private static final int transparent = Color.argb(0,0,0,0);

    private static int border_width, fill_padding, border_radii;
    private int[] inset_layer_fill, inset_layer_glow;

    private Activity activity;

    private GradientDrawable borderDrawable, borderFillDrawable, glowDrawable;
    private LayerDrawable layerDrawable;


    public FadedCell(Context context, ShelvingUnitUI.NEIGHBOR neighbor){
        super(context);
        this.activity = (Activity)context;

        int colorStart = context.getResources().getColor(R.color.faded_rack_gray);

        border_width = (int) context.getResources().getDimension(R.dimen.cell_border_width);
        fill_padding = (int) context.getResources().getDimension(R.dimen.fill_padding_width);
        border_radii = (int) context.getResources().getDimension(R.dimen.corners);

        GradientDrawable.Orientation orientation;
        if ((neighbor == ShelvingUnitUI.NEIGHBOR.LEFT && Prefs.REVERSED_FADED_RACKS)
                    || (neighbor == ShelvingUnitUI.NEIGHBOR.RIGHT && !Prefs.REVERSED_FADED_RACKS)){
            orientation = GradientDrawable.Orientation.LEFT_RIGHT;
        }else{
            orientation = GradientDrawable.Orientation.RIGHT_LEFT;
        }

        if ((neighbor == ShelvingUnitUI.NEIGHBOR.LEFT && !Prefs.REVERSED_FADED_RACKS)
                || (neighbor == ShelvingUnitUI.NEIGHBOR.RIGHT && Prefs.REVERSED_FADED_RACKS)){
            inset_layer_fill = new int[]{0, border_width, border_width, border_width};
            inset_layer_glow = new int[]{0, fill_padding, fill_padding, fill_padding};
        }else{
            inset_layer_fill = new int[]{border_width, border_width, 0, border_width};
            inset_layer_glow = new int[]{fill_padding, fill_padding, 0, fill_padding};
        }

        borderDrawable = new GradientDrawable();
        borderDrawable.setOrientation(orientation);
        borderDrawable.setColors(new int[]{colorStart, transparent});

        borderFillDrawable = new GradientDrawable();
        borderFillDrawable.setColor(Color.BLACK);

        glowDrawable = new GradientDrawable();
        glowDrawable.setOrientation(orientation);
        glowDrawable.setColors(new int[]{transparent, transparent});

        setCornerRadii(border_radii);

        empty();
    }

    public void empty() {
        activity.runOnUiThread(new SetImageDrawableRunnable(this,
                new Drawable[]{borderDrawable, borderFillDrawable, glowDrawable},
                new int[][]{{0, 0, 0, 0},inset_layer_fill, inset_layer_glow}));
    }

    public void glow(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                glowDrawable.setAlpha(255);
                glowDrawable.setColors(new int[]{Color.RED, transparent});

                ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(glowDrawable, PropertyValuesHolder.ofInt("alpha", 0));
                animator.setTarget(glowDrawable);
                animator.setDuration(Prefs.FADED_GLOW_DURATION);
                animator.start();
            }
        });
    }

    private void setCornerRadii(int corner_radius){
        float[] corner_arr = new float[]{corner_radius, corner_radius, corner_radius, corner_radius,
                corner_radius, corner_radius, corner_radius, corner_radius};
        borderDrawable.setCornerRadii(corner_arr);
        borderFillDrawable.setCornerRadii(corner_arr);
        glowDrawable.setCornerRadii(corner_arr);
    }

    public Drawable getDrawable(){return layerDrawable;}

}
