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
    //время, прошедшее после начала работы таймера
    private long mPassedNow;
    private TimerState mState;

    public TimerNotification getNotification() {
        return mNotification;
    }

    private TimerNotification mNotification;
    //тип интервала времени
    private TimerManager.IntervalType mIntervalType;

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
        showNotification();
        mNotification.updateTime(timePassedSec);
    }


    public void start(){
        if (mState != TimerState.RUNNING){
            Log.v(TAG, "START");
            mState = TimerState.RUNNING;
            mNotification.updateTime(mPassedBefore);
            mNotification.updateState(mState);
            TimerManager.getInstance(mContext).onTimerStart();
            mSub = Observable.interval(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(passed -> {
                        mPassedNow = passed;
                        updateTaskTime(mPassedBefore+mPassedNow);
                        if (mTimeNeed > 0 && mPassedBefore+mPassedNow >= mTimeNeed){
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
            mNotification.updateTime(timePassed);
        }
    }

    public long getPassedTime(){
        return mPassedBefore+mPassedNow;
    }

    public boolean isRunning(){
        return mState == TimerState.RUNNING;
    }

}
