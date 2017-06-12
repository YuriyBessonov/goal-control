package app.warinator.goalcontrol.job;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.timer.TaskReminderNotification;
import app.warinator.goalcontrol.utils.Util;

/**
 * Created by Warinator on 29.04.2017.
 */

public class TaskReminderJob extends Job {

    public static final String TAG = "task_reminder_tag";
    private static final String ARG_TASK_ID = "task_id";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        long taskId = params.getExtras().getLong(ARG_TASK_ID, 0);
        if (taskId > 0){
            ConcreteTaskDAO.getDAO().get(taskId).doOnError(Throwable::printStackTrace).subscribe(concreteTask -> {
                Calendar tomorrow = Util.justDate(Calendar.getInstance());
                tomorrow.add(Calendar.DATE, 1);
                if (!concreteTask.isRemoved() && concreteTask.getDateTime().compareTo(tomorrow) < 0){
                    TaskReminderNotification taskReminderNotification = new TaskReminderNotification(getContext(), concreteTask);
                    taskReminderNotification.refresh();
                }
            });
        }
        return Result.SUCCESS;
    }

    public static void schedule(long taskId, long when) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putLong(ARG_TASK_ID, taskId);

        long now = Calendar.getInstance().getTimeInMillis();
        when -= when % TimeUnit.MINUTES.toMillis(1);
        long delay = when - now;

        if (delay > 0){
            new JobRequest.Builder(TAG)
                    .setExact(delay)
                    .setExtras(extras)
                    .setPersisted(true)
                    .build()
                    .schedule();
        }
    }
}

