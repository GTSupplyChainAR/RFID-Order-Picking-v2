package com.thad.rfid_orderpick.Log;

import android.content.Context;
import android.util.Log;

import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by theo on 3/14/18.
 */

public class LogIO {
    private static final String TAG = "|LogIO|";

    private Context context;
    private File dir;

    public LogIO(Context context){
        this.context = context;
        dir = context.getExternalFilesDir(null);
        File experiment_log_dir = new File(dir, "experiment_logs");
        if(!experiment_log_dir.exists())
            experiment_log_dir.mkdirs();
    }

    public StudyData readStudyData(){
        if(!Utils.isExternalStorageWritable()) {
            Log.e(TAG, "Storage not readable");
            return null;
        }

        StudyData studyData = new StudyData();
        try {
            String jsonStr = readExternal(Prefs.USER_STUDY_FILENAME);
            JSONObject userStudyJSON = new JSONObject(jsonStr);
            studyData = new StudyData(userStudyJSON);
        }catch (JSONException e){
            e.printStackTrace();
            Log.e(TAG, "Failed to read Study Data.");
        }

        return studyData;
    }

    public void saveStudyData(StudyData studyData){
        try {
            JSONObject jsonObject = studyData.getJSON();
            saveExternal(Prefs.USER_STUDY_FILENAME, jsonObject.toString());
        }catch (JSONException e){
            e.printStackTrace();
            Log.e(TAG, "Failed to save Study Data.");
        }
    }

    private String readExternal(String filename){
        Log.d(TAG, "Reading from "+filename);

        File file = new File(dir, filename);
        String text = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                text = text + strLine;
            }
            in.close();
            Log.d(TAG, "File read.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to read file.");
        }
        return text;
    }

    private boolean saveExternal(String filename, String data){
        if(!Utils.isExternalStorageWritable()) {
            Log.e(TAG, "Storage not writable");
            return false;
        }
        File file = new File(dir, filename);
        Log.d(TAG, "Saving "+filename+" to "+file.toString());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();
            Log.d(TAG, "File saved.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to save file.");
            return false;
        }
        return true;
    }

    public void deleteLogFile(String log_filename){
        File deleteFile = new File(dir, Prefs.EXPERIMENT_LOGS_FOLDER+"/"+log_filename);
        boolean deleted = deleteFile.delete();
        Log.d(TAG, log_filename+((deleted)?" was deleted.":" failed to delete."));
    }
    public void deleteAllLogFiles(){
        File log_folder = new File(dir, Prefs.EXPERIMENT_LOGS_FOLDER);
        if (log_folder.isDirectory())
        {
            String[] children = log_folder.list();
            for (int i = 0; i < children.length; i++)
                new File(log_folder, children[i]).delete();
        }
    }





    public void createSampleData() throws Exception {

        JSONArray subjects = new JSONArray();


        JSONObject theoJSON = new JSONObject();
        theoJSON.put("id", 1);
        theoJSON.put("name", "Theo Panagiotopoulos");
        JSONArray logsJSON = new JSONArray();
        JSONObject logJSON = new JSONObject();
        logJSON.put("log_id", 1);
        logJSON.put("data_version","1.0");
        logJSON.put("date", "14-Mar-2018 22:44:29");
        logJSON.put("duration", "01:25:55");
        logJSON.put("log_filename","theo_panagiotopoulos_1_1.txt");
        logsJSON.put(logJSON);
        theoJSON.put("logs",logsJSON);
        subjects.put(theoJSON);

        JSONObject charu = new JSONObject();
        charu.put("id", 2);
        charu.put("name", "Charu Thomas");
        charu.put("logs",new JSONArray());
        subjects.put(charu);

        JSONObject sarthak = new JSONObject();
        sarthak.put("id", 3);
        sarthak.put("name", "Sarthak Srinivas");
        sarthak.put("logs",new JSONArray());
        subjects.put(sarthak);

        JSONObject pramod = new JSONObject();
        pramod.put("id", 4);
        pramod.put("name", "Pramod Kotipalli");
        pramod.put("logs",new JSONArray());
        subjects.put(pramod);

        JSONObject jObj = new JSONObject();
        jObj.put("subjects", subjects);
        saveExternal("user_study.json", jObj.toString());


        File experiment_log_dir = new File(dir, "experiment_logs");
        if(!experiment_log_dir.exists())
            experiment_log_dir.mkdirs();

        String placeholderTxt = "#LOG\nsubject_id|1\n"
                +"subject_name|Theodore Panagiotopoulos\n"
                +"START_TIME|00:00:00\n.\n.\n.";
        saveExternal("experiment_logs/theo_panagiotopoulos_1_1.txt", placeholderTxt);
    }
}
