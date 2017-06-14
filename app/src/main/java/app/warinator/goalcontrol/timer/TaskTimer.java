package app.warinator.goalcontrol.timer;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.ConcreteTask;
import rx.Observable;
import rx.Subscription;
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
    //тип интервала времени
    private TimerManager.IntervalType mIntervalType;

    private static final String TAG = "THE_TIMER";

    private TimerNotification getNotification(){
        return TimerManager.getInstance(mContext).getTimerNotification();
    }

    public enum TimerState {
        RUNNING,
        PAUSED,
        STOPPED
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
        mTimeNeed = (long) (Math.ceil((double)timeNeedSec/60.0)*60);
        mIntervalType = intType;
        mPassedBefore = timePassedSec;
        if (mTimeNeed > 0 && mPassedBefore > mTimeNeed){
            mPassedBefore = mTimeNeed;
        }
        if (mPassedBefore > 0){
            mState = TimerState.PAUSED;
        }
        else {
            mState = TimerState.STOPPED;
        }
        getNotification().updateState(mState);
        if (mIntervalType != TimerManager.IntervalType.SMALL_BREAK &&
                mIntervalType != TimerManager.IntervalType.BIG_BREAK){
            mPassedWork = mPassedBefore;
        }
        else {
            mPassedWork = 0;
        }
        updateIntervalType();
        getNotification().updateTime(mPassedBefore, mTimeNeed);
    }


    public void start(){
        if (mState != TimerState.RUNNING){
            Log.v(TAG, "START");
            if (mState == TimerState.STOPPED){
                mPassedWork = mPassedBefore = 0;
            }
            mState = TimerState.RUNNING;
            getNotification().updateTime(mPassedBefore, mTimeNeed);
            getNotification().updateState(mState);
            TimerManager.getInstance(mContext).onTimerStart();
            mSub = Observable.interval(1, TimeUnit.MINUTES)
                    .subscribeOn(Schedulers.computation())
                    .subscribe(passed -> {
                        passed *= 60;
                        mPassedNow = passed;
                        updateTaskTime(getPassedTime());
                        if (mTimeNeed > 0 && getPassedTime() >= mTimeNeed){
                            //проиграть звук и вибрировать при следующем обновлении уведомления
                            getNotification().setNoisy(true);
                            stop();
                        }
                    });
        }
    }

    public void pause(){
        Log.v(TAG, "PAUSE");
        if (mSub != null && !mSub.isUnsubscribed()){
            mSub.unsubscribe();
        }
        if (mState != TimerState.STOPPED){
            mState = TimerState.PAUSED;
        }
        getNotification().updateState(mState);
        mPassedBefore += mPassedNow;
        if (mIntervalType != TimerManager.IntervalType.SMALL_BREAK &&
                mIntervalType != TimerManager.IntervalType.BIG_BREAK){
            mPassedWork += mPassedNow;
        }
        mPassedNow = 0;
        TimerManager.getInstance(mContext).saveTimer();
    }

    public void stop(){
        Log.v(TAG, "STOP");
        mState = TimerState.STOPPED;
        pause();
        TimerManager.getInstance(mContext).onTimerStop();
    }


    public void updateIntervalType(){
        switch (mIntervalType) {
            case SMALL_BREAK:
                getNotification().updateName(mContext.getString(R.string.break_small), R.color.colorPrimary);
                break;
            case BIG_BREAK:
                getNotification().updateName(mContext.getString(R.string.break_big), R.color.colorPrimary);
                break;
            default:
                getNotification().updateName(mTask.getTask().getName(), R.color.colorGreyDark);
                break;
        }
    }

    private void updateTaskTime(long timePassed){
        getNotification().updateTime(timePassed, mTimeNeed);
        TimerManager.getInstance(mContext).saveTimer();
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

    public boolean isStopped(){
        return mState == TimerState.STOPPED;
    }

}
