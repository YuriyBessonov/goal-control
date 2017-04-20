package app.warinator.goalcontrol;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by Warinator on 20.04.2017.
 */

public class TasksJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag){
            case CurrentTasksJob.TAG:
                return new CurrentTasksJob();
            default:
                return null;
        }
    }
}
