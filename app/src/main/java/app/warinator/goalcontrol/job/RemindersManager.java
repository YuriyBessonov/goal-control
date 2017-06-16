package app.warinator.goalcontrol.job;

import android.content.Context;

import java.util.Calendar;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.utils.PrefUtils;
import app.warinator.goalcontrol.utils.Util;
import rx.Subscription;

/**
 * Created by Warinator on 30.04.2017.
 */

public class RemindersManager {
    private static Subscription mSub;

    public static void scheduleTodayReminders(Context context){
        PrefUtils pref = new PrefUtils(context);
        Calendar today = Util.justDate(Calendar.getInstance());
        if (Util.compareDays(today, Util.justDate(pref.getLastLaunched())) > 0){
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.setTimeInMillis(today.getTimeInMillis());
            tomorrow.add(Calendar.DATE, 1);
            mSub = ConcreteTaskDAO.getDAO().getAllForDateRange(today, tomorrow, true).map(tasks -> {
                for (ConcreteTask ct : tasks){
                    if (ct.getDateTime() != null && ct.getTask().isWithTime()){
                        scheduleReminder(ct);
                    }
                }
                return tasks;
            }).subscribe(tasks -> {
                mSub.unsubscribe();
                mSub = null;
            });
            pref.setLastLaunched(today.getTimeInMillis());
        }
    }

    public static void scheduleReminder(ConcreteTask ct){
        TaskReminderJob.schedule(ct.getId(), ct.getDateTime().getTimeInMillis());
    }

}