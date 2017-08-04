package app.warinator.goalcontrol.job;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import rx.Subscription;

/**
 * Класс фоновой задачи добавления задач на сегодня в список текущих
 */
public class TasksDailyJob extends Job {

    public static final String TAG = "tasks_daily";
    private Subscription mSub;

    public static void schedule() {
        schedule(true);
    }

    //Запланировать добавление новых задач в список текущих и планирование напоминаний
    //на интервал  0:00 - 0:05
    public static void schedule(boolean updateCurrent) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 0:00 - 0:05
        long startMs = TimeUnit.MINUTES.toMillis(60 - minute)
                + TimeUnit.HOURS.toMillis((24 - hour - 1) % 24);
        long endMs = startMs + TimeUnit.MINUTES.toMillis(5);

        new JobRequest.Builder(TAG)
                .setExecutionWindow(startMs, endMs)
                .setPersisted(true)
                .setUpdateCurrent(updateCurrent)
                .build()
                .schedule();
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            mSub = ConcreteTaskDAO.getDAO().addAllNecessaryToQueue()
                    .concatMap(integer -> {
                        Calendar yearAgo = Calendar.getInstance();
                        yearAgo.add(Calendar.YEAR,-1);
                        return ConcreteTaskDAO.getDAO().deleteAllBefore(yearAgo);
                    }).subscribe(integers -> {
                        mSub.unsubscribe();
                        mSub = null;
                    }, Throwable::printStackTrace);
            RemindersManager.scheduleTodayReminders(getContext());
            return Result.SUCCESS;
        } finally {
            schedule(false);
        }
    }
}
