package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;

import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.utils.Util;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.AMOUNT_DONE;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.DATE_TIME;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.DELAY;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TASK_ID;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TIME_SPENT;

/**
 * Created by Warinator on 07.04.2017.
 */

public class ConcreteTask extends BaseModel {
    private Task task;
    private Calendar dateTime;
    private int delay;
    private int amountDone;
    private long timeSpent;

    public ConcreteTask(long id, Task task, Calendar dateTime, int delay, int amountDone, long timeSpent) {
        this.id = id;
        this.task = task;
        this.dateTime = dateTime;
        this.delay = delay;
        this.amountDone = amountDone;
        this.timeSpent = timeSpent;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        if (task != null){
            contentValues.put(TASK_ID, task.getId());
        }
        if (dateTime != null){
            contentValues.put(DATE_TIME, dateTime.getTimeInMillis());
        }
        contentValues.put(DELAY, delay);
        contentValues.put(AMOUNT_DONE, amountDone);
        contentValues.put(TIME_SPENT, timeSpent);
        return contentValues;
    }

    public static final Func1<Cursor, ConcreteTask> FROM_CURSOR = new Func1<Cursor, ConcreteTask>() {
        @Override
        public ConcreteTask call(Cursor cursor) {
            Calendar calendar = null;
            long dateTime = cursor.getLong(cursor.getColumnIndex(DATE_TIME));
            if (dateTime > 0){
                calendar = Util.calendarFromMillis(dateTime);
            }
            long taskId = cursor.getLong(cursor.getColumnIndex(TASK_ID));
            return new ConcreteTask(
                    cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                    taskId > 0 ? TaskDAO.getDAO().get(taskId).firstOrDefault(null).toBlocking().single() : null,
                    calendar,
                    cursor.getInt(cursor.getColumnIndex(DELAY)),
                    cursor.getInt(cursor.getColumnIndex(AMOUNT_DONE)),
                    cursor.getLong(cursor.getColumnIndex(TIME_SPENT))
            );
        }
    };

    public Task getTask() {
        return task;
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    public int getDelay() {
        return delay;
    }

    public int getAmountDone() {
        return amountDone;
    }

    public long getTimeSpent() {
        return timeSpent;
    }
}
