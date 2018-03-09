package com.thad.rfid_lib.Static;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.DisplayMetrics;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
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

    public static double distance(int[] p1, int[] p2){
        return Math.sqrt(Math.pow(p1[0]-p2[0],2)+Math.pow(p1[1]-p2[1],2));
    }

    public static Drawable loadDrawableFromAssets(Context context, String filename){
        try {
            InputStream ims = context.getAssets().open(filename);
             return Drawable.createFromStream(ims, null);
        }
        catch(IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String formatTimestamp(Long time){
        int seconds = (int) (time / 1000)%60;
        int minutes = ((int)(time/1000)/60)%60;
        int hours = (int)(time/1000)/60/60;

        String hoursStr = Integer.toString(hours);
        if(hours < 10) hoursStr = "0"+hoursStr;

        String minutesStr = Integer.toString(minutes);
        if(minutes < 10) minutesStr = "0"+minutesStr;

        String secondsStr = Integer.toString(seconds);
        if(seconds < 10) secondsStr = "0"+secondsStr;

        return hoursStr+":"+minutesStr+":"+secondsStr;
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
