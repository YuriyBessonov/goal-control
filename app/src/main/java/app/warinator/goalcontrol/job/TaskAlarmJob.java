package app.warinator.goalcontrol.job;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.timer.TaskNotification;
import rx.functions.Action1;

/**
 * Created by Warinator on 29.04.2017.
 */

public class TaskAlarmJob extends Job {

    public static final String TAG = "task_alarm_tag";
    private static final String ARG_TASK_ID = "task_id";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        /*
        long taskId = params.getExtras().getLong(ARG_TASK_ID, 0);
        if (taskId > 0){
            Intent serviceIntent = new Intent(getContext(), TimerNotificationService.class);
            serviceIntent.setAction(ACTION_SHOW_NOTIFICATION);
            serviceIntent.putExtra(TimerNotificationService.ARG_TASK_ID, taskId);
            getContext().startService(serviceIntent);
        }*/

        /*
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), MainActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(getContext())
                .setContentTitle("Job Demo")
                .setContentText("Periodic job ran")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_timer)
                .setShowWhen(true)
                .setColor(Color.GREEN)
                .setLocalOnly(true)
                .build();

        NotificationManagerCompat.from(getContext()).notify(new Random().nextInt(), notification);
        */
        ConcreteTaskDAO.getDAO().get(43L).subscribe(new Action1<ConcreteTask>() {
            @Override
            public void call(ConcreteTask concreteTask) {
                TaskNotification taskNotification = new TaskNotification(getContext(), concreteTask);
                taskNotification.refresh();
            }
        });

        return Result.SUCCESS;
    }

    public static void schedule(long taskId, long when) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putLong(ARG_TASK_ID, taskId);

        long startMs = TimeUnit.SECONDS.toMillis(30);
        long endMs = startMs + TimeUnit.SECONDS.toMillis(30);
        new JobRequest.Builder(TAG)
                .setExecutionWindow(startMs, endMs)
                //.setExact(300000L)
                .setExtras(extras)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }
}
