package app.warinator.goalcontrol.timer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.activity.MainActivity;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.utils.Util;

import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_SHOW_NOTIFICATION;

/**
 * Created by Warinator on 26.04.2017.
 */

public class TaskReminderNotification extends BaseTaskNotification{
    private static final int NOTIFICATION_ID = 346261;
    private static final int REQUEST_CODE = 53535;

    public TaskReminderNotification(Context context, ConcreteTask task){
        super(context, task, new Intent(context, MainActivity.class));
        mNotificationId = NOTIFICATION_ID;
        mNotifyBuilder.setOngoing(false);
        mNotification = mNotifyBuilder.build();
        setNoisy(true);

    }

    @Override
    public void setupView(ConcreteTask task){
        super.setupView(task);
        mNotificationView.setViewVisibility(R.id.pb_timer, View.GONE);
        mNotificationView.setViewVisibility(R.id.la_controls, View.GONE);
        mNotificationView.setViewVisibility(R.id.tv_timer, View.GONE);
        mNotificationView.setViewVisibility(R.id.btn_start_task, View.VISIBLE);
        if (task.getTask().isWithTime()){
            mNotificationView.setViewVisibility(R.id.la_task_time, View.VISIBLE);
            mNotificationView.setTextViewText(R.id.tv_task_time, Util.getFormattedTime(task.getDateTime()));
        }
    }

    @Override
    protected void setupListeners(long taskId) {
        Intent startIntent = new Intent(mContext, TimerNotificationService.class);
        startIntent.setAction(ACTION_SHOW_NOTIFICATION);
        startIntent.putExtra(TimerNotificationService.ARG_TASK_ID, taskId);
        PendingIntent pStartIntent = PendingIntent.getService(mContext, REQUEST_CODE, startIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_start_task, pStartIntent);
    }

}

