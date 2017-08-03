package app.warinator.goalcontrol.timer;

import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.utils.PrefUtils;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
import rx.Subscription;

import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_HIDE_NOTIFICATION;
import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_SHOW_DETACHED;
import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_SHOW_NOTIFICATION;

/**
 * Класс, управляющий таймером
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
    private TimerNotification mTimerNotification;
    private int mIntervalsDone;
    private long mStartTime;
    private long mPassedTime;

    private TimerManager(Context context) {
        mContext = context;
        mTimer = TaskTimer.getInstance(context);
    }

    public static TimerManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TimerManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public TimerNotification getTimerNotification() {
        return mTimerNotification;
    }

    //Начать учет времени для задачи; если задача назначена на другой день, то
    //заменить её назначенной на сегодня (добавить такую, если отсутствует)
    public void startTask(ConcreteTask ct) {
        if (ct.getDateTime() != null && !Util.dayIsToday(ct.getDateTime())) {
            ConcreteTaskDAO.getDAO().getAllForTaskToday(ct.getTask().getId(), false)
                    .concatMap(concreteTasks -> {
                        if (concreteTasks.size() > 0) {
                            start(concreteTasks.get(0));
                            return Observable.just(-1L);
                        } else {
                            Calendar today = Calendar.getInstance();
                            Calendar dateTime = Calendar.getInstance();
                            dateTime.setTime(ct.getDateTime().getTime());
                            dateTime.set(Calendar.YEAR, today.get(Calendar.YEAR));
                            dateTime.set(Calendar.MONTH, today.get(Calendar.MONTH));
                            dateTime.set(Calendar.DATE, today.get(Calendar.DATE));
                            ConcreteTask newCt = new ConcreteTask();
                            Task t = new Task();
                            t.setId(ct.getTask().getId());
                            newCt.setTask(t);
                            newCt.setDateTime(dateTime);
                            newCt.setQueuePos(-1);
                            return ConcreteTaskDAO.getDAO().add(newCt);
                        }
                    })
                    .concatMap(id -> (id > 0 ?
                            ConcreteTaskDAO.getDAO().get(id) :
                            Observable.just(null)))
                    .subscribe(addedTask -> {
                        if (addedTask != null) {
                            start(addedTask);
                        }
                    });
        } else {
            start(ct);
        }

    }

    //Установить задачу в качестве текущей и запустить таймер
    private void start(ConcreteTask ct) {
        if (!mTimer.isStopped()) {
            saveTaskTime();
        }
        mStartTime = 0;
        setNextTask(ct);
        mTimer.start();
    }

    //Подготовить таймер для задачи
    private void setNextTask(ConcreteTask ct) {
        mTask = ct;
        mTimerNotification = new TimerNotification(mContext, ct, mAutoForward);
        showNotification(true);
        Task task = ct.getTask();
        mIntervals.clear();
        //сформировать очередь интервалов
        mIntervalsDone--;
        if (task.getChronoTrackMode() == Task.ChronoTrackMode.INTERVAL) {
            int toBigBreak = task.getBigBreakEvery();
            long workTime = task.getWorkTime() / 1000;
            long smallBreak = task.getSmallBreakTime() / 1000;
            long bigBreak = task.getBigBreakTime() / 1000;
            int interval = 0;
            for (int i = 0; i < task.getIntervalsCount(); i++) {
                if (mIntervalsDone <= interval++) {
                    mIntervals.add(new Interval(IntervalType.WORK, workTime));
                }
                if (toBigBreak > 0 && bigBreak > 0 && (i + 1) % toBigBreak == 0 && mIntervalsDone <= interval++) {
                    mIntervals.add(new Interval(IntervalType.BIG_BREAK, bigBreak));
                } else if (smallBreak > 0 && mIntervalsDone <= interval++) {
                    mIntervals.add(new Interval(IntervalType.SMALL_BREAK, smallBreak));
                }
            }
        } else if (task.getChronoTrackMode() == Task.ChronoTrackMode.COUNTDOWN) {
            mIntervals.push(new Interval(IntervalType.WORK, task.getWorkTime() / 1000));
        } else {
            mIntervals.push(new Interval(IntervalType.NONE, 0));
        }
        getQueueWithTask(ct);
        goToNextInterval();
    }

    //Скрыть уведомление таймера
    private void hideNotification() {
        Intent serviceIntent = new Intent(mContext, TimerNotificationService.class);
        serviceIntent.setAction(ACTION_HIDE_NOTIFICATION);
        mContext.startService(serviceIntent);
    }

    //Отобразить смахиваемое/закремленное уведомление
    private void showNotification(boolean attached) {
        Intent serviceIntent = new Intent(mContext, TimerNotificationService.class);
        if (attached) {
            serviceIntent.setAction(ACTION_SHOW_NOTIFICATION);
        } else {
            serviceIntent.setAction(ACTION_SHOW_DETACHED);
        }
        mContext.startService(serviceIntent);
    }

    //Получить очередь задач, предварительно добавив в неё целевую задачу, и
    //определить в ней позицию целевой задачи
    private void getQueueWithTask(ConcreteTask ct) {
        if (mTasksQueueSub != null && !mTasksQueueSub.isUnsubscribed()) {
            mTasksQueueSub.unsubscribe();
        }

        Observable<List<ConcreteTask>> obs;
        if (mTasks == null) {
            if (ct.getQueuePos() < 0) {
                obs = ConcreteTaskDAO.getDAO().addTaskToQueue(ct.getId())
                        .concatMap(integer -> ConcreteTaskDAO.getDAO().getAllQueued(true));
            } else {
                obs = ConcreteTaskDAO.getDAO().getAllQueued(true);
            }
        } else {
            if (ct.getQueuePos() < 0) {
                obs = ConcreteTaskDAO.getDAO().addTaskToQueue(ct.getId())
                        .concatMap(integer -> Observable.just(mTasks));
            } else {
                obs = Observable.just(mTasks);
            }
        }
        obs.subscribe(tasks -> {
            mTasks = tasks;
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId() == ct.getId()) {
                    mCurrentPos = i;
                    break;
                }
            }
        });
    }

    //Обновить порядок задач в очереди
    public void refreshOrder() {
        if (mTask != null) {
            mTasks = null;
            getQueueWithTask(mTask);
        }
    }

    //Перейти к очередному интервалу
    private void goToNextInterval() {
        if (!mIntervals.isEmpty()) {
            Interval interval = mIntervals.remove();
            mIntervalsDone++;
            long before = mStartTime > 0 ? mPassedTime + getTimeNow() - mStartTime : mPassedTime;
            mTimer.init(mTask, interval.mType, before, interval.mTime);
            if (mStartTime > 0) {//автоматически продолжить отсчет, если сохранено время запуска
                mTimer.start();
            }
            mPassedTime = mStartTime = 0;
        } else if (mTasks != null && !mTasks.isEmpty()) {
            mCurrentPos = (mCurrentPos + 1) % mTasks.size();
            mIntervalsDone = 1;
            setNextTask(mTasks.get(mCurrentPos));
        } else {
            hideNotification();
        }
    }

    //Переключить состояние таймера
    public void actionStartOrPause() {
        if (mTimer.isRunning()) {
            mTimer.pause();
        } else {
            showNotification(true);
            mTimer.start();
        }
    }

    //Остановить таймер
    public void actionStopOrNext() {
        if (!mTimer.isStopped()) {
            mTimer.stop();
        } else {
            goToNextInterval();
        }
    }

    //Перейти к следующей задаче
    public void actionNextTask() {
        mIntervals.clear();
        mIntervalsDone = 0;
        if (!mTimer.isStopped()) {
            mTimer.stop();
        }
        goToNextInterval();
    }

    //Переключить автозапуск очередного таймера
    public void actionSwitchAutoForward() {
        mAutoForward = !mAutoForward;
        mTimerNotification.updateAutoForward(mAutoForward);
    }

    //При остановке таймера
    public void onTimerStop() {
        saveTaskTime();
        mStartTime = 0;
        if (mAutoForward) {
            goToNextInterval();
            mTimer.start();
        } else {
            showNotification(false);
        }
    }


    //При запуске таймера
    public void onTimerStart() {
        mStartTime = getTimeNow();
    }


    //Сохранить состояние таймера в настройках
    public void saveTimer() {
        if (mTask != null && mTimer != null) {
            if (mTimer.isStopped()) {
                new PrefUtils(mContext).dropTimer();
            } else {
                long startTime = mTimer.isRunning() ? mStartTime : 0;
                new PrefUtils(mContext).saveTimer(mTask.getId(), startTime, mTimer.getPassedTime(),
                        mIntervalsDone, mAutoForward);
            }

        }
    }

    //Восстановить состояние таймера
    public void restoreTimer() {
        PrefUtils pref = new PrefUtils(mContext);
        long taskId = pref.getTaskId();
        if (taskId > 0) {
            mStartTime = pref.getStartedTime();
            mPassedTime = pref.getPassedTime();
            mIntervalsDone = pref.getIntervalsDone();
            mAutoForward = pref.getAutoForward();
            ConcreteTaskDAO.getDAO().get(taskId).subscribe(this::setNextTask);
        }
    }

    //Сохранить затраченное время в БД
    private void saveTaskTime() {
        if (mTask != null && mTask.getId() > 0 && mTimer.getPassedWorkTime() > 0) {
            if (mTaskTimeSaveSub != null && !mTaskTimeSaveSub.isUnsubscribed()) {
                mTaskTimeSaveSub.unsubscribe();
            }
            mTaskTimeSaveSub = ConcreteTaskDAO.getDAO().addTimeSpent(mTask.getId(),
                    mTimer.getPassedWorkTime() * 1000)
                    .subscribe(integer -> mTaskTimeSaveSub.unsubscribe());
        }
    }

    //Получить текущее время
    private long getTimeNow() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }

    //Тип интервала
    public enum IntervalType {
        WORK, SMALL_BREAK, BIG_BREAK, NONE
    }

    //Интервал таймера
    private static class Interval {
        IntervalType mType;
        long mTime;

        Interval(IntervalType type, long time) {
            mType = type;
            mTime = time;
        }
    }

}
