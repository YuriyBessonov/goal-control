package app.warinator.goalcontrol;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import rx.Subscription;

/**
 * Created by Warinator on 20.04.2017.
 */

public class CurrentTasksJob extends Job {

    public static final String TAG = "current_tasks_tag";

    public static void schedule(){
        schedule(true);
    }

    private Subscription mSub;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            mSub = QueuedDAO.getDAO().addAllTodayTasks()
                    .subscribe(aLong -> {
                        mSub.unsubscribe();
                        mSub = null;
                    });
            return Result.SUCCESS;
        } finally {
            schedule(false);
        }
    }

    public static void schedule(boolean updateCurrent) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 0:00 - 1:00
        long startMs = TimeUnit.MINUTES.toMillis(60 - minute)
                + TimeUnit.HOURS.toMillis((24 - hour - 1) % 24);
        long endMs = startMs + TimeUnit.HOURS.toMillis(1);


        new JobRequest.Builder(TAG)
                .setExecutionWindow(startMs, endMs)
                .setPersisted(true)
                .setUpdateCurrent(updateCurrent)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule();
    }
}
