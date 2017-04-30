package app.warinator.goalcontrol.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Warinator on 30.04.2017.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent newIntent = new Intent(context, WakefulReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            //// TODO: use calendar.add(Calendar.SECOND,MINUTE,HOUR, int);
            //calendar.add(Calendar.SECOND, 10);

            //ALWAYS recompute the calendar after using add, set, roll
            Date date = calendar.getTime();

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), alarmIntent);
        }
        */
    }
}
