package app.warinator.goalcontrol.timer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.activity.MainActivity;
import app.warinator.goalcontrol.activity.TimerNotificationHelperActivity;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.utils.ColorUtil;
import app.warinator.goalcontrol.utils.Util;

/**
 * Created by Warinator on 26.04.2017.
 */

public class TimerNotification  {
    private Context mContext;
    private RemoteViews mNotificationView;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private ConcreteTask mTask;
    private static final int NOTIFICATION_ID = 4352;

    public TimerNotification(Context context, ConcreteTask task){
        mContext = context;
        mTask = task;
        Intent i = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(i);

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.app_icon_transp_24)
                .setOngoing(true);
        mNotifyBuilder.setContentIntent(intent);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_timer2);
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
        if (task.getTask().getWorkTime() > 0){
            mNotificationView.setViewVisibility(R.id.pb_timer, View.VISIBLE);
            mNotificationView.setProgressBar(R.id.pb_timer, 100, 0, false);
        }
        else {
            mNotificationView.setViewVisibility(R.id.pb_timer, View.INVISIBLE);
        }

        setListeners();

        mNotifyBuilder.setCustomContentView(mNotificationView);

        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }

    private void setListeners(){
        Intent startPauseIntent = new Intent(mContext, TimerBroadcastReceiver.class);
        startPauseIntent.setAction(TimerNotificationHelperActivity.ACTION_START);
        PendingIntent pStartPauseIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 2554245, startPauseIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_start_pause, pStartPauseIntent);


        Intent stopIntent = new Intent(mContext, TimerBroadcastReceiver.class);
        stopIntent.setAction(TimerNotificationHelperActivity.ACTION_STOP);
        PendingIntent pStopIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 4324623, stopIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_stop, pStopIntent);

        Intent nextAutoIntent = new Intent(mContext, TimerBroadcastReceiver.class);
        nextAutoIntent.setAction(TimerNotificationHelperActivity.ACTION_NEXT);
        PendingIntent pNextAutoIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 525323, nextAutoIntent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_next, pNextAutoIntent);

    }

    public void updateTime(long timePassed){
        String timeText;
        long workTime = mTask.getTask().getWorkTime()/1000;
        if (workTime > 0){
            long timeLeft = workTime - timePassed;
            timeText = String.format("-%s", Util.getFormattedTime(timeLeft*1000));
            int percentPassed = (int)Math.ceil(((double)(timePassed/60)/(double)(workTime/60))*100.0);
            mNotificationView.setProgressBar(R.id.pb_timer, 100, percentPassed, false);
        }
        else {
            timeText = Util.getFormattedTime(timePassed*1000);
        }
        mNotificationView.setTextViewText(R.id.tv_timer, timeText);
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
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
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }

    public void updateName(String newName){
        mNotificationView.setTextViewText(R.id.tv_task_name, newName);
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
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
