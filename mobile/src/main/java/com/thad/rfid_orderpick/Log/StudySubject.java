package com.thad.rfid_orderpick.Log;

import android.util.Log;

import com.thad.rfid_lib.Experiment.ExperimentLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theo on 3/14/18.
 */

public class StudySubject {
    private static final String TAG = "|StudySubject|";

    private int id;
    private String name;

    private ArrayList<ExperimentLog> logs;

    public StudySubject(int id, String name){
        this.id = id;
        this.name = name;
        logs = new ArrayList<>();
    }
    public StudySubject(JSONObject subjectJSON) throws JSONException{
        this(subjectJSON.getInt("id"), subjectJSON.getString("name"));

        Log.d(TAG, toString());
        JSONArray logsJSON = subjectJSON.getJSONArray("logs");
        for (int i = 0; i < logsJSON.length(); i++) {
            JSONObject logJSON = logsJSON.getJSONObject(i);
            ExperimentLog log = new ExperimentLog(logJSON);
            Log.d(TAG, log.toString());
            addLog(log);
        }
    }


    public void addLog(ExperimentLog log){
        logs.add(log);
    }

    public void deleteAllLogs(LogIO logIO){
        for(ExperimentLog log : logs)
            logIO.deleteLogFile(log.getFilename());
        logs.clear();
    }

    public int getID(){return id;}
    public List<Integer> getLogIDs(){
        ArrayList<Integer> logIDS = new ArrayList<>();
        for(ExperimentLog log : logs)
            logIDS.add(log.getID());
        return logIDS;
    }
    public String getName(){return name;}
    public ArrayList<ExperimentLog> getLogs(){return logs;}

    public JSONObject getJSON() throws JSONException{
        JSONObject jObj = new JSONObject();
        jObj.put("id", id);
        jObj.put("name", name);
        JSONArray logsJSON = new JSONArray();
        for(ExperimentLog log : logs) {
            JSONObject logJSON = log.getJSON();
            logsJSON.put(logJSON);
        }
        jObj.put("logs", logsJSON);
        return jObj;
    }
    public String toString(){
        String tag = (name + " " + id).toLowerCase().replace(' ','_');
        return tag;
    }
}
