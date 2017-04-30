package app.warinator.goalcontrol.timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.activity.MainActivity;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.utils.ColorUtil;
import app.warinator.goalcontrol.utils.Util;

import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_AUTO_FORWARD;
import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_NEXT_TASK;
import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_START_PAUSE;
import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_STOP_NEXT;

/**
 * Created by Warinator on 26.04.2017.
 */

public class TimerNotification {
    private Context mContext;
    private RemoteViews mNotificationView;
    private NotificationCompat.Builder mNotifyBuilder;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private boolean mIsNoisy;
    public static final int NOTIFICATION_ID = 83626;

    public TimerNotification(Context context, ConcreteTask task, boolean autoForwardEnabled){
        mContext = context;
        Intent i = MainActivity.getTaskOptionsIntent(mContext, task.getId());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(i);
        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent intent = PendingIntent.getActivity(mContext, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifyBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.app_icon_transp_24)
                .setAutoCancel(true)
                .setOngoing(true);

        mNotifyBuilder.setContentIntent(intent);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_timer);
        mNotificationView.setTextViewText(R.id.tv_task_name, task.getTask().getName());
        int bgCol;
        if (task.getTask().getProject() != null){
            int colInd = task.getTask().getProject().getColor();
            bgCol = ColorUtil.getProjectColor(colInd, mContext);
        }
        else {
            bgCol = ColorUtil.getProjectColor(ColorUtil.COLOR_DEFAULT, mContext);
        }
        Bitmap iconBgr = getBitmap(mContext, R.drawable.filled_circle_40, bgCol);
        mNotificationView.setImageViewBitmap(R.id.iv_task_icon_bgr, iconBgr);
        int icInd = task.getTask().getIcon();
        IconicsDrawable icDrawable = new IconicsDrawable(mContext, GoogleMaterial.Icon.values()[icInd]);
        icDrawable.setAlpha(141);
        icDrawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorGreyDark), PorterDuff.Mode.SRC_ATOP);
        mNotificationView.setImageViewBitmap(R.id.iv_task_icon, icDrawable.toBitmap());
        int color;
        if (autoForwardEnabled){
            color = ContextCompat.getColor(mContext, R.color.colorPrimary);
        }
        else {
            color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);
        }
        mNotificationView.setImageViewBitmap(R.id.btn_auto_forward, getBitmap(mContext, R.drawable.ic_forward, color));
        if (task.getTask().getWorkTime() > 0){
            mNotificationView.setViewVisibility(R.id.pb_timer, View.VISIBLE);
            mNotificationView.setProgressBar(R.id.pb_timer, 100, 0, false);
        }
        else {
            mNotificationView.setViewVisibility(R.id.pb_timer, View.INVISIBLE);
        }

        setListeners();

        mNotifyBuilder.setCustomContentView(mNotificationView);
        mNotification = mNotifyBuilder.build();
        Log.v("THE_TIMER","NOTIFICATION CREATED");
    }

    public void refresh(){
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        if (mIsNoisy){
           setNoisy(false);
        }
    }

    public void show(Service notificationService){
        notificationService.startForeground(NOTIFICATION_ID, mNotification);
        if (mIsNoisy){
            setNoisy(false);
        }
    }

    public void cancel(){
        mNotificationManager.cancel(NOTIFICATION_ID);
    }


    public void setNoisy(boolean noisy){
        mIsNoisy = noisy;
        if (noisy){
            long[] v = {500,1000};
            mNotification.vibrate = v;
            mNotification.sound = RingtoneManager.getDefaultUri
                    (RingtoneManager.TYPE_NOTIFICATION);
        }
        else {
            mNotification.defaults = 0;
            mNotification.sound = null;
            mNotification.vibrate = null;
        }
    }

    private void setListeners(){
        Intent startPauseIntent = new Intent(mContext, TimerNotificationService.class);
        startPauseIntent.setAction(ACTION_START_PAUSE);
        PendingIntent pStartPauseIntent = PendingIntent.getService(mContext, 0, startPauseIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_start_pause, pStartPauseIntent);


        Intent stopIntent = new Intent(mContext, TimerNotificationService.class);
        stopIntent.setAction(ACTION_STOP_NEXT);
        PendingIntent pStopIntent = PendingIntent.getService(mContext, 0, stopIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_stop_next, pStopIntent);

        Intent autoForwardIntent = new Intent(mContext, TimerNotificationService.class);
        autoForwardIntent.setAction(ACTION_AUTO_FORWARD);
        PendingIntent pAutoForwardIntent = PendingIntent.getService(mContext, 0, autoForwardIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_auto_forward, pAutoForwardIntent);

        Intent nextTaskIntent = new Intent(mContext, TimerNotificationService.class);
        nextTaskIntent.setAction(ACTION_NEXT_TASK);
        PendingIntent pNextTaskIntent = PendingIntent.getService(mContext, 0, nextTaskIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.iv_task_icon, pNextTaskIntent);

    }

    public void updateTime(long timePassed, long timeNeed){
        String timeText;
        if (timeNeed > 0){
            long timeLeft = timeNeed - timePassed;
            timeText = String.format("-%s", Util.getFormattedTime(timeLeft*1000));
            int percentPassed = (int)Math.ceil(((double)(timePassed/60)/(double)(timeNeed/60))*100.0);
            mNotificationView.setProgressBar(R.id.pb_timer, 100, percentPassed, false);
        }
        else {
            timeText = Util.getFormattedTime(timePassed*1000);
        }
        mNotificationView.setTextViewText(R.id.tv_timer, timeText);
        refresh();
    }

    public void updateState(TaskTimer.TimerState state){
        Bitmap bmp;
        if (state == TaskTimer.TimerState.RUNNING){
            bmp = getBitmap(mContext, R.drawable.ic_pause);
        }
        else {
            bmp = getBitmap(mContext, R.drawable.ic_play_accent);
        }
        mNotificationView.setImageViewBitmap(R.id.btn_start_pause, bmp);

        if (state == TaskTimer.TimerState.STOPPED){
            bmp = getBitmap(mContext, R.drawable.ic_skip_next);
        }
        else {
            bmp = getBitmap(mContext, R.drawable.ic_stop);
        }
        mNotificationView.setImageViewBitmap(R.id.btn_stop_next, bmp);
        refresh();
    }

    public void updateAutoForward(boolean enabled){
        int color;
        if (enabled){
            color = ContextCompat.getColor(mContext, R.color.colorPrimary);
        }
        else {
            color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);
        }
        mNotificationView.setImageViewBitmap(R.id.btn_auto_forward, getBitmap(mContext, R.drawable.ic_forward, color));
        refresh();
    }

    public void updateName(String newName){
        mNotificationView.setTextViewText(R.id.tv_task_name, newName);
        refresh();
    }


    private Bitmap getBitmap(Context context, int drawableRes, int color) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        drawable.draw(canvas);
        return bitmap;
    }

    private Bitmap getBitmap(Context context, int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}

