package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.AMOUNT_DONE;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.DATE_TIME;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.IS_REMOVED;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TASK_ID;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TIME_SPENT;

/**
 * Created by Warinator on 07.04.2017.
 */

public class ConcreteTaskDAO extends BaseDAO<ConcreteTask>{
    private static ConcreteTaskDAO instance;

    public ConcreteTaskDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.ConcreteTaskCols._TAB_NAME;
            mMapper = ConcreteTask.FROM_CURSOR;
        }
    }

    public static ConcreteTaskDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.ConcreteTaskCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.ConcreteTaskCols._TAB_NAME).execute(db);
        createTable(db);
    }

    //Все задачи, назначенные в дни не ранее, чем d1, но ранее, чем d2
    public Observable<List<ConcreteTask>> getAllForDateRange(Calendar d1, Calendar d2) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d AND %s >= %d AND %s < %d", mTableName, IS_REMOVED, 0,
                DATE_TIME, d1.getTimeInMillis(), DATE_TIME, d2.getTimeInMillis())).autoUpdates(true).run().mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Все задачи с неуказанной датой
    public Observable<List<ConcreteTask>> getAllWithNoDate() {
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT * FROM %s WHERE %s = %d AND %s IS NULL",
                mTableName, IS_REMOVED, 0, DATE_TIME)).autoUpdates(true).run().mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Все задачи, не отмеченные как удаленные
    public Observable<List<ConcreteTask>> getAllNotRemoved(boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT * FROM %s WHERE %s = %d", mTableName, IS_REMOVED, 0)).autoUpdates(autoUpdates)
                .run()
                .mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Сумма выполненных единиц для задачи
    public Observable<Integer> getTotalAmountDone(Long taskId) {
        return rawQuery(mTableName, String.format("SELECT SUM(%s) FROM %s WHERE %s = %s",
                DbContract.ConcreteTaskCols.AMOUNT_DONE, mTableName, TASK_ID, String.valueOf(taskId)))
                .run().mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Добавить множество задач
    public Observable<Long> add(final ArrayList<ConcreteTask> items) {
        ArrayList<Observable<Long>> observables = new ArrayList<>();
        for (ConcreteTask t : items){
            ContentValues values = t.getContentValues();
            observables.add(insert(mTableName, values));
        }
        return Observable.concat(observables).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Количество повторений задачи, начиная с сегодняшнего дня
    public Observable<Integer> getTimesLeftStartingToday(long taskId){
        Calendar now = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(0);
        today.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        long timeMs = today.getTimeInMillis();
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT COUNT(*) FROM %s WHERE %s = %d AND %s = %d AND %s >= %d",
                mTableName,  IS_REMOVED, 0, TASK_ID, taskId, DATE_TIME, timeMs)).run().mapToOne(cursor -> cursor.getInt(0))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Отметить как удаленную
    public Observable<Integer> markAsRemoved(long id){
        ContentValues cv = new ContentValues();
        cv.put(IS_REMOVED, true);
        return update(mTableName, cv, DbContract.ID+" = "+id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Обновить дату и время
    public Observable<Integer> updateDateTime(long id, Calendar dateTime){
        ContentValues cv = new ContentValues();
        if (dateTime != null){
            cv.put(DATE_TIME, dateTime.getTimeInMillis());
        }
        else {
            cv.putNull(DATE_TIME);
        }
        return update(mTableName, cv, DbContract.ID+" = "+id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Получить длину серии непрерывных выполнений на данный момент
    public Observable<Integer> getCompletedSeriesLength(long taskId){
        Calendar today = Util.justDate(Calendar.getInstance());
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT * FROM %s WHERE %s = %d AND %s < %d ORDER BY %s",
                mTableName, TASK_ID, taskId, DATE_TIME, today.getTimeInMillis(), DATE_TIME))
                .autoUpdates(false).run().mapToList(mMapper).map(tasks -> {
                    int len = 0;
                    for (ConcreteTask ct : tasks){
                        if (ct.getAmountDone() <= 0){
                            len = 0;
                        }
                        else {
                            len++;
                        }
                    }
                    return len;
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Увеличить затраченное время на заданное значение
    public Observable<Integer> addTimeSpent(long id, long timeSpent){
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT %s FROM %s WHERE %s = %d",
                TIME_SPENT, mTableName, DbContract.ID, id))
                .autoUpdates(false).run().mapToOne(cursor -> cursor.getLong(0)).concatMap(oldTime -> {
                    long newTime = oldTime + timeSpent;
                    ContentValues cv = new ContentValues();
                    cv.put(TIME_SPENT, newTime);
                    return update(mTableName, cv, DbContract.ID+" = "+id);
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Список задач по списку их id
    public Observable<List<ConcreteTask>> getSpecified(List<Long> ids) {
        StringBuilder idListStr = new StringBuilder();
        idListStr.append("(");
        for (int i=0; i<ids.size(); i++){
            long id = ids.get(i);
            idListStr.append(String.valueOf(id));
            if (i < ids.size() - 1){
                idListStr.append(", ");
            }
        }
        idListStr.append(")");
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT * FROM %s WHERE %s IN %s",
                mTableName, DbContract.ID, idListStr.toString())).autoUpdates(true).run().mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public enum Group {
        TASKS, PROJECTS, CATEGORIES, DAY, NONE
    }

    //Статистика по времени
    public Observable<List<StatisticItem>> getTimeStatistics(Calendar from, Calendar to, Group groupBy){
        StringBuilder sbQuery = new StringBuilder();
        sbQuery.append("SELECT SUM(").append(TIME_SPENT).append(")");
        String targetField = "";
        switch (groupBy){
            case TASKS:
                targetField = TASK_ID;
                break;
            case PROJECTS:
                targetField = DbContract.TaskCols.PROJECT_ID;
                break;
            case CATEGORIES:
                targetField = DbContract.TaskCols.CATEGORY_ID;
                break;
            case DAY:
                targetField = DATE_TIME;
                break;
        }
        if (groupBy != Group.NONE) {
            sbQuery.append(", ").append(targetField);
        }
        sbQuery.append(" FROM ").append(mTableName);
        if (groupBy == Group.CATEGORIES || groupBy == Group.PROJECTS){
            sbQuery.append(String.format(" INNER JOIN %s on %s.%s = %s.%s",
                    DbContract.TaskCols._TAB_NAME, mTableName, TASK_ID,
                    DbContract.TaskCols._TAB_NAME, DbContract.ID));
        }
        sbQuery.append(String.format(Locale.getDefault(), " WHERE %s >= %d AND %s < %d",
                DATE_TIME, from.getTimeInMillis(), DATE_TIME, to.getTimeInMillis()));
        if (groupBy == Group.DAY){
            targetField = "strftime('%Y-%m-%d', "+DATE_TIME+" / 1000, 'unixepoch', 'localtime')";
        }
        if (groupBy != Group.NONE){
            sbQuery.append(" GROUP BY ").append(targetField);
        }
        Log.v("THE_QUERY", sbQuery.toString());
        return rawQuery(mTableName, sbQuery.toString()).autoUpdates(false).run().mapToList(cursor -> {
            StatisticItem item = new StatisticItem();
            item.groupAmount = cursor.getLong(0);
            if (cursor.getColumnCount() > 1){
                item.groupId = cursor.getLong(1);
            }
            return item;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Статистика по прогрессу
    public Observable<List<StatisticItem>> getProgressStatistics(Calendar from, Calendar to, Group groupBy){
        final String KEY_AMOUNT_NEED = "amt_need";
        String targetField = "";
        switch (groupBy){
            case TASKS:
                targetField = TASK_ID;
                break;
            case PROJECTS:
                targetField = DbContract.TaskCols.PROJECT_ID;
                break;
            case CATEGORIES:
                targetField = DbContract.TaskCols.CATEGORY_ID;
                break;
            case DAY:
                targetField = DATE_TIME;
                break;
        }

        StringBuilder sbQuery = new StringBuilder();
        sbQuery.append(String.format(
                "SELECT SUM(%s), %s, %s, %s, %s FROM %s INNER JOIN %s on %s.%s = %s.%s",
                AMOUNT_DONE, TASK_ID, DbContract.TaskCols.TRACK_MODE, targetField, DbContract.TaskCols.AMOUNT_TOTAL,
                mTableName, DbContract.TaskCols._TAB_NAME, mTableName, TASK_ID,
                DbContract.TaskCols._TAB_NAME, DbContract.ID));
        sbQuery.append(String.format(Locale.getDefault(), " WHERE %s >= %d AND %s < %d",
                DATE_TIME, from.getTimeInMillis(), DATE_TIME, to.getTimeInMillis()));
        sbQuery.append(" GROUP BY ").append(TASK_ID);
        if (groupBy != Group.NONE){
            sbQuery.append(" ORDER BY ").append(targetField);
        }

        Log.v("THE_QUERY", sbQuery.toString());

        String finalTargetField = targetField;
        Observable<List<StatisticItem>> obs = rawQuery(mTableName, sbQuery.toString()).autoUpdates(false).run()
                .mapToList(cursor -> {
                    Task.ProgressTrackMode mode = Task.ProgressTrackMode.values()[cursor.getInt(2)];
                    ContentValues cv = new ContentValues();
                    cv.put(AMOUNT_DONE, cursor.getInt(0));
                    long taskId = cursor.getLong(1);
                    cv.put(TASK_ID, taskId);
                    cv.put(finalTargetField, cursor.getLong(3));

                    Log.v("_THE_QUERY_1", cv.toString());

                    if (mode == Task.ProgressTrackMode.UNITS || mode == Task.ProgressTrackMode.PERCENT){
                        return Observable.just(cv).zipWith(Observable.just(cursor.getInt(4)), (contentValues, integer) -> {
                            contentValues.put(KEY_AMOUNT_NEED, integer);
                            return contentValues;
                        });
                    }
                    else if (mode == Task.ProgressTrackMode.LIST){
                        return Observable.just(cv).zipWith(CheckListItemDAO.getDAO().getAllForTask(cursor.getLong(1), false)
                                .map(List::size), (contentValues, integer) -> {
                                    contentValues.put(KEY_AMOUNT_NEED, integer);
                                    return contentValues;
                                });
                    }
                    else {
                        return Observable.just(cv).zipWith(TaskDAO.getDAO().get(taskId), (contentValues, task) -> {
                            int amtNeed = task.isRepeatable() ? task.getRepeatCount() : 1;
                            if (task.isRepeatable() && !task.isInterval()){
                                amtNeed *= task.getWeekdays().getCheckedDays().size();
                            }
                            contentValues.put(KEY_AMOUNT_NEED, amtNeed);
                            return contentValues;
                        });
                    }
                })
                .flatMap(new Func1<List<Observable<ContentValues>>, Observable<List<ContentValues>>>() {
                    @Override
                    public Observable<List<ContentValues>> call(List<Observable<ContentValues>> observables) {
                        return Observable.concat(observables).toList();
                    }
                })
                .map(cvList -> {
                    List<StatisticItem> items = new ArrayList<>();
                    int i = 0, n = cvList.size();
                    long groupProgress = 0;
                    long lastId = 0;
                    while (i < n){
                        ContentValues cv = cvList.get(i++);
                        Log.v("_THE_QUERY_A", cv.toString());
                        int amtNeed = cv.getAsInteger(KEY_AMOUNT_NEED);
                        groupProgress = Util.fracToPercent((double)cv.getAsInteger(AMOUNT_DONE)/(double)amtNeed);
                        lastId = cv.getAsLong(finalTargetField);
                        while (i < n){
                            cv = cvList.get(i);
                            Log.v("_THE_QUERY_B", cv.toString());
                            amtNeed = cv.getAsInteger(KEY_AMOUNT_NEED);
                            long groupId = cv.getAsLong(finalTargetField);
                            if (groupId != lastId){
                                break;
                            }
                            groupProgress += Util.fracToPercent((double)cv.getAsInteger(AMOUNT_DONE)/(double)amtNeed);
                            i++;
                        }
                        StatisticItem item = new StatisticItem();
                        item.groupAmount = groupProgress;
                        item.groupId = lastId;;
                        items.add(item);
                    }
                    return items;
                });
                /*
                .map(cvList -> {
                    List<StatisticItem> items = new ArrayList<>();
                    for (ContentValues cv : cvList){
                        Log.v("_THE_QUERY_3", cv.toString());
                        int amtNeed = cv.getAsInteger(KEY_AMOUNT_NEED);
                        if (amtNeed > 0){
                            long progress = Util.fracToPercent((double)cv.getAsInteger(AMOUNT_DONE)/(double)amtNeed);
                            long taskId = cv.getAsLong(TASK_ID);
                            StatisticItem item = new StatisticItem();
                            item.groupAmount = progress;
                            item.groupId = taskId;
                            items.add(item);
                        }
                    }
                    return items;
                });
                */
        return obs.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static class StatisticItem {
        public long groupAmount;
        public long groupId;
    }
}
