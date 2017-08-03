package app.warinator.goalcontrol.model;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.utils.Util;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.AMOUNT_DONE;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.DATE_TIME;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.IS_REMOVED;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.QUEUE_POS;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TASK_ID;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TIME_SPENT;

/**
 * Назначенная задача
 */
public class ConcreteTask extends BaseModel {

    public static final int QUEUE_POS_NONE = -1;
    public static final int QUEUE_POS_REMOVED = -2;

    public static final Func1<Cursor, ConcreteTask> FROM_CURSOR = cursor -> {
        Calendar calendar = null;
        long dateTime1 = cursor.getLong(cursor.getColumnIndex(DATE_TIME));
        if (dateTime1 > 0) {
            calendar = Util.calendarFromMillis(dateTime1);
        }
        long taskId = cursor.getLong(cursor.getColumnIndex(TASK_ID));
        Task task = null;
        if (taskId > 0) {
            task = new Task();
            task.setId(taskId);
        }
        ConcreteTask ct = new ConcreteTask(
                cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                task,
                calendar,
                cursor.getInt(cursor.getColumnIndex(AMOUNT_DONE)),
                cursor.getLong(cursor.getColumnIndex(TIME_SPENT)),
                cursor.getInt(cursor.getColumnIndex(QUEUE_POS)),
                cursor.getInt(cursor.getColumnIndex(IS_REMOVED)) > 0
        );
        return ct;
    };

    private Task task;
    private Calendar dateTime;
    private int amountDone;
    private long timeSpent;
    private int queuePos;
    private boolean isRemoved;
    private int mAmtNeedTotal;
    private int mAmtDoneTotal;
    private int mTimesBefore;
    private int mTimesTotal;
    private State mState = State.NORMAL;

    public ConcreteTask(long id, Task task, Calendar dateTime, int amountDone, long timeSpent,
                        int queuePos, boolean isRemoved) {
        this.id = id;
        this.task = task;
        this.dateTime = dateTime;
        this.amountDone = amountDone;
        this.timeSpent = timeSpent;
        this.queuePos = queuePos;
        this.isRemoved = isRemoved;
    }

    public ConcreteTask() {
    }

    public State getState() {
        return mState;
    }

    public void setState(State mState) {
        this.mState = mState;
    }

    public int getAmtNeedTotal() {
        return mAmtNeedTotal;
    }

    public void setAmtNeedTotal(int amtNeedTotal) {
        mAmtNeedTotal = amtNeedTotal;
    }

    public int getTimesBefore() {
        return mTimesBefore;
    }

    public void setTimesBefore(int timesBefore) {
        mTimesBefore = timesBefore;
    }

    public int getTimesTotal() {
        return mTimesTotal;
    }

    public void setTimesTotal(int timesTotal) {
        mTimesTotal = timesTotal;
    }

    public int getAmtDoneTotal() {
        return mAmtDoneTotal;
    }

    public void setAmtDoneTotal(int amtDoneTotal) {
        mAmtDoneTotal = amtDoneTotal;
    }

    public int getQueuePos() {
        return queuePos;
    }

    public void setQueuePos(int queuePos) {
        this.queuePos = queuePos;
    }

    public boolean isQueued() {
        return queuePos >= 0;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        if (task != null) {
            contentValues.put(TASK_ID, task.getId());
        }
        if (dateTime != null) {
            contentValues.put(DATE_TIME, dateTime.getTimeInMillis());
        }
        contentValues.put(AMOUNT_DONE, amountDone);
        contentValues.put(TIME_SPENT, timeSpent);
        contentValues.put(QUEUE_POS, queuePos);
        contentValues.put(IS_REMOVED, isRemoved);
        return contentValues;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    public void setDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
    }

    public int getAmountDone() {
        return amountDone;
    }

    public void setAmountDone(int amountDone) {
        this.amountDone = amountDone;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public boolean isOverdue() {
        return dateTime != null && Util.dayIsInThePast(dateTime);
    }

    public int getAmtExpected() {
        double amtAvg = (double) mAmtNeedTotal / mTimesTotal;
        return (int) ((mTimesBefore + 1) * amtAvg);
    }

    public int getAmtToday() {
        return task.getAmountOnce() > 0 ? task.getAmountOnce() :
                Math.max(getAmtExpected() - mAmtDoneTotal + getAmountDone(), 0);
        /*
        //Альтернативный вариант
        if (task.getAmountOnce() > 0) {
            return task.getAmountOnce();
        } else {
            int timesLeft = mTimesTotal - mTimesBefore;
            return (int) Math.ceil((double) (mAmtNeedTotal - mAmtDoneTotal + amountDone) / timesLeft);
        }
        */
    }

    public int getProgressReal() {
        switch (task.getProgressTrackMode()) {
            case MARK:
                return Util.fracToPercent((double) getAmtDoneTotal() / getAmtNeedTotal());
            case SEQUENCE:
                return getAmountDone() > 0 ? 100 : 0;
            case LIST:
                return Util.fracToPercent((double) getAmtDoneTotal() / getAmtNeedTotal());
            default:
                return Util.fracToPercent((double) getAmtDoneTotal() / getAmtNeedTotal());
        }
    }

    public int getProgressExp() {
        if (task.getProgressTrackMode() == Task.ProgressTrackMode.MARK) {
            return Util.fracToPercent((double) (getTimesBefore() + 1) / getTimesTotal());
        } else if (task.getProgressTrackMode() == Task.ProgressTrackMode.SEQUENCE ||
                task.getProgressTrackMode() == Task.ProgressTrackMode.LIST) {
            return 0;
        } else {
            return Util.fracToPercent((double) getAmtExpected() / getAmtNeedTotal());
        }
    }

    public enum State {
        NORMAL, REMOVING, PROCESSED
    }
}
