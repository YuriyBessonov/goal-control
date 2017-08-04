package app.warinator.goalcontrol.timer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.activity.MainActivity;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.utils.Util;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_START;

/**
 * Напоминание о задаче
 */
public class TaskReminderNotification extends BaseTaskNotification {
    public TaskReminderNotification(Context context, ConcreteTask task) {
        super(context, task, new Intent(context, MainActivity.class));
        mNotificationId = (int)task.getId();
        mNotifyBuilder.setOngoing(false);
        mNotification = mNotifyBuilder.build();
        setNoisy(true);

    }

    @Override
    public void setupView(ConcreteTask task) {
        super.setupView(task);
        mNotificationView.setViewVisibility(R.id.pb_timer, View.GONE);
        mNotificationView.setViewVisibility(R.id.la_controls, View.GONE);
        mNotificationView.setViewVisibility(R.id.tv_timer, View.GONE);
        mNotificationView.setViewVisibility(R.id.btn_start_task,
                task.getTask().getChronoTrackMode() != Task.ChronoTrackMode.NONE ?
                View.VISIBLE : View.GONE);
        if (task.getTask().isWithTime()) {
            mNotificationView.setViewVisibility(R.id.la_task_time, View.VISIBLE);
            mNotificationView.setTextViewText(R.id.tv_task_time,
                    Util.getFormattedTime(task.getDateTime()));
        }
    }

    @Override
    protected void setupListeners(long taskId) {
        Intent startIntent = new Intent(mContext, TimerNotificationService.class);
        startIntent.setAction(ACTION_START);
        startIntent.putExtra(TimerNotificationService.ARG_TASK_ID, taskId);
        PendingIntent pStartIntent = PendingIntent.getService(mContext,
                (int)taskId, startIntent, FLAG_UPDATE_CURRENT);
        mNotificationView.setOnClickPendingIntent(R.id.btn_start_task, pStartIntent);
    }

}

