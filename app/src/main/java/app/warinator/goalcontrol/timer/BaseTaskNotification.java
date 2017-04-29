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

public class BaseTaskNotification {
    private Context mContext;
    private RemoteViews mNotificationView;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    public static final int NOTIFICATION_ID = 83626;

    public BaseTaskNotification(Context context, ConcreteTask task, Intent clickIntent){
        mContext = context;

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(clickIntent);
        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.app_icon_transp_24)
                .setAutoCancel(true)
                .setOngoing(true);

        notifyBuilder.setContentIntent(intent);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        setupView(task);

        notifyBuilder.setCustomContentView(mNotificationView);
        mNotification = notifyBuilder.build();
    }

    public void setupView(ConcreteTask task){
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
    }

    public void refresh(){
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public void show(Service notificationService){
        notificationService.startForeground(NOTIFICATION_ID, mNotification);
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

