package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;

import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.database.DAO.TrackUnitDAO;
import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.utils.Util;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.TaskCols.AMOUNT_ONCE;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.AMOUNT_TOTAL;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.BIG_BREAK_EVERY;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.BIG_BREAK_TIME;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.CATEGORY_ID;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.CHRONO_MODE;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.DATE_BEGIN;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.ICON;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.INTERVALS_COUNT;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.INTERVAL_VALUE;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.IS_INTERVAL;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.IS_REPEATABLE;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.NAME;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.NOTE;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.PRIORITY;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.PROJECT_ID;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.REMINDER;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.REPEAT_COUNT;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.SMALL_BREAK_TIME;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.TRACK_MODE;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.UNITS_ID;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.WEEKDAYS;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.WITH_TIME;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.WORK_TIME;

/**
 * Created by Warinator on 29.03.2017.
 */

public class Task extends BaseModel{
    public enum ChronoTrackMode {
       DIRECT, COUNTDOWN, INTERVAL, NONE
    }

    public enum ProgressTrackMode {
        MARK, PERCENT, UNITS, LIST, SEQUENCE
    }

    public enum Priority {MINOR, LOW, MEDIUM, HIGH, CRITICAL }

    private String name;
    private Project project;
    private Priority priority;
    private Category category;
    private Calendar reminder;
    private String note;
    private int icon;

    private boolean isRepeatable;
    private Calendar beginDate;
    private boolean withTime;
    private Weekdays weekdays;
    private int repeatCount;
    private boolean isInterval;
    private int intervalValue;

    private ProgressTrackMode progressTrackMode;
    private TrackUnit units;
    private int amountTotal;
    private int amountOnce;

    private ChronoTrackMode chronoTrackMode;
    private long workTime;
    private long smallBreakTime;
    private long bigBreakTime;
    private int intervalsCount;

    public int getBigBreakEvery() {
        return bigBreakEvery;
    }

    public void setBigBreakEvery(int bigBreakEvery) {
        this.bigBreakEvery = bigBreakEvery;
    }

    private int bigBreakEvery;

    /*
    {
        withTime = true;
        priority = Priority.MEDIUM;
        progressTrackMode = ProgressTrackMode.MARK;
        chronoTrackMode = ChronoTrackMode.DIRECT;
        beginDate = Calendar.getInstance();
        reminder = Calendar.getInstance();
        reminder.setTimeInMillis(0);
        weekdays = new Weekdays(0);
    }
    */

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = super.getContentValues();
        cv.put(NAME, name);
        if (project != null){
            cv.put(PROJECT_ID, project.getId());
        }
        cv.put(PRIORITY, priority.ordinal());
        if (category != null){
            cv.put(CATEGORY_ID, category.getId());
        }
        if (reminder != null){
            cv.put(REMINDER, reminder.getTimeInMillis());
        }
        cv.put(NOTE, note);
        cv.put(ICON, icon);

        cv.put(IS_REPEATABLE, isRepeatable);
        if (beginDate != null){
            cv.put(DATE_BEGIN, beginDate.getTimeInMillis());
        }
        cv.put(WITH_TIME, withTime);
        cv.put(WEEKDAYS, weekdays.getBitMask());
        cv.put(REPEAT_COUNT, repeatCount);
        cv.put(IS_INTERVAL, isInterval);
        cv.put(INTERVAL_VALUE, intervalValue);

        cv.put(TRACK_MODE, progressTrackMode.ordinal());
        if (units != null){
            cv.put(UNITS_ID, units.getId());
        }
        cv.put(AMOUNT_TOTAL, amountTotal);
        cv.put(AMOUNT_ONCE, amountOnce);

        cv.put(CHRONO_MODE, chronoTrackMode.ordinal());
        cv.put(WORK_TIME, workTime);
        cv.put(SMALL_BREAK_TIME, smallBreakTime);
        cv.put(BIG_BREAK_TIME, bigBreakTime);
        cv.put(INTERVALS_COUNT, intervalsCount);
        cv.put(BIG_BREAK_EVERY, bigBreakEvery);
        return cv;
    }

    public static final Func1<Cursor, Task> FROM_CURSOR = new Func1<Cursor, Task>() {
        @Override
        public Task call(Cursor cursor) {
            final Task task = new Task();
            task.id = cursor.getLong(cursor.getColumnIndex(DbContract.ID));
            task.name = cursor.getString(cursor.getColumnIndex(NAME));
            long projectId = cursor.getLong(cursor.getColumnIndex(PROJECT_ID));
            task.priority = Priority.values()
                    [cursor.getInt(cursor.getColumnIndex(PRIORITY))];
            long categoryId = cursor.getLong(cursor.getColumnIndex(CATEGORY_ID));
            Calendar calendar = null;
            long milis = cursor.getLong(cursor.getColumnIndex(REMINDER));
            if (milis > 0){
                calendar = Util.calendarFromMillis(milis);
            }
            task.reminder = calendar;
            task.note = cursor.getString(cursor.getColumnIndex(NOTE));
            task.icon = cursor.getInt(cursor.getColumnIndex(ICON));

            task.isRepeatable = cursor.getInt(cursor.getColumnIndex(IS_REPEATABLE)) > 0;
            calendar = null;
            milis = cursor.getLong(cursor.getColumnIndex(DATE_BEGIN));
            if (milis > 0){
                calendar = Util.calendarFromMillis(milis);
            }
            task.beginDate = calendar;
            task.withTime = cursor.getInt(cursor.getColumnIndex(WITH_TIME)) > 0;
            task.weekdays = new Weekdays(cursor.getInt(cursor.getColumnIndex(WEEKDAYS)));
            task.repeatCount = cursor.getInt(cursor.getColumnIndex(REPEAT_COUNT));
            task.isInterval = cursor.getInt(cursor.getColumnIndex(IS_INTERVAL)) > 0;
            task.intervalValue = cursor.getInt(cursor.getColumnIndex(INTERVAL_VALUE));

            task.progressTrackMode = ProgressTrackMode.values()
                    [cursor.getInt(cursor.getColumnIndex(TRACK_MODE))];
            long unitsId = cursor.getLong(cursor.getColumnIndex(UNITS_ID));
            task.amountTotal = cursor.getInt(cursor.getColumnIndex(AMOUNT_TOTAL));
            task.amountOnce = cursor.getInt(cursor.getColumnIndex(AMOUNT_ONCE));

            task.chronoTrackMode = ChronoTrackMode.values()
                    [cursor.getInt(cursor.getColumnIndex(CHRONO_MODE))];
            task.workTime = cursor.getLong(cursor.getColumnIndex(WORK_TIME));
            task.smallBreakTime = cursor.getLong(cursor.getColumnIndex(SMALL_BREAK_TIME));
            task.bigBreakTime = cursor.getLong(cursor.getColumnIndex(BIG_BREAK_TIME));
            task.intervalsCount = cursor.getInt(cursor.getColumnIndex(INTERVALS_COUNT));
            task.bigBreakEvery = cursor.getInt(cursor.getColumnIndex(BIG_BREAK_EVERY));

            if (projectId > 0){
                task.project = ProjectDAO.getDAO().get(projectId).firstOrDefault(null).toBlocking().single();
            }
            if (categoryId > 0){
                task.category = CategoryDAO.getDAO().get(categoryId).firstOrDefault(null).toBlocking().single();
            }
            if (unitsId > 0){
                task.units = TrackUnitDAO.getDAO().get(unitsId).firstOrDefault(null).toBlocking().single();
            }
            return task;
        }
    };

    public String getName() {
        return name;
    }

    public Project getProject() {
        return project;
    }

    public Priority getPriority() {
        return priority;
    }

    public Category getCategory() {
        return category;
    }

    public Calendar getReminder() {
        return reminder;
    }

    public String getNote() {
        return note;
    }

    public int getIcon() {
        return icon;
    }

    public boolean isRepeatable() {
        return isRepeatable;
    }

    public Calendar getBeginDate() {
        return beginDate;
    }

    public boolean isWithTime() {
        return withTime;
    }

    public Weekdays getWeekdays() {
        return weekdays;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public boolean isInterval() {
        return isInterval;
    }

    public int getIntervalValue() {
        return intervalValue;
    }

    public ProgressTrackMode getProgressTrackMode() {
        return progressTrackMode;
    }

    public TrackUnit getUnits() {
        return units;
    }

    public int getAmountTotal() {
        return amountTotal;
    }

    public int getAmountOnce() {
        return amountOnce;
    }

    public ChronoTrackMode getChronoTrackMode() {
        return chronoTrackMode;
    }

    public long getWorkTime() {
        return workTime;
    }

    public long getSmallBreakTime() {
        return smallBreakTime;
    }

    public long getBigBreakTime() {
        return bigBreakTime;
    }

    public int getIntervalsCount() {
        return intervalsCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setReminder(Calendar reminder) {
        this.reminder = reminder;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setRepeatable(boolean repeatable) {
        isRepeatable = repeatable;
    }

    public void setBeginDate(Calendar beginDate) {
        this.beginDate = beginDate;
    }

    public void setWithTime(boolean withTime) {
        this.withTime = withTime;
    }

    public void setWeekdays(Weekdays weekdays) {
        this.weekdays = weekdays;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setInterval(boolean interval) {
        isInterval = interval;
    }

    public void setIntervalValue(int intervalValue) {
        this.intervalValue = intervalValue;
    }

    public void setProgressTrackMode(ProgressTrackMode progressTrackMode) {
        this.progressTrackMode = progressTrackMode;
    }

    public void setUnits(TrackUnit units) {
        this.units = units;
    }

    public void setAmountTotal(int amountTotal) {
        this.amountTotal = amountTotal;
    }

    public void setAmountOnce(int amountOnce) {
        this.amountOnce = amountOnce;
    }

    public void setChronoTrackMode(ChronoTrackMode chronoTrackMode) {
        this.chronoTrackMode = chronoTrackMode;
    }

    public void setWorkTime(long workTime) {
        this.workTime = workTime;
    }

    public void setSmallBreakTime(long smallBreakTime) {
        this.smallBreakTime = smallBreakTime;
    }

    public void setBigBreakTime(long bigBreakTime) {
        this.bigBreakTime = bigBreakTime;
    }

    public void setIntervalsCount(int intervalsCount) {
        this.intervalsCount = intervalsCount;
    }
}
