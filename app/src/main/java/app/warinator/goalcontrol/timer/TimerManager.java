package app.warinator.goalcontrol.timer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.utils.PrefUtils;
import rx.Observable;
import rx.Subscription;

import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_HIDE_NOTIFICATION;
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
    private Subscription mTasksQueueSub;

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
        Log.v("THE_QUEUED","set next task");
        mTask = ct;
        mTimerNotification = new TimerNotification(mContext, ct, mAutoForward);
        showNotification();
        Task task = ct.getTask();
        mIntervals.clear();
        //сформировать очередь интервалов
        mIntervalsDone--;
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

    private void hideNotification(){
        Log.v("THE_TIMER","HIDE NOTIFICATION");
        Intent serviceIntent = new Intent(mContext, TimerNotificationService.class);
        serviceIntent.setAction(ACTION_HIDE_NOTIFICATION);
        mContext.startService(serviceIntent);
    }

    //получить очередь задач, предварительно добавив в неё целевую задачу, и
    //определить в ней позицию целевой задачи
    private void getQueueWithTask(ConcreteTask ct){
        Log.v("THE_QUEUED","request : "+ct.getTask().getName());
        if (mTasksQueueSub != null && !mTasksQueueSub.isUnsubscribed()){
            mTasksQueueSub.unsubscribe();
        }

        Observable<List<ConcreteTask>> obs;
        if (mTasks == null){
            if (ct.getQueuePos() < 0){
                obs = ConcreteTaskDAO.getDAO().addTaskToQueue(ct.getId())
                        .concatMap(integer -> ConcreteTaskDAO.getDAO().getAllQueued(true));
            }
            else {
                obs = ConcreteTaskDAO.getDAO().getAllQueued(true);
            }
        }
        else {
            if (ct.getQueuePos() < 0){
                obs = ConcreteTaskDAO.getDAO().addTaskToQueue(ct.getId())
                        .concatMap(integer -> Observable.just(mTasks));
            }
            else {
                obs = Observable.just(mTasks);
            }
        }
        obs.subscribe(tasks -> {
            mTasks = tasks;
            for (int i=0; i<tasks.size(); i++){
                if (tasks.get(i).getId() == ct.getId()){
                    mCurrentPos = i;
                    Log.v("THE_QUEUED","current pos : "+mCurrentPos);
                    break;
                }
            }
        });
    }

    public void refreshOrder(){
        Log.v("THE_QUEUED","refresh order ");
        if (mTask != null){
            mTasks = null;
            getQueueWithTask(mTask);
        }
    }


    //перейти к очередному интервалу
    private void goToNextInterval(){
        if (!mIntervals.isEmpty()){
            Interval interval = mIntervals.remove();
            mIntervalsDone++;
            long before = mStartTime > 0 ?  mPassedTime + getTimeNow() - mStartTime : mPassedTime;
            mTimer.init(mTask, interval.mType, before, interval.mTime);
            if (mStartTime > 0){//автоматически продолжить отсчет, если сохранено время запуска
                mTimer.start();
            }
            mPassedTime = mStartTime = 0;
        }
        else if (mTasks != null && !mTasks.isEmpty()){
            mCurrentPos = (mCurrentPos+1)%mTasks.size();
            mIntervalsDone = 1;
            setNextTask(mTasks.get(mCurrentPos));
        }
        else {
            hideNotification();
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
            if (mTimer.isStopped()){
                new PrefUtils(mContext).dropTimer();
                Log.v("THE_TIMER","TIMER DROPPED");
            }
            else {
                long startTime = mTimer.isRunning() ? mStartTime : 0;
                new PrefUtils(mContext).saveTimer(mTask.getId(), startTime, mTimer.getPassedTime(), mIntervalsDone, mAutoForward);
                Log.v("THE_TIMER","TIMER SAVED");
            }

        }
    }

    public void restoreTimer(){
        PrefUtils pref = new PrefUtils(mContext);
        long taskId = pref.getTaskId();
        if (taskId > 0){
            mStartTime = pref.getStartedTime();
            mPassedTime = pref.getPassedTime();
            mIntervalsDone = pref.getIntervalsDone();
            mAutoForward = pref.getAutoForward();
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
