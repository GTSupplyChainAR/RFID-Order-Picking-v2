package com.thad.rfid_orderpick.Log;

import android.content.Context;
import android.util.Log;

import com.thad.rfid_lib.Experiment.ExperimentLog;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_orderpick.MobileClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.security.auth.Subject;

/**
 * Created by theo on 3/14/18.
 */

public class StudyHandler {
    private static final String TAG = "|StudyHandler|";

    private Context context;

    private boolean isStudyRunning;

    private StudyData studyData;
    private LogIO logIO;

    public StudyHandler(MobileClient client){
        this.context = client.getContext();

        logIO = new LogIO(context);
        studyData = new StudyData();

        isStudyRunning = false;

        init();
    }

    private void init() {
        studyData = logIO.readStudyData();

        if(false) {
            logIO.deleteAllLogFiles();
            studyData.deleteAll(logIO);
            studyData = new StudyData();
            logIO.saveStudyData(studyData);
        }
    }


    public ExperimentLog getExperimentLog(){
        int id = getUniqueID();
        StudySubject activeSubject = studyData.getActiveSubject();
        ExperimentLog log = new ExperimentLog(id);
        log.setFilename(activeSubject.toString());
        log.setDate(Utils.getDateInFormat());
        activeSubject.addLog(log);
        autosave();
        return log;
    }

    public void onSubjectSelected(String username){
        StudySubject selected = studyData.getSubject(username);
        if(selected == null){
            studyData.addSubject(new StudySubject(getUniqueID(), username));
            autosave();
        }else {
            studyData.selectSubject(selected);
        }
        isStudyRunning = true;
    }

    public boolean isStudyRunning(){return isStudyRunning;}
    public List<String> getSubjectNames(){
        return studyData.getNames();
    }
    public StudySubject getActiveSubject(){return studyData.getActiveSubject();}

    public void autosave(){
        logIO.saveStudyData(studyData);
    }

    private int getUniqueID(){
        List<Integer> existingIDs = studyData.getIDs();
        Random rand = new Random();
        int lower_bound = (int) Math.pow(10, Prefs.ID_DIGIT_NUM-1);
        int upper_bound = lower_bound*10-1;
        int uniqueID;
        while(true) {
            uniqueID = lower_bound + Math.abs(rand.nextInt()) % (upper_bound - lower_bound);
            boolean isUnique = true;
            for (Integer id : existingIDs){
                if (id == uniqueID) {
                    isUnique = false;
                    break;
                }
            }
            if(isUnique)
                break;
        }

        return uniqueID;
    }
    public StudyData getStudyData(){return studyData;}

}
