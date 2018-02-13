package com.thad.rfid_orderpick;

import android.content.Context;

/**
 * Created by theo on 2/5/18.
 */

public class DrawableMapper {

    Context mContext;

    public DrawableMapper(Context context){
        mContext = context;
    }

    public int getDrawable(String tag){
        int id = mContext.getResources().getIdentifier(tag, "drawable", mContext.getPackageName());
        return id;
    }

    public int getDrawable(int s, int e){
        //ARROWS
        return R.drawable.a11;
    }
}
