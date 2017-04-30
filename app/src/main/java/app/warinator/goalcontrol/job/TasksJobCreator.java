package app.warinator.goalcontrol.job;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by Warinator on 20.04.2017.
 */

public class TasksJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag){
            case QueuedTasksJob.TAG:
                return new QueuedTasksJob();
            case TaskAlarmJob.TAG:
                return new TaskAlarmJob();
            default:
                return null;
        }
    }
}
