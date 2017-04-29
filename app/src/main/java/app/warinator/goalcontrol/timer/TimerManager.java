package app.warinator.goalcontrol.timer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.utils.PrefUtils;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;

import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_SHOW_NOTIFICATION;

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
    private boolean mAutoForward = false;
    private Subscription mTaskTimeSaveSub;

    public TimerNotification getTimerNotification() {
        return mTimerNotification;
    }

    private TimerNotification mTimerNotification;

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

    //Установить задачу в качестве текущей и запустить таймер
    public void startTask(ConcreteTask ct){
        saveTaskTime();
        mStartTime = 0;
        setNextTask(ct);
        mTimer.start();
    }

    //Подготовить таймер для задачи
    public void setNextTask(ConcreteTask ct) {
        mTask = ct;
        mTimerNotification = new TimerNotification(mContext, ct, mAutoForward);
        showNotification();
        Task task = ct.getTask();
        mIntervals.clear();
        //сформировать очередь интервалов
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

    private void showNotification(){
        Log.v("THE_TIMER","SHOW NOTIFICATION");
        Intent serviceIntent = new Intent(mContext, TimerNotificationService.class);
        serviceIntent.setAction(ACTION_SHOW_NOTIFICATION);
        mContext.startService(serviceIntent);
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
            mTimer.init(mTask, interval.mType, before, interval.mTime);
            if (mStartTime > 0){//автоматически продолжить отсчет, если сохранено время запуска
                mTimer.start();
            }
            mPassedTime = mStartTime = 0;
        }
        else {
            mCurrentPos = (mCurrentPos+1)%mTasks.size();
            mIntervalsDone = 0;
            setNextTask(mTasks.get(mCurrentPos));
        }
    }

    //Переключить состояние таймера
    public void actionStartOrPause(){
        if (mTimer.isRunning()){
            mTimer.pause();
        }
        else {
            mTimer.start();
        }
    }

    //Остановить таймер
    public void actionStopOrNext(){
        if (!mTimer.isStopped()){
            mTimer.stop();
        }
        else {
            goToNextInterval();
        }
    }

    public void actionNextTask(){
        mIntervals.clear();
        mIntervalsDone = 0;
        if (!mTimer.isStopped()){
            mTimer.stop();
        }
        goToNextInterval();
    }

    //Переключить автозапуск очередного таймера
    public void actionSwitchAutoForward(){
        mAutoForward = !mAutoForward;
        mTimerNotification.updateAutoForward(mAutoForward);
    }


    public void onTimerStop(){
        saveTaskTime();
        mIntervalsDone++;
        mStartTime = 0;
        if (mAutoForward){
            goToNextInterval();
            mTimer.start();
        }
    }

    public void onTimerStart(){
        mStartTime = getTimeNow();
    }


    public void saveTimer(){
        if (mTask != null && mTimer != null){
            long startTime = mTimer.isRunning() ? mStartTime : 0;
            new PrefUtils(mContext).save(mTask.getId(), startTime, mTimer.getPassedTime(), mIntervalsDone);
            Log.v("THE_TIMER","TIMER SAVED");
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

    private void saveTaskTime(){
        if (mTask != null && mTask.getId() > 0 && mTimer.getPassedWorkTime() > 0){
            if (mTaskTimeSaveSub != null && !mTaskTimeSaveSub.isUnsubscribed()){
                mTaskTimeSaveSub.unsubscribe();
            }
            mTaskTimeSaveSub = ConcreteTaskDAO.getDAO().addTimeSpent(mTask.getId(), mTimer.getPassedWorkTime()*1000)
                    .subscribe(integer -> mTaskTimeSaveSub.unsubscribe());
        }
    }

    private long getTimeNow() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
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
