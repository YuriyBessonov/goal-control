package app.warinator.goalcontrol.job;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class TasksJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case TasksDailyJob.TAG:
                return new TasksDailyJob();
            case TaskReminderJob.TAG:
                return new TaskReminderJob();
            default:
                return null;
        }
    }
}
