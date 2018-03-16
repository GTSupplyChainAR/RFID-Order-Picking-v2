package com.thad.rfid_lib.Experiment;

import android.util.Log;

import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by theo on 3/14/18.
 */

public class ExperimentLog {
    private static final String TAG = "|StudySubject|";

    private int id;
    private String data_version, filename;
    private String date, duration;

    private boolean isArchive;
    private long time_started;

    private Experiment experiment;
    private File log_file;
    private FileOutputStream log_output_stream;

    public ExperimentLog(int id){
        this.id = id;
        duration = "00:00:00";
        isArchive = true;
    }
    public ExperimentLog(int id, String data_version, String date, String duration, String filename){
        this(id);
        this.data_version = data_version;
        this.filename = filename;
        this.date = date;
        this.duration = duration;
    }
    public ExperimentLog(JSONObject logJSON) throws JSONException{
        this(logJSON.getInt("log_id"), logJSON.getString("data_version"),
            logJSON.getString("date"), logJSON.getString("duration"),
            logJSON.getString("log_filename"));
    }

    public void startLog(Experiment experiment){
        this.experiment = experiment;
        try {
            isArchive = false;
            log_file = new File(experiment.getContext().getExternalFilesDir(null),
                    Prefs.EXPERIMENT_LOGS_FOLDER + "/" + filename);
            log_output_stream = new FileOutputStream(log_file);
            addLine("===== START =====", true);
            addLine(filename, true);
            addLine("Experiment Starting...", true);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            Log.d(TAG, "Failed to start log.");
        }
    }
    public void closeLog(){
        duration = Utils.formatTimestamp(experiment.getElapsedTime());
        try {
            addLine("Experiment Terminated.", true);
            addLine("===== END =====.", true);
            log_output_stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to close log.");
        }
    }

    public boolean addLine(String line){return addLine(line, false);}
    public boolean addLine(String line, boolean isComment){
        if(!Utils.isExternalStorageWritable()) {
            Log.e(TAG, "Storage not writable");
            return false;
        }
        try {
            String timestamp = "["+Utils.formatTimestamp(experiment.getElapsedTime(), true)+"]: ";
            String final_line = ((isComment)?"# ":timestamp)+line+"\n";
            Log.d(TAG, "Added to log : "+final_line);
            log_output_stream.write(final_line.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to add line to log.");
            return false;
        }
        return true;
    }

    public void setFilename(String subjectTag){
        filename = subjectTag+"_log_"+id+".txt";
    }
    public void setDate(String date){this.date = date;}
    public void setDataVersion(String version){this.data_version = version;}

    public int getID(){return id;}
    public String getFilename(){return filename;}

    public JSONObject getJSON() throws JSONException{
        JSONObject jObj = new JSONObject();
        jObj.put("log_id", id);
        jObj.put("data_version", data_version);
        jObj.put("date", date);
        jObj.put("duration", duration);
        jObj.put("log_filename", filename);
        return jObj;
    }
    public String toString(){return filename;}
}
