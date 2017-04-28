package app.warinator.goalcontrol.timer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Warinator on 27.04.2017.
 */

public class TimerNotificationService extends Service {
    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_NEXT = "nextInterval";
    public static final String ACTION_SHOW_NOTIFICATION = "show_notification";
    public static final String ACTION_HIDE_NOTIFICATION = "hide_notification";

    public static final String ARG_TASK_ID = "task_id";

    private TimerManager mTimerManager;
    private boolean mIsStarted;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()){
            case ACTION_START:
                Log.v("THE_TIMER","SERVICE: START");
                mTimerManager.startOrPauseTimer();
                break;
            case ACTION_STOP:
                Log.v("THE_TIMER","SERVICE: STOP");
                mTimerManager.stopTimer();
                break;
            case ACTION_NEXT:
                Log.v("THE_TIMER","SERVICE: NEXT");
                mTimerManager.nextInterval();
                break;
            case ACTION_SHOW_NOTIFICATION:
                Log.v("THE_TIMER","SERVICE: START FG");
                if (mTimerManager.getTimerNotification() != null){
                    if (!mIsStarted){
                        mIsStarted = true;
                        mTimerManager.getTimerNotification().show(this);
                    }
                    else {
                        mTimerManager.getTimerNotification().refresh();
                    }
                }
                break;
            case ACTION_HIDE_NOTIFICATION:
                Log.v("THE_TIMER","SERVICE: STOP FG");
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
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.v("THE_TIMER","SERVICE DESTRUCTION");
        mTimerManager.saveTimer();
        super.onDestroy();
    }
}
