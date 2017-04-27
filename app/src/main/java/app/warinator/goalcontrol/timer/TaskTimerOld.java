package app.warinator.goalcontrol.timer;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.utils.PrefUtils;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Warinator on 25.04.2017.
 */

public class TaskTimerOld {
    private ConcreteTask mTask;
    private static TaskTimerOld mInstance;
    private Subscription mSub;
    private long mWorkTime;
    private long mPassedBefore;
    private long mPassedNow;
    private PrefUtils mPref;
    private TaskTimer.TimerState mState;
    private TimerNotification mNotification;

    public enum TimerState {
        STOPPED,
        RUNNING
    }

    private TaskTimerOld(Context context){
        mPref = new PrefUtils(context);
    }

    public static TaskTimerOld getInstance(Context context){
        if (mInstance == null){
            mInstance = new TaskTimerOld(context);
        }
        return mInstance;
    }


    public void start(){
        if (mState != TaskTimer.TimerState.RUNNING){
            Log.v("THE_TIMER", "START!");
            mPref.setStartedTime(getTimeNow());
            mState = TaskTimer.TimerState.RUNNING;
            //showNotification();
            mNotification.updateTime(mPassedBefore, mWorkTime);
            mNotification.updateState(mState);
            mSub = Observable.interval(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(passed -> {
                        mPassedNow = passed;
                        updateUi(mPassedBefore+mPassedNow);
                        if (mWorkTime > 0){
                            if (mPassedBefore+mPassedNow >= mWorkTime){
                                stop();
                            }
                        }
                        else {
                            //direct
                        }
                    });
        }
    }

    public void pause(){
        Log.v("THE_TIMER", "PAUSE!");
        mState = TaskTimer.TimerState.STOPPED;
        if (mNotification != null){
            mNotification.updateState(mState);
        }
        mPassedBefore += mPassedNow;
        mPassedNow = 0;
        if (mSub != null && !mSub.isUnsubscribed()){
            mSub.unsubscribe();
        }
    }

    public void stop(){
        Log.v("THE_TIMER", "STOP!");
        pause();
        mPref.setPassedTime(0);
        mPref.setStartedTime(0);
        mPassedNow = mPassedBefore = 0;
    }

    public void save(){
        mPref.setPassedTime(mPassedBefore);
    }

    public void restore(){
        long startTime = mPref.getStartedTime();
        if (startTime > 0){//время было сохранено
            mPassedBefore = mPref.getPassedTime() + (getTimeNow() - startTime);
            if (mWorkTime > 0 && mPassedBefore >= mWorkTime){
                stop();
                //todo: finish
            }
            else {
                start();
            }
        }
        else {
            stop();
        }
    }

    public void setTargetTask(ConcreteTask task){
        mTask = task;
        mWorkTime = task.getTask().getWorkTime()/1000;
    }

    private long getTimeNow(){
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }


    public void showNotification(Context context){
        mNotification = new TimerNotification(context, mTask);
    }

    private void updateUi(long timePassed){
        if (mNotification != null && timePassed % 60 == 0){
            mNotification.updateTime(timePassed, mWorkTime);
        }
    }

    public boolean isRunning(){
        return mState == TaskTimer.TimerState.RUNNING;
    }

}
