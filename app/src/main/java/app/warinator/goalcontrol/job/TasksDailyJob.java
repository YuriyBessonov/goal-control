package app.warinator.goalcontrol.job;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import rx.Subscription;

/**
 * Created by Warinator on 20.04.2017.
 */

public class TasksDailyJob extends Job {

    public static final String TAG = "tasks_daily";

    public static void schedule(){
        schedule(true);
    }

    private Subscription mSub;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            mSub = ConcreteTaskDAO.getDAO().addAllForTodayToQueue().subscribe(integer -> {
                mSub.unsubscribe();
                mSub = null;
            });
            RemindersManager.scheduleTodayReminders(getContext());
            return Result.SUCCESS;
        } finally {
            schedule(false);
        }
    }

    public static void schedule(boolean updateCurrent) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 0:00 - 0:10
        long startMs = TimeUnit.MINUTES.toMillis(60 - minute)
                + TimeUnit.HOURS.toMillis((24 - hour - 1) % 24);
        long endMs = startMs + TimeUnit.MINUTES.toMillis(10);

        new JobRequest.Builder(TAG)
                .setExecutionWindow(startMs, endMs)
                .setPersisted(true)
                .setUpdateCurrent(updateCurrent)
                .build()
                .schedule();
    }
}
