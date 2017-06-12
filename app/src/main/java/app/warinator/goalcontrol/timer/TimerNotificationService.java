package app.warinator.goalcontrol.timer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import rx.Subscription;

/**
 * Created by Warinator on 27.04.2017.
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
        if (intent == null){
            Log.v("THE_TIMER","SERVICE: null intent");
            intent = new Intent(getApplicationContext(), TimerNotificationService.class);
            intent.setAction(ACTION_SHOW_NOTIFICATION);
        }
        switch (intent.getAction()){
            case ACTION_START_PAUSE:
                Log.v("THE_TIMER","SERVICE: START/PAUSE");
                mTimerManager.actionStartOrPause();
                break;
            case ACTION_STOP_NEXT:
                Log.v("THE_TIMER","SERVICE: STOP/NEXT");
                mTimerManager.actionStopOrNext();
                break;
            case ACTION_AUTO_FORWARD:
                Log.v("THE_TIMER","SERVICE: AUTO FORWARD");
                mTimerManager.actionSwitchAutoForward();
                break;
            case ACTION_NEXT_TASK:
                Log.v("THE_TIMER","SERVICE: NEXT TASK");
                mTimerManager.actionNextTask();
                break;
            case ACTION_SHOW_NOTIFICATION:
                Log.v("THE_TIMER","SERVICE: START FG");
                long taskId = intent.getLongExtra(ARG_TASK_ID, 0);
                if (mTimerManager.getTimerNotification() != null){
                    if (!mIsStarted){
                        Log.v("THE_TIMER","SERVICE: NOT STARTED");
                        mIsStarted = true;
                        mTimerManager.getTimerNotification().show(this);
                    }
                    else {
                        Log.v("THE_TIMER","SERVICE: ALREDY STARTED");
                        mTimerManager.getTimerNotification().refresh();
                    }

                    if (taskId > 0){
                        Log.v("THE_TIMER","SERVICE: FOR TASK "+taskId);
                        mSub = ConcreteTaskDAO.getDAO().get(taskId)
                                .subscribe(concreteTask -> {
                                    mTimerManager.startTask(concreteTask);
                                    mSub.unsubscribe();
                                });
                    }
                }
                else if (taskId > 0){
                    onCreate();
                    mSub = ConcreteTaskDAO.getDAO().get(taskId).subscribe(concreteTask -> {
                        mTimerManager.startTask(concreteTask);
                        mSub.unsubscribe();
                    });

                }
                break;
            case ACTION_HIDE_NOTIFICATION:
                Log.v("THE_TIMER","SERVICE: STOP FG");
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
        Log.v("THE_TIMER","SERVICE CREATION");
        mTimerManager = TimerManager.getInstance(getApplicationContext());
        mTimerManager.restoreTimer();
    }

    @Override
    public void onDestroy() {
        Log.v("THE_TIMER","SERVICE DESTRUCTION");
        mTimerManager.getTimerNotification().cancel();
        mTimerManager.saveTimer();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mTimerManager.saveTimer();
        Log.v("THE_TIMER","TASK_REMOVED");
    }

}
