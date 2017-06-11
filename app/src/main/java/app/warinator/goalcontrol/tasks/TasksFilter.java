package app.warinator.goalcontrol.tasks;

import android.os.Parcel;
import android.os.Parcelable;

import app.warinator.goalcontrol.model.ConcreteTask;

/**
 * Created by Warinator on 19.04.2017.
 */

public class TasksFilter implements Parcelable {
    public static final int ALL = -990990;
    public static final Creator<TasksFilter> CREATOR = new Creator<TasksFilter>() {
        @Override
        public TasksFilter createFromParcel(Parcel in) {
            return new TasksFilter(in);
        }

        @Override
        public TasksFilter[] newArray(int size) {
            return new TasksFilter[size];
        }
    };
    private long taskId;
    private long projectId;
    private long categoryId;
    private int priority;

    public TasksFilter(Parcel in) {
        taskId = in.readLong();
        projectId = in.readLong();
        categoryId = in.readLong();
        priority = in.readInt();
    }

    public TasksFilter() {
        taskId = projectId = categoryId = priority = ALL;
    }

    public boolean isOn(){
        return taskId != ALL || projectId != ALL || categoryId != ALL || priority != ALL;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean matches(ConcreteTask ct) {
        if ((taskId == ALL || ct.getTask().getId() == taskId) &&
                (projectId == ALL || ct.getTask().getProject() != null && ct.getTask().getProject().getId() == projectId) &&
                (categoryId == ALL || ct.getTask().getCategory() != null && ct.getTask().getCategory().getId() == categoryId) &&
                (priority == ALL || ct.getTask().getPriority().ordinal() == priority)) {
            return true;
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(taskId);
        dest.writeLong(projectId);
        dest.writeLong(categoryId);
        dest.writeInt(priority);
    }
}
