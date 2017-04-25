package app.warinator.goalcontrol;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.database.DbManager;
import rx.Subscription;

/**
 * Created by Warinator on 20.04.2017.
 */

public class QueuedTasksJob extends Job {

    public static final String TAG = "current_tasks_tag";

    public static void schedule(){
        schedule(true);
    }

    private Subscription mSub;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            Log.v("#JOB#", "do it");
            SQLiteDatabase db = DbManager.getInstance(getContext().getApplicationContext()).getDatabase().getReadableDatabase();
            QueuedDAO.getDAO().addAllTodayTasks().toBlocking().single();
            Log.v("#JOB#", "done");
            return Result.SUCCESS;
        } finally {
            schedule(false);
        }
    }

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
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule();
    }
}
