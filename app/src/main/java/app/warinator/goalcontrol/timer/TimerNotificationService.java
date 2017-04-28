package app.warinator.goalcontrol.timer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;

/**
 * Created by Warinator on 27.04.2017.
 */

public class TimerNotificationService extends Service {
    private static final int NOTIFICATION_ID = 83626;
    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_NEXT = "nextInterval";
    public static final String ACTION_START_FOREGROUND = "start_foreground";
    public static final String ACTION_STOP_FOREGROUND = "stop_foreground";

    public static final String ARG_TASK_ID = "task_id";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()){
            case ACTION_START:
                Log.v("THE_TIMER","SERVICE: START");
                break;
            case ACTION_STOP:
                Log.v("THE_TIMER","SERVICE: STOP");
                break;
            case ACTION_NEXT:
                Log.v("THE_TIMER","SERVICE: NEXT");
                break;
            case ACTION_START_FOREGROUND:
                Log.v("THE_TIMER","SERVICE: START FG");
                long taskId = intent.getExtras().getLong(ARG_TASK_ID);
                ConcreteTaskDAO.getDAO().get(taskId).subscribe(this::showNotification);
                break;
            case ACTION_STOP_FOREGROUND:
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

    private void showNotification(ConcreteTask ct){
        TimerNotificationSrv notificationSrv = new TimerNotificationSrv(this, ct);
    }
}
