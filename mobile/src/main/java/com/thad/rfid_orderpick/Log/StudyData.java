package com.thad.rfid_orderpick.Log;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theo on 3/14/18.
 */

public class StudyData {
    private static final String TAG = "|StudyData|";

    private ArrayList<StudySubject> subjects;
    private StudySubject activeSubject;

    public StudyData(){
        subjects = new ArrayList<>();
    }
    public StudyData(JSONObject userStudyJSON) throws JSONException{
        this();
        JSONArray subjectsJSON = userStudyJSON.getJSONArray("subjects");
        for(int i = 0 ; i < subjectsJSON.length() ; i++) {
            JSONObject subjectJSON = subjectsJSON.getJSONObject(i);
            StudySubject studySubject = new StudySubject(subjectJSON);
            addSubject(studySubject);
        }
    }

    public void addSubject(StudySubject subject){
        subjects.add(subject);
        activeSubject = subject;
    }
    public void selectSubject(StudySubject subject){ activeSubject = subject; }

    public ArrayList<StudySubject> getSubjects(){return subjects;}
    public StudySubject getSubject(String username){
        for(StudySubject subject : subjects)
            if(subject.getName().equals(username))
                return subject;
        return null;
    }
    public StudySubject getActiveSubject(){return activeSubject;}
    public List<Integer> getIDs(){
        List<Integer> ids = new ArrayList<>();
        for(StudySubject subject : subjects){
            ids.add(subject.getID());
            List<Integer> logIDs = subject.getLogIDs();
            ids.addAll(logIDs);
        }
        return ids;
    }
    public List<String> getNames(){
        List<String> names = new ArrayList<>();
        for(StudySubject subject : subjects)
            names.add(subject.getName());
        return names;
    }

    public void deleteSoftAll(){subjects.clear();}
    public void deleteAll(LogIO logIO){
        for(StudySubject subject : subjects)
            subject.deleteAllLogs(logIO);
        subjects.clear();
    }
    public void deleteSoft(int id){delete(id, null);}
    public void delete(int id, LogIO logIO){
        for(int i = 0 ; i < subjects.size() ; i++){
            if(id == subjects.get(i).getID()){
                if(logIO != null)
                    subjects.get(i).deleteAllLogs(logIO);
                subjects.remove(i);
                break;
            }
        }
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonSubjects = new JSONArray();
        for(StudySubject subject : subjects){
            JSONObject jsonSubject = subject.getJSON();
            jsonSubjects.put(jsonSubject);
        }
        jsonObject.put("subjects", jsonSubjects);
        return jsonObject;
    }
}
