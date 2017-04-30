package app.warinator.goalcontrol.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;
import java.util.Date;

import app.warinator.goalcontrol.model.main.ConcreteTask;

/**
 * Created by Warinator on 30.04.2017.
 */

public class WakefulReceiver extends WakefulBroadcastReceiver {

    private AlarmManager mAlarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        WakefulReceiver.completeWakefulIntent(intent);
    }

    public void setAlarm(Context context, ConcreteTask ct) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WakefulReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, (int) ct.getId(), intent, 0);

        long alarmTime = ct.getDateTime().getTimeInMillis() - ct.getTask().getReminder().getTimeInMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarmTime);

        Date date = calendar.getTime();

        mAlarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), alarmIntent);


        /*
        //// TODO: you may need to reference the context by ApplicationActivity.class
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
                */
    }

    public void cancelAlarm(Context context, ConcreteTask ct) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WakefulReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, (int)ct.getId(), intent, 0);
        mAlarmManager.cancel(alarmIntent);

        /*
        //// TODO: you may need to reference the context by ApplicationActivity.class
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        */
    }
}
