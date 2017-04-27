package app.warinator.goalcontrol.timer;

import android.content.Context;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.utils.PrefUtils;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Warinator on 26.04.2017.
 */
public class TimerManager {

    private static TimerManager mInstance;
    LinkedList<Interval> mIntervals = new LinkedList<>();
    private Context mContext;
    private TaskTimer mTimer;
    private ConcreteTask mTask;
    private List<ConcreteTask> mTasks;
    private int mCurrentPos;
    private boolean mAutoStartNext = false;

    private int mIntervalsDone;
    private long mStartTime;
    private long mPassedTime;

    private TimerManager(Context context){
        mContext = context;
        mTimer = TaskTimer.getInstance(context);
    }

    public static TimerManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TimerManager(context.getApplicationContext());
        }
        return mInstance;
    }

    //Подготовить таймер для задачи
    public void setNextTask(ConcreteTask ct) {
        mTask = ct;
        Task task = ct.getTask();
        mIntervals.clear();
        //TODO создание очереди интервалов
        if (task.getChronoTrackMode() == Task.ChronoTrackMode.INTERVAL) {
            int toBigBreak = task.getBigBreakEvery();
            long workTime = task.getWorkTime()/1000;
            long smallBreak = task.getSmallBreakTime()/1000;
            long bigBreak = task.getBigBreakTime()/1000;
            int interval = 0;
            for (int i = 0; i < task.getIntervalsCount(); i++) {
                if (mIntervalsDone <= interval++){
                    mIntervals.add(new Interval(IntervalType.WORK, workTime));
                }
                if (smallBreak > 0 && mIntervalsDone <= interval++) {
                    mIntervals.add(new Interval(IntervalType.SMALL_BREAK, smallBreak));
                }
                if (toBigBreak > 0 && bigBreak > 0 && (i + 1) % toBigBreak == 0 && mIntervalsDone <= interval++) {
                    mIntervals.add(new Interval(IntervalType.BIG_BREAK, bigBreak));
                }
            }
        } else if (task.getChronoTrackMode() == Task.ChronoTrackMode.COUNTDOWN) {
            mIntervals.push(new Interval(IntervalType.WORK, task.getWorkTime()/1000));
        }
        else {
            mIntervals.push(new Interval(IntervalType.NONE, 0));
        }
        getQueueWithTask(ct);
        goToNextInterval();
    }

    //получить очередь задач, предварительно добавив в неё целевую задачу, и
    //определить в ней позицию целевой задачи
    private void getQueueWithTask(ConcreteTask ct){
        QueuedDAO.getDAO().containsTask(ct.getId()).concatMap(new Func1<Boolean, Observable<Long>>() {
            @Override
            public Observable<Long> call(Boolean contains) {
                if (!contains){
                    return QueuedDAO.getDAO().addTask(ct.getId());
                }
                else {
                    return Observable.just(0L);
                }
            }
        }).concatMap(aLong -> QueuedDAO.getDAO().getAllQueued(true)).subscribe(tasks -> {
            mTasks = tasks;
            for (int i=0; i<tasks.size(); i++){
                if (tasks.get(i).getId() == ct.getId()){
                    mCurrentPos = i;
                    break;
                }
            }
        });
    }

    //перейти к очередному интервалу
    private void goToNextInterval(){
        if (!mIntervals.isEmpty()){
            Interval interval = mIntervals.remove();
            long before = mStartTime > 0 ?  mPassedTime + getTimeNow() - mStartTime : mPassedTime;
            mPassedTime = mStartTime = 0;
            mTimer.init(mTask, interval.mType, before, interval.mTime);
        }
        else {
            mCurrentPos = (mCurrentPos+1)%mTasks.size();
            mIntervalsDone = 0;
            setNextTask(mTasks.get(mCurrentPos));
        }
    }

    public void startOrPauseTimer(){
        if (mTimer.isRunning()){
            mTimer.pause();
        }
        else {
            mTimer.start();
        }
    }

    public void stopTimer(){
        mTimer.stop();
    }

    public void next(){
        goToNextInterval();
    }

    public void switchAutoNext(){
        mAutoStartNext = !mAutoStartNext;
    }

    private long getTimeNow() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }

    public void saveTimer(){
        if (mTask != null && mTimer != null){
            long startTime = mTimer.isRunning() ? mStartTime : 0;
            new PrefUtils(mContext).save(mTask.getId(), startTime, mTimer.getPassedTime(), mIntervalsDone);
        }
    }

    public void restoreTimer(){
        PrefUtils pref = new PrefUtils(mContext);
        long taskId = pref.getTaskId();
        if (taskId > 0){
            mStartTime = pref.getStartedTime();
            mPassedTime = pref.getPassedTime();
            mIntervalsDone = pref.getIntervalsDone();
            ConcreteTaskDAO.getDAO().get(taskId).subscribe(this::setNextTask);
        }
    }

    public void onTimerStop(){
        mIntervalsDone++;
        mStartTime = 0;
        goToNextInterval();
        if (mAutoStartNext){
            startOrPauseTimer();
        }
    }

    public void onTimerStart(){
        mStartTime = getTimeNow();
    }

    public enum IntervalType {
        WORK, SMALL_BREAK, BIG_BREAK, NONE
    }

    private static class Interval {
        IntervalType mType;
        long mTime;

        Interval(IntervalType type, long time) {
            mType = type;
            mTime = time;
        }
    }

}
