package app.warinator.goalcontrol.job;

import android.content.Context;

import java.util.Calendar;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.utils.PrefUtils;
import app.warinator.goalcontrol.utils.Util;
import rx.Subscription;

/**
 * Класс для планирования напоминаний
 */
public class RemindersManager {
    private static Subscription mSub;

    //Запланировать напоминания для задач на сегодня
    public static void scheduleTodayReminders(Context context) {
        PrefUtils pref = new PrefUtils(context);
        Calendar today = Util.justDate(Calendar.getInstance());
        if (Util.compareDays(today, Util.justDate(pref.getLastLaunched())) > 0) {
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.setTimeInMillis(today.getTimeInMillis());
            tomorrow.add(Calendar.DATE, 1);
            mSub = ConcreteTaskDAO.getDAO().getAllForDateRange(today, tomorrow, true).map(tasks -> {
                for (ConcreteTask ct : tasks) {
                    if (ct.getDateTime() != null && ct.getTask().getReminder() >= 0) {
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

    //Запланировать напоминание для задачи
    public static void scheduleReminder(ConcreteTask ct) {
        long remTime = ct.getDateTime().getTimeInMillis() -
                ct.getTask().getReminder();
        TaskReminderJob.schedule(ct.getId(), remTime);
    }

}
