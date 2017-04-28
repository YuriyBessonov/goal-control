package app.warinator.goalcontrol.timer;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Warinator on 25.04.2017.
 */

public class TaskTimer {
    private static TaskTimer mInstance;
    private Context mContext;
    //задача, для которой ведется отсчет
    private ConcreteTask mTask;
    //подписка таймера
    private Subscription mSub;
    //время обратного отсчета
    private long mTimeNeed;
    //время, прошедшее до начала работы таймера
    private long mPassedBefore;
    //время, прошедшее до начала работы таймера, исключая перерывы
    private long mPassedWork;
    //время, прошедшее после начала работы таймера
    private long mPassedNow;
    private TimerState mState;
    private TimerNotification mNotification;
    //тип интервала времени
    private TimerManager.IntervalType mIntervalType;

    public TimerNotification getNotification() {
        return mNotification;
    }

    private static final String TAG = "THE_TIMER";

    public enum TimerState {
        STOPPED,
        RUNNING
    }

    private TaskTimer(Context context){
        mContext = context;
    }

    public static TaskTimer getInstance(Context context){
        if (mInstance == null){
            mInstance = new TaskTimer(context.getApplicationContext());
        }
        return mInstance;
    }

    //Инициализация таймера.
    //Постусловия: таймер в начальном состоянии, отображено уведомление
    public void init(ConcreteTask ct, TimerManager.IntervalType intType, long timePassedSec, long timeNeedSec){
        pause();
        mTask = ct;
        mTimeNeed = timeNeedSec;
        mIntervalType = intType;
        mPassedBefore = timePassedSec;
        if (mTimeNeed > 0 && mPassedBefore > mTimeNeed){
            mPassedBefore = mTimeNeed;
        }
        if (mIntervalType != TimerManager.IntervalType.SMALL_BREAK &&
                mIntervalType != TimerManager.IntervalType.BIG_BREAK){
            mPassedWork = mPassedBefore;
        }
        else {
            mPassedWork = 0;
        }
        showNotification();
        mNotification.updateTime(mPassedBefore, mTimeNeed);
    }


    public void start(){
        if (mState != TimerState.RUNNING){
            Log.v(TAG, "START");
            mState = TimerState.RUNNING;
            mNotification.updateTime(mPassedBefore, mTimeNeed);
            mNotification.updateState(mState);
            TimerManager.getInstance(mContext).onTimerStart();
            mSub = Observable.interval(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(passed -> {
                        mPassedNow = passed;
                        updateTaskTime(getPassedTime());
                        if (mTimeNeed > 0 && getPassedTime() >= mTimeNeed){
                                stop();
                        }
                    });
        }
    }

    public void pause(){
        Log.v(TAG, "PAUSE");
        mState = TimerState.STOPPED;
        if (mSub != null && !mSub.isUnsubscribed()){
            mSub.unsubscribe();
        }
        if (mNotification != null){
            mNotification.updateState(mState);
        }
        mPassedBefore += mPassedNow;
        if (mIntervalType != TimerManager.IntervalType.SMALL_BREAK &&
                mIntervalType != TimerManager.IntervalType.BIG_BREAK){
            mPassedWork += mPassedNow;
        }
        mPassedNow = 0;
    }

    public void stop(){
        Log.v(TAG, "STOP");
        pause();
        TimerManager.getInstance(mContext).onTimerStop();
    }


    public void showNotification(){
        mNotification = new TimerNotification(mContext, mTask);
        switch (mIntervalType) {
            case SMALL_BREAK:
                mNotification.updateName(mContext.getString(R.string.break_small));
                break;
            case BIG_BREAK:
                mNotification.updateName(mContext.getString(R.string.break_big));
                break;
            default:
                mNotification.updateName(mTask.getTask().getName());
                break;
        }
    }

    private void updateTaskTime(long timePassed){
        Log.v(TAG, "PASSED "+ Util.getFormattedTimeSeconds(timePassed*1000));
        if (mNotification != null && timePassed % 60 == 0){
            mNotification.updateTime(timePassed, mTimeNeed);
        }
    }

    public long getPassedTime(){
        return mPassedBefore+mPassedNow;
    }

    public long getPassedWorkTime(){
        long passedWork = mPassedWork;
        if (mIntervalType != TimerManager.IntervalType.SMALL_BREAK &&
                mIntervalType != TimerManager.IntervalType.BIG_BREAK){
            passedWork += mPassedNow;
        }
        return passedWork;
    }

    public boolean isRunning(){
        return mState == TimerState.RUNNING;
    }

}
