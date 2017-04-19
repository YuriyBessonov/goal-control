package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;

import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
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
    public static final Func1<Cursor, ConcreteTask> FROM_CURSOR = cursor -> {
        Calendar calendar = null;
        long dateTime1 = cursor.getLong(cursor.getColumnIndex(DATE_TIME));
        if (dateTime1 > 0) {
            calendar = Util.calendarFromMillis(dateTime1);
        }
        long taskId = cursor.getLong(cursor.getColumnIndex(TASK_ID));
        ConcreteTask ct = new ConcreteTask(
                cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                taskId > 0 ? TaskDAO.getDAO().get(taskId).firstOrDefault(null).toBlocking().single() : null,
                calendar,
                cursor.getInt(cursor.getColumnIndex(DELAY)),
                cursor.getInt(cursor.getColumnIndex(AMOUNT_DONE)),
                cursor.getLong(cursor.getColumnIndex(TIME_SPENT))
        );
        ct.progressReal = ct.getProgressRealPercent().firstOrDefault(0).toBlocking().single();
        ct.progressExp = ct.getProgressExpPercent().firstOrDefault(0).toBlocking().single();
        return ct;
    };
    private Task task;
    private Calendar dateTime;
    private int delay;
    private int amountDone;
    private long timeSpent;

    public int getProgressReal() {
        return progressReal;
    }

    public int getProgressExp() {
        return progressExp;
    }

    private int progressReal;
    private int progressExp;

    public ConcreteTask(long id, Task task, Calendar dateTime, int delay, int amountDone, long timeSpent) {
        this.id = id;
        this.task = task;
        this.dateTime = dateTime;
        this.delay = delay;
        this.amountDone = amountDone;
        this.timeSpent = timeSpent;
    }

    public ConcreteTask() {}

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        if (task != null) {
            contentValues.put(TASK_ID, task.getId());
        }
        if (dateTime != null) {
            contentValues.put(DATE_TIME, dateTime.getTimeInMillis());
        }
        contentValues.put(DELAY, delay);
        contentValues.put(AMOUNT_DONE, amountDone);
        contentValues.put(TIME_SPENT, timeSpent);
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

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
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


    public int getRepeatTimes(){
        if (!task.isRepeatable()){
            return 1;
        }
        else {
            int repeatTimes = task.getRepeatCount();
            if (!task.isInterval()){
                repeatTimes *= task.getWeekdays().getCheckedDays().size();
            }
            return repeatTimes;
        }
    }

    public int getAmtExpected(int timesLeft){
        if (dateTime != null && Util.compareDays(Calendar.getInstance(), dateTime) < 0){
            return  0;
        }
        else {
            return (int)((double)task.getAmountTotal()
                    * (1.0 - (double)(timesLeft-1)/getRepeatTimes()));
        }
    }

    public int getAmtToday(int allDone, int timesLeft){
        if (task.getAmountOnce() > 0){
            return task.getAmountOnce();
        }
        else {
            return (int)Math.ceil((double)(task.getAmountTotal() - allDone)/timesLeft);
        }
    }

    public Observable<Integer> getProgressRealPercent(){
        Task.ProgressTrackMode mode = task.getProgressTrackMode();
        switch (mode){
            case MARK:
                return Observable.just((amountDone > 0) ? 100 : 0);
            case LIST:
                return CheckListItemDAO.getDAO().getAllForTask(task.getId(), true).map(checkListItems -> {
                    int compl = 0;
                    for (CheckListItem item : checkListItems) {
                        if (item.isCompleted()){
                            compl++;
                        }
                    }
                    return Util.fracToPercent((double)compl/(double)checkListItems.size());
                });
            case SEQUENCE:
                break;
            default:
                return ConcreteTaskDAO.getDAO().getTotalAmountDone(task.getId())
                        .map(allDone -> Util.fracToPercent((double)allDone/(double)task.getAmountTotal()));
        }
        return Observable.just(0);
    }

    public Observable<Integer> getProgressExpPercent(){
        Task.ProgressTrackMode mode = task.getProgressTrackMode();
        switch (mode){
            case MARK:
                return Observable.just(isOverdue() ? 100 : 0);
            case LIST:
                return Observable.just(0);
            case SEQUENCE:
                break;
            default:
                return ConcreteTaskDAO.getDAO().getTimesLeftStartingToday(task.getId())
                        .map(timesLeft -> Util.fracToPercent((double)getAmtExpected(timesLeft)/(double)task.getAmountTotal()));
        }
        return Observable.just(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nid: ");
        sb.append(getId());
        sb.append("\nИмя: ");
        sb.append(getTask().getName());
        sb.append("\nПриоритет: ");
        sb.append(getTask().getPriority().toString());
        sb.append("\nДата: ");
        if (getDateTime() != null){
            sb.append(getDateTime().getTimeInMillis());
        }
        else {
            sb.append(" - ");
        }
        sb.append("\nПроект: ");
        if (getTask().getProject() != null){
            sb.append(getTask().getProject().getName());
        }
        else {
            sb.append(" - ");
        }
        sb.append("\nКатегория: ");
        if (getTask().getCategory() != null){
            sb.append(getTask().getCategory().getName());
        }
        else {
            sb.append(" - ");
        }
        sb.append("\n");
        return sb.toString();
    }
}
