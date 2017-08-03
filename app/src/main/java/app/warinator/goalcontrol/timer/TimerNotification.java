package app.warinator.goalcontrol.timer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.view.View;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.activity.MainActivity;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.utils.Util;

import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_AUTO_FORWARD;
import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_NEXT_TASK;
import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_START_PAUSE;
import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_STOP_NEXT;


/**
 * Таймер в панели уведомлений
 */
public class TimerNotification extends BaseTaskNotification {
    public static final int NOTIFICATION_ID = 83626;
    private boolean mAutoForwardEnabled;

    public TimerNotification(Context context, ConcreteTask task, boolean autoForwardEnabled) {
        super(context, task, MainActivity.getTaskOptionsIntent(context, task.getId()));
        mNotificationId = NOTIFICATION_ID;
        mAutoForwardEnabled = autoForwardEnabled;
        int color;
        if (mAutoForwardEnabled) {
            color = ContextCompat.getColor(mContext, R.color.colorPrimary);
        } else {
            color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);
        }
        mNotificationView.setImageViewBitmap(R.id.btn_auto_forward, getBitmap(mContext, R.drawable.ic_forward, color));
    }

    @Override
    public void setupView(ConcreteTask task) {
        super.setupView(task);
        if (task.getTask().getWorkTime() > 0) {
            mNotificationView.setViewVisibility(R.id.pb_timer, View.VISIBLE);
            mNotificationView.setProgressBar(R.id.pb_timer, 100, 0, false);
        } else {
            mNotificationView.setViewVisibility(R.id.pb_timer, View.INVISIBLE);
        }
    }

    @Override
    protected void setupListeners(long taskId) {
        Intent startPauseIntent = new Intent(mContext, TimerNotificationService.class);
        startPauseIntent.setAction(ACTION_START_PAUSE);
        PendingIntent pStartPauseIntent =
                PendingIntent.getService(mContext, 0, startPauseIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_start_pause, pStartPauseIntent);


        Intent stopIntent = new Intent(mContext, TimerNotificationService.class);
        stopIntent.setAction(ACTION_STOP_NEXT);
        PendingIntent pStopIntent = PendingIntent.getService(mContext, 0, stopIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_stop_next, pStopIntent);

        Intent autoForwardIntent = new Intent(mContext, TimerNotificationService.class);
        autoForwardIntent.setAction(ACTION_AUTO_FORWARD);
        PendingIntent pAutoForwardIntent =
                PendingIntent.getService(mContext, 0, autoForwardIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_auto_forward, pAutoForwardIntent);

        Intent nextTaskIntent =
                new Intent(mContext, TimerNotificationService.class);
        nextTaskIntent.setAction(ACTION_NEXT_TASK);
        PendingIntent pNextTaskIntent = PendingIntent.getService(mContext, 0, nextTaskIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.iv_task_icon, pNextTaskIntent);

    }

    @Override
    public void refresh() {
        super.refresh();
        if (mIsNoisy) {
            setNoisy(false);
        }
    }

    @Override
    public void show(Service notificationService) {
        super.show(notificationService);
        if (mIsNoisy) {
            setNoisy(false);
        }
    }

    public void updateName(String newName, int colorRes) {
        mNotificationView.setTextViewText(R.id.tv_task_name, newName);
        mNotificationView.setTextColor(R.id.tv_task_name, ContextCompat.getColor(mContext, colorRes));
        refresh();
    }

    //Обновить время таймера
    public void updateTime(long timePassed, long timeNeed) {
        String timeText;
        if (timeNeed > 0) {
            long timeLeft = timeNeed - timePassed;
            timeText = String.format("-%s", Util.getFormattedTime(timeLeft * 1000));
            int percentPassed = (int) Math.ceil(((double) (timePassed / 60) /
                    (double) (timeNeed / 60)) * 100.0);
            mNotificationView.setProgressBar(R.id.pb_timer, 100, percentPassed, false);
        } else {
            timeText = Util.getFormattedTime(timePassed * 1000);
        }
        mNotificationView.setTextViewText(R.id.tv_timer, timeText);
        refresh();
    }

    //Обновить состояние таймера
    public void updateState(TaskTimer.TimerState state) {
        Bitmap bmp;
        if (state == TaskTimer.TimerState.RUNNING) {
            bmp = getBitmap(mContext, R.drawable.ic_pause);
        } else {
            bmp = getBitmap(mContext, R.drawable.ic_play_accent);
        }
        mNotificationView.setImageViewBitmap(R.id.btn_start_pause, bmp);

        if (state == TaskTimer.TimerState.STOPPED) {
            bmp = getBitmap(mContext, R.drawable.ic_skip_next);
        } else {
            bmp = getBitmap(mContext, R.drawable.ic_stop);
        }
        mNotificationView.setImageViewBitmap(R.id.btn_stop_next, bmp);
        refresh();
    }

    //Обновить состояние режима автоперехода к следующей задаче
    public void updateAutoForward(boolean enabled) {
        int color;
        if (enabled) {
            color = ContextCompat.getColor(mContext, R.color.colorPrimary);
        } else {
            color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);
        }
        mNotificationView.setImageViewBitmap(R.id.btn_auto_forward, getBitmap(mContext,
                R.drawable.ic_forward, color));
        refresh();
    }

    public void showDetached(){
        mNotifyBuilder.setOngoing(false);
        mNotification = mNotifyBuilder.build();
        refresh();
    }

}



