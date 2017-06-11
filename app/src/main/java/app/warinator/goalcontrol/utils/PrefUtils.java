package app.warinator.goalcontrol.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Warinator on 26.04.2017.
 */

public class PrefUtils {
    private static final String STARTED_TIME = "com.warinator.started";
    private static final String PASSED_TIME = "com.warinator.passed";
    private static final String TASK_ID = "com.warinator.task_id";
    private static final String AUTO_FORWARD = "com.warinator.auto_next";
    private static final String INTERVALS_DONE = "com.warinator.intervals_done";
    private static final String LAST_LAUNCHED = "com.warinator.last_scheduled";

    private SharedPreferences mPreferences;

    public PrefUtils(Context c) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public long getStartedTime() {
        return mPreferences.getLong(STARTED_TIME, 0);
    }
    public long getPassedTime() {
        return mPreferences.getLong(PASSED_TIME, 0);
    }
    public long getTaskId() {
        return mPreferences.getLong(TASK_ID, 0);
    }
    public int getIntervalsDone() {
        return mPreferences.getInt(INTERVALS_DONE, 0);
    }

    public void setStartedTime(long started) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(STARTED_TIME, started);
        editor.apply();
    }

    public void setPassedTime(long passed) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(PASSED_TIME, passed);
        editor.apply();
    }

    public void saveTimer(long taskId, long startedTime, long passedTime, int intervalsDone, boolean autoFwdEnabled){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(TASK_ID, taskId);
        editor.putLong(STARTED_TIME, startedTime);
        editor.putLong(PASSED_TIME, passedTime);
        editor.putInt(INTERVALS_DONE, intervalsDone);
        editor.putBoolean(AUTO_FORWARD, autoFwdEnabled);
        editor.apply();
    }

    public void dropTimer(){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(TASK_ID, 0);
        editor.putLong(STARTED_TIME, 0);
        editor.putLong(PASSED_TIME, 0);
        editor.putInt(INTERVALS_DONE, 0);
        editor.putBoolean(AUTO_FORWARD, false);
        editor.apply();
    }

    public void setLastLaunched(long lastLaunched){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(LAST_LAUNCHED, lastLaunched);
        editor.apply();
    }

    public long getLastLaunched(){
        return mPreferences.getLong(LAST_LAUNCHED, 0);
    }
    public boolean getAutoForward(){
        return mPreferences.getBoolean(AUTO_FORWARD, false);
    }
}
