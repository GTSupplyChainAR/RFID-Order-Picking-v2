package com.thad.rfid_lib;

import android.util.Log;

import com.thad.rfid_lib.Static.Utils;

import java.util.ArrayList;

/**
 * Created by theo on 4/21/18.
 */

public class RunLog {
    private static final String TAG = "|RunLog|";

    private ArrayList<Scan> scans;
    public RunLog(){ scans = new ArrayList<Scan>(); }

    public void addScan(String timestamp_raw, String tag){
        scans.add(new Scan(Utils.decodeTimestamp(timestamp_raw), tag));
    }

    public long getWaitTime(int index){
        long prevTime = 0l;
        if(index != 0)
            prevTime = scans.get(index-1).getTime();

        long currTime = scans.get(index).getTime();
        return currTime - prevTime;
    }
    public long getTime(int index){
        return scans.get(index).getTime();
    }
    public String getTag(int index){
        return scans.get(index).getTag();
    }

    public int size(){return scans.size();}

    private class Scan{
        private Long timestamp;
        private String tag;
        public Scan(Long timestamp, String tag){
            this.timestamp = timestamp;
            this.tag = tag;
        }
        public long getTime(){return timestamp;}
        public String getTag(){return tag;}
    }
}
