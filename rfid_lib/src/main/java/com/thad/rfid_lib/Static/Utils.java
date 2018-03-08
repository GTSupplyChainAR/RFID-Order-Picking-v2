package com.thad.rfid_lib.Static;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.DisplayMetrics;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by theo on 2/27/18.
 */

public class Utils {

    public enum SOUNDS {ERROR, SUCCESS, CLICK}

    public static String[] getJSONStringKeys(JSONObject json){
        Iterator<String> iter = json.keys();
        List<String> keysList = new ArrayList<String>();
        while(iter.hasNext()) {
            String key = iter.next();
            keysList.add(key);
        }
        String[] keysArray = keysList.toArray(new String[keysList.size()]);
        return keysArray;
    }

    public static String tagToLetter(String tag){return String.valueOf(tag.charAt(0));}
    public static int[] tagToPos(String tag){
        return new int[]{Character.getNumericValue(tag.charAt(1))-1,
                Character.getNumericValue(tag.charAt(2))-1};
    }

    public static int getScreenWidth(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Activity activity = (Activity)context;
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int dp_to_pixels(Context context, float dp){
        return (int) ((dp)*context.getResources().getDisplayMetrics().density +0.5f);
    }

    public static void changeDrawableColor(Drawable drawable, int color_resource){
        if (drawable instanceof ShapeDrawable) {
            // cast to 'ShapeDrawable'
            ShapeDrawable shapeDrawable = (ShapeDrawable) drawable;
            shapeDrawable.getPaint().setColor(color_resource);
        } else if (drawable instanceof GradientDrawable) {
            // cast to 'GradientDrawable'
            GradientDrawable gradientDrawable = (GradientDrawable) drawable;
            gradientDrawable.setColor(color_resource);
        } else if (drawable instanceof ColorDrawable) {
            // alpha value may need to be set again after this call
            ColorDrawable colorDrawable = (ColorDrawable) drawable;
            colorDrawable.setColor(color_resource);
        }
    }
}
