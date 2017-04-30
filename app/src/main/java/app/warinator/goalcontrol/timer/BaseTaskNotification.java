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
import android.widget.RemoteViews;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.activity.MainActivity;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.utils.ColorUtil;

/**
 * Created by Warinator on 26.04.2017.
 */

public abstract class BaseTaskNotification {
    protected Context mContext;
    protected RemoteViews mNotificationView;
    private NotificationManager mNotificationManager;
    protected Notification mNotification;
    protected int mNotificationId;
    private boolean mIsNoisy;
    NotificationCompat.Builder mNotifyBuilder;

    public BaseTaskNotification(Context context, ConcreteTask task, Intent clickIntent){
        mContext = context;

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(clickIntent);
        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotifyBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.app_icon_transp_24)
                .setAutoCancel(true)
                .setOngoing(true);

        mNotifyBuilder.setContentIntent(intent);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        setupView(task);
        setupListeners(task.getId());

        mNotifyBuilder.setCustomContentView(mNotificationView);
        mNotification = mNotifyBuilder.build();
    }

    public void setupView(ConcreteTask task){
        mNotificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_task);
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
    }

    protected abstract void setupListeners(long taskId);

    public final void refresh(){
        mNotificationManager.notify(mNotificationId, mNotification);
    }

    public final void show(Service notificationService){
        notificationService.startForeground(mNotificationId, mNotification);
    }

    public void updateName(String newName){
        mNotificationView.setTextViewText(R.id.tv_task_name, newName);
        refresh();
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

    protected Bitmap getBitmap(Context context, int drawableRes, int color) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        drawable.draw(canvas);
        return bitmap;
    }

    protected Bitmap getBitmap(Context context, int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}

