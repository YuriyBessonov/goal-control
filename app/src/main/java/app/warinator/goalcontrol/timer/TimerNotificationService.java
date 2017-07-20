package app.warinator.goalcontrol.timer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import rx.Subscription;

/**
 * Служба уведомления таймера
 */
public class TimerNotificationService extends Service {
    public static final String ACTION_START_PAUSE = "start_pause";
    public static final String ACTION_STOP_NEXT = "stop_next";
    public static final String ACTION_AUTO_FORWARD = "auto_forward";
    public static final String ACTION_NEXT_TASK = "next_task";
    public static final String ACTION_SHOW_NOTIFICATION = "show_notification";
    public static final String ACTION_HIDE_NOTIFICATION = "hide_notification";

    public static final String ARG_TASK_ID = "task_id";

    private TimerManager mTimerManager;
    private boolean mIsStarted;
    private Subscription mSub;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            intent = new Intent(getApplicationContext(), TimerNotificationService.class);
            intent.setAction(ACTION_SHOW_NOTIFICATION);
        }
        switch (intent.getAction()) {
            case ACTION_START_PAUSE:
                mTimerManager.actionStartOrPause();
                break;
            case ACTION_STOP_NEXT:
                mTimerManager.actionStopOrNext();
                break;
            case ACTION_AUTO_FORWARD:
                mTimerManager.actionSwitchAutoForward();
                break;
            case ACTION_NEXT_TASK:
                mTimerManager.actionNextTask();
                break;
            case ACTION_SHOW_NOTIFICATION:
                long taskId = intent.getLongExtra(ARG_TASK_ID, 0);
                if (mTimerManager.getTimerNotification() != null) {
                    if (!mIsStarted) {
                        mIsStarted = true;
                        mTimerManager.getTimerNotification().show(this);
                    } else {
                        mTimerManager.getTimerNotification().refresh();
                    }

                    if (taskId > 0) {
                        mSub = ConcreteTaskDAO.getDAO().get(taskId)
                                .subscribe(concreteTask -> {
                                    mTimerManager.startTask(concreteTask);
                                    mSub.unsubscribe();
                                });
                    }
                } else if (taskId > 0) {
                    onCreate();
                    mSub = ConcreteTaskDAO.getDAO().get(taskId).subscribe(concreteTask -> {
                        mTimerManager.startTask(concreteTask);
                        mSub.unsubscribe();
                    });

                }
                break;
            case ACTION_HIDE_NOTIFICATION:
                mIsStarted = false;
                stopForeground(true);
                stopSelf();
                break;
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mTimerManager = TimerManager.getInstance(getApplicationContext());
        mTimerManager.restoreTimer();
    }

    @Override
    public void onDestroy() {
        mTimerManager.getTimerNotification().cancel();
        mTimerManager.saveTimer();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mTimerManager.saveTimer();
    }

}
