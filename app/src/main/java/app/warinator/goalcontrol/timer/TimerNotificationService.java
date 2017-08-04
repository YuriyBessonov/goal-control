package app.warinator.goalcontrol.timer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Служба уведомления таймера
 */
public class TimerNotificationService extends Service {
    public static final String ACTION_START_PAUSE = "start_pause";
    public static final String ACTION_STOP_NEXT = "stop_next";
    public static final String ACTION_AUTO_FORWARD = "auto_forward";
    public static final String ACTION_NEXT_TASK = "next_task";
    public static final String ACTION_HIDE_NOTIFICATION = "hide_notification";
    public static final String ACTION_START = "start";
    public static final String ACTION_SHOW_DETACHED = "show_detached";
    public static final String ACTION_SHOW_ATTACHED = "show_attached";

    public static final String ARG_TASK_ID = "task_id";

    private TimerManager mTimerManager;
    private boolean mIsInForeground = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            intent = new Intent(getApplicationContext(), TimerNotificationService.class);
            intent.setAction(ACTION_START);
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
            case ACTION_START:
                long taskId = intent.getLongExtra(ARG_TASK_ID, 0);
                if (taskId > 0){
                    cancelReminderNotification((int)taskId);
                    mTimerManager.startTask(taskId);
                }
                else {
                    mTimerManager.restoreTimer();
                }
                break;
            case ACTION_SHOW_ATTACHED:
                TimerNotification notification = mTimerManager.getTimerNotification();
                if (notification != null){
                    notification.setOngoing(true);
                    if (!mIsInForeground){
                        mIsInForeground = true;
                        notification.show(this);
                    }
                }
                break;
            case ACTION_SHOW_DETACHED:
                stopForeground(false);
                mTimerManager.getTimerNotification().setOngoing(false);
                mTimerManager.getTimerNotification().refresh();
                return START_NOT_STICKY;
            case ACTION_HIDE_NOTIFICATION:
                stopForeground(true);
                return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private void cancelReminderNotification(int id){
        ((NotificationManager) getApplicationContext().getSystemService(
                Context.NOTIFICATION_SERVICE)).cancel(id);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mTimerManager = TimerManager.getInstance(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        mTimerManager.saveTimer();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mTimerManager.saveTimer();
    }

}
