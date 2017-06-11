package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.AMOUNT_DONE;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.DATE_TIME;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.IS_REMOVED;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.QUEUE_POS;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TASK_ID;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TIME_SPENT;

/**
 * Created by Warinator on 07.04.2017.
 */

public class ConcreteTaskDAO extends RemovableDAO<ConcreteTask>{

    private static ConcreteTaskDAO instance;

    public ConcreteTaskDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.ConcreteTaskCols._TAB_NAME;
            mMapper = ConcreteTask.FROM_CURSOR;
            mColRemoved = IS_REMOVED;
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


    @Override
    public Observable<ConcreteTask> get(Long id) {
        return rawQuery(mTableName, "SELECT * FROM "+ mTableName +
                " WHERE " + DbContract.ID + " = " + String.valueOf(id)).autoUpdates(false)
                .run()
                .mapToOne(mMapper)
                .map(this::getProgressAndTaskObs)
                .flatMap(concreteTaskObservable -> concreteTaskObservable)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> markAsRemoved(long id){
        ContentValues cv = new ContentValues();
        cv.put(mColRemoved, true);
        cv.put(QUEUE_POS, -1);
        return update(mTableName, cv, CONFLICT_IGNORE , DbContract.ID+" = "+id)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Все задачи, назначенные в дни не ранее, чем d1, но ранее, чем d2
    public Observable<List<ConcreteTask>> getAllForDateRangeOldest(Calendar d1, Calendar d2, boolean autoUpdates) {
        /*
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d AND %s >= %d AND %s < %d", mTableName, IS_REMOVED, 0,
                DATE_TIME, d1.getTimeInMillis(), DATE_TIME, d2.getTimeInMillis())).autoUpdates(autoUpdates).run().mapToList(mMapper)
                .map(withProgressAndTask).flatMap(listObservable -> listObservable)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        */

        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d AND %s >= %d AND %s < %d", mTableName, IS_REMOVED, 0,
                DATE_TIME, d1.getTimeInMillis(), DATE_TIME, d2.getTimeInMillis())).autoUpdates(autoUpdates).run()
                .mapToList(mMapper).concatMap(tasks -> {
                    ArrayList<Observable<ConcreteTask>> obsList = new ArrayList<>();
                    for (ConcreteTask ct : tasks){
                        obsList.add(getProgressAndTaskObs(ct));
                    }
                    return Observable.concat(obsList).toList();
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<List<ConcreteTask>> getAllForDateRangeOld(Calendar d1, Calendar d2, boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d AND %s >= %d AND %s < %d", mTableName, IS_REMOVED, 0,
                DATE_TIME, d1.getTimeInMillis(), DATE_TIME, d2.getTimeInMillis())).autoUpdates(autoUpdates).run()
                .mapToList(cursor -> {
                    ConcreteTask ct = mMapper.call(cursor);
                    return getProgressAndTaskObs(ct);
                })
                .flatMap(obsList -> Observable.merge(obsList).take(obsList.size()).toList())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<ConcreteTask>> getAllForDateRange(Calendar d1, Calendar d2, boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d AND %s >= %d AND %s < %d", mTableName, IS_REMOVED, 0,
                DATE_TIME, d1.getTimeInMillis(), DATE_TIME, d2.getTimeInMillis())).autoUpdates(autoUpdates).run()
                .mapToList(mMapper).flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    //Все задачи с неуказанной датой
    public Observable<List<ConcreteTask>> getAllWithNoDate() {
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT * FROM %s WHERE %s = %d AND %s IS NULL",
                mTableName, IS_REMOVED, 0, DATE_TIME)).autoUpdates(true).run().mapToList(mMapper)
                .flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Все задачи, не отмеченные как удаленные
    public Observable<List<ConcreteTask>> getAll(boolean autoUpdates, boolean withRemoved) {
        StringBuilder querySb = new StringBuilder();
        querySb.append("SELECT * FROM ").append(mTableName);
        if (!withRemoved){
            querySb.append(String.format(Locale.getDefault(),
                    " WHERE %s = %d", mColRemoved, 0));
        }
        return rawQuery(mTableName, querySb.toString()).autoUpdates(autoUpdates)
                .run().mapToList(mMapper)
                .flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Сумма выполненных единиц для задачи
    public Observable<Integer> getTotalAmountDone(Long taskId) {
        return rawQuery(mTableName, String.format("SELECT SUM(%s) FROM %s WHERE %s = %s",
                DbContract.ConcreteTaskCols.AMOUNT_DONE, mTableName, TASK_ID, String.valueOf(taskId)))
                .run().mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> getAmountDoneAndTimesUntil(Long taskId, Calendar date) {
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT SUM(%s) FROM %s WHERE %s = %d AND %s < %d",
                DbContract.ConcreteTaskCols.AMOUNT_DONE, mTableName, TASK_ID, taskId, DATE_TIME, date.getTimeInMillis()))
                .run().mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    //Добавить множество задач
    public Observable<Long> addOld(final ArrayList<ConcreteTask> items) {
        return getMaxPos().concatMap(new Func1<Integer, Observable<Long>>() {
            @Override
            public Observable<Long> call(Integer maxPos) {
                ArrayList<Observable<Long>> observables = new ArrayList<>();
                Calendar today = Util.justDate(Calendar.getInstance());
                for (ConcreteTask t : items){
                    if (t.getDateTime() != null && (Util.compareDays(today, t.getDateTime()) == 0)){
                        t.setQueuePos(++maxPos);
                    }
                    ContentValues values = t.getContentValues();
                    observables.add(insert(mTableName, values));
                }
                return Observable.concat(observables);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Long>> add(final ArrayList<ConcreteTask> tasks) {
        return getMaxPos().observeOn(Schedulers.io()).concatMap(new Func1<Integer, Observable<List<Long>>>() {
            @Override
            public Observable<List<Long>> call(Integer maxPos) {
                Calendar today = Util.justDate(Calendar.getInstance());
                BriteDatabase.Transaction transaction = db.newTransaction();
                List<Long> ids = new ArrayList<>();
                try {
                    int pos = maxPos;
                    for (ConcreteTask t : tasks){
                        if (t.getDateTime() != null && (Util.compareDays(today, t.getDateTime()) == 0)){
                            t.setQueuePos(++pos);
                        }
                        ContentValues values = t.getContentValues();
                        ids.add(db.insert(mTableName, values));
                    }
                    transaction.markSuccessful();
                } finally {
                    transaction.end();
                }
                return Observable.just(ids);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
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


    public Observable<Integer> markAsRemovedForTask(long taskId){
        ContentValues cv = new ContentValues();
        cv.put(IS_REMOVED, true);
        return update(mTableName, cv, TASK_ID+" = "+taskId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Обновить дату и время
    public Observable<Integer> updateDateTime(long id, Calendar dateTime, int queuePos){
        return getMaxPos().concatMap(maxPos -> {
            ContentValues cv = new ContentValues();
            if (dateTime != null){
                cv.put(DATE_TIME, dateTime.getTimeInMillis());
                if (queuePos < 0 && Util.compareDays(Calendar.getInstance(), dateTime) == 0){
                    cv.put(QUEUE_POS, ++maxPos);
                }
            }
            else {
                cv.putNull(DATE_TIME);
            }
            return update(mTableName, cv, DbContract.ID+" = "+id);
        })
       .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Получить длину серии непрерывных выполнений на данный момент
    public Observable<Integer> getCompletedSeriesLength(long taskId){
        Calendar tomorrow = Util.justDate(Calendar.getInstance());
        tomorrow.add(Calendar.DATE, 1);
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT %s, %s FROM %s WHERE %s = %d AND %s < %d ORDER BY %s DESC",
                AMOUNT_DONE, DATE_TIME, mTableName, TASK_ID, taskId, DATE_TIME,
                tomorrow.getTimeInMillis(), DATE_TIME))
                .autoUpdates(false).run().mapToList(cursor -> {
                    if (Util.compareDays(Util.justDate(cursor.getLong(1)),
                            Util.justDate(Calendar.getInstance())) == 0){
                        if (cursor.getInt(0) > 0){
                            return 1;
                        }
                        else {
                            return -1;
                        }
                    }
                    else {
                        return cursor.getInt(0);
                    }

                })
                .concatMap(values -> {
                    int len = 0;
                    for (int t : values){
                        if (t < 0)
                            continue;
                        if (t == 0){
                            break;
                        }
                        len++;
                    }
                    return Observable.just(len);
                });
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
    public Observable<List<ConcreteTask>> getSpecified(List<Long> ids, boolean autoUpdates) {
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
                mTableName, DbContract.ID, idListStr.toString())).autoUpdates(autoUpdates).run().mapToList(mMapper)
                .flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Список задач по id задачи
    public Observable<List<ConcreteTask>> getByTaskId(long taskId, boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT * FROM %s WHERE %s = %d",
                mTableName, TASK_ID, taskId)).autoUpdates(autoUpdates).run().mapToList(mMapper)
                .flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public enum Group {
        TASKS, PROJECTS, CATEGORIES, DAY, NONE
    }

    public Observable<Integer> deleteWithoutTrigger(long id){
        return Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            subscriber.onNext(db.delete(mTableName, DbContract.ID+" = "+id));
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Статистика по времени
    public Observable<List<StatisticItem>> getTimeStatistics(Calendar from, Calendar to, Group groupBy, long taskId){
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
        if (taskId != 0){
            sbQuery.append(String.format(Locale.getDefault(), " AND %s = %d",TASK_ID, taskId));
        }

        if (groupBy == Group.DAY){
            targetField = "strftime('%Y-%m-%d', "+DATE_TIME+" / 1000, 'unixepoch', 'localtime')";
        }
        if (groupBy != Group.NONE){
            sbQuery.append(" GROUP BY ").append(targetField);
        }
        sbQuery.append(" ORDER BY ").append(groupBy == Group.DAY ? DATE_TIME : "SUM("+TIME_SPENT+")");
        //Log.v("THE_QUERY", sbQuery.toString());

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
                    if (groupBy != Group.DAY){
                        cv.put(finalTargetField, cursor.getLong(3));
                    }
                    else {
                        cv.put(finalTargetField, Util.justDate(cursor.getLong(3)).getTimeInMillis());
                    }

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

        return obs.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<StatisticItem>> getTaskAmtByDays(Calendar from, Calendar to, long taskId){
        StringBuilder sbQuery = new StringBuilder();
        sbQuery.append(String.format("SELECT SUM(%s), %s FROM %s", AMOUNT_DONE, DATE_TIME, mTableName));
        sbQuery.append(String.format(Locale.getDefault(), " WHERE %s = %d AND %s >= %d AND %s < %d",
               TASK_ID, taskId, DATE_TIME, from.getTimeInMillis(), DATE_TIME, to.getTimeInMillis()));
        sbQuery.append(" GROUP BY ").append("strftime('%Y-%m-%d', "+DATE_TIME+" / 1000, 'unixepoch', 'localtime')");
        sbQuery.append(" ORDER BY ").append(DATE_TIME);

        Log.v("THE_QUERY", sbQuery.toString());

        return rawQuery(mTableName, sbQuery.toString()).autoUpdates(false).run()
                .mapToList(cursor -> {
                    StatisticItem item = new StatisticItem();
                    item.groupAmount = cursor.getInt(0);
                    item.groupId = cursor.getLong(1);
                    return item;
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    //получить все задачи в очереди
    public Observable<List<ConcreteTask>> getAllQueued(boolean autoUpdates){
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = 0 AND %s >= 0 ORDER BY %s",
                mTableName,IS_REMOVED, QUEUE_POS, QUEUE_POS)).autoUpdates(autoUpdates).run().mapToList(mMapper)
                .flatMap(withProgressAndTask).observeOn(Schedulers.computation())
                .map(tasks -> {
                    Collections.sort(tasks, (o1, o2) -> {
                        if (o1.getQueuePos() < o2.getQueuePos()){
                            return -1;
                        }
                        if (o1.getQueuePos() > o2.getQueuePos()){
                            return 1;
                        }
                        return 0;
                    });
                    return tasks;
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить последнюю позицию в очереди
    private Observable<Integer> getMaxPos(){
        return rawQuery(mTableName, String.format("SELECT MAX(%s) FROM %s WHERE %s = 0",
                QUEUE_POS, mTableName, IS_REMOVED)).autoUpdates(false).run()
                .mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //добавить задачу в очередь
    public Observable<Integer> addTaskToQueue(long id){
        return getMaxPos().concatMap(maxPos -> {
            ContentValues cv = new ContentValues();
            cv.put(QUEUE_POS, maxPos+1);
            return update(mTableName, cv, DbContract.ID+" = "+id);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //добавить все задачи на сегодня в очередь
    public Observable<Integer> addAllForTodayToQueue(){
        Calendar d1 = Util.justDate(Calendar.getInstance());
        Calendar d2 = Calendar.getInstance();
        d2.add(Calendar.DATE, 1);
        d2 = Util.justDate(d2);
        return ConcreteTaskDAO.getDAO().getAllForDateRange(d1, d2, false).observeOn(Schedulers.io())
                .zipWith(getMaxPos(), (tasks, maxPos) -> {
                    BriteDatabase.Transaction transaction = db.newTransaction();
                    try {
                        int pos = maxPos;
                        for (ConcreteTask task : tasks){
                            if (task.getQueuePos() < 0){
                                ContentValues cv = new ContentValues();
                                cv.put(QUEUE_POS, ++pos);
                                db.update(mTableName, cv, DbContract.ID+" = "+task.getId());
                            }
                        }
                        transaction.markSuccessful();
                    } finally {
                        transaction.end();
                    }
                    return tasks.size();
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    //обновить позиции задач в очереди
    public Observable<Integer> updateQueuePositions(List<ConcreteTask> tasks){
        return Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            BriteDatabase.Transaction transaction = db.newTransaction();
            try {
                int pos = 0;
                for (ConcreteTask task : tasks){
                    if (task.getQueuePos() != pos){
                        ContentValues cv = new ContentValues();
                        cv.put(QUEUE_POS, pos);
                        db.update(mTableName, cv, DbContract.ID+" = "+task.getId());
                    }
                    pos++;
                }
                transaction.markSuccessful();
            } finally {
                transaction.end();
            }
            subscriber.onNext(tasks.size());
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    //убрать задачу из очереди
    public Observable<Integer> removeFromQueue(long taskId){
        ContentValues cv = new ContentValues();
        cv.put(QUEUE_POS, -1);
        return update(mTableName, cv, DbContract.ID+" = "+taskId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public static class StatisticItem {
        public long groupAmount;
        public long groupId;
        public String label;
        public int color;
    }


    Func1<List<ConcreteTask>, Observable<List<ConcreteTask>>> withProgressAndTask = tasks ->
            Observable.from(tasks).flatMap(this::getProgressAndTaskObs).take(tasks.size()).toList();


    private Observable<Integer> getRealRepeatCountUntil(Long taskId, Calendar date) {
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT COUNT(*) FROM %s WHERE %s = %d AND %s < %d",
                mTableName, TASK_ID, taskId, DATE_TIME, date.getTimeInMillis()))
                .run().mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io());
    }

    private Observable<Integer> getRealRepeatCount(Long taskId) {
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT COUNT(*) FROM %s WHERE %s = %d",
                mTableName, TASK_ID, taskId))
                .run().mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io());
    }

    //todo: мб auto update true ?

    private Observable<ConcreteTask> getProgressAndTaskObsOld(ConcreteTask ct){
        Log.v("ZAD", "получить observable для задачи "+ct.getTask().getId()+" "+ct.getId());
        Observable<Task> obs = TaskDAO.getDAO().get(ct.getTask().getId());
        return obs.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).concatMap(task -> {
            Log.v("ZAD","задача "+ct.getTask().getId()+" "+ct.getId()+" получена");
            ct.setTask(task);
            //общий объём выполнения
            if (task.getProgressTrackMode() == Task.ProgressTrackMode.LIST){
                //количество элементов списка
                return CheckListItemDAO.getDAO().getCountForTask(task.getId(), false);
            }
            else {
                //заданный объём
                return Observable.just(task.getAmountTotal());
            }
        }).concatMap(amtNeedTotal -> {
            Log.v("ZAD", ct.getId()+" "+"получен общий объём "+amtNeedTotal);
            ct.setAmtNeedTotal(amtNeedTotal);
            Task task = ct.getTask();
            //весь выполненный объём
            if (task.getProgressTrackMode() == Task.ProgressTrackMode.LIST){
                //отмеченные элементы списка
                return CheckListItemDAO.getDAO().getCountDoneForTask(task.getId(), false);
            }
            else if (task.getProgressTrackMode() == Task.ProgressTrackMode.SEQUENCE){
                //длина серии
                return getCompletedSeriesLength(ct.getTask().getId());
            }
            else {
                //суммарный объём
                return ConcreteTaskDAO.getDAO().getTotalAmountDone(task.getId());
            }
        }).concatMap(amtDoneTotal -> {
            Log.v("ZAD", ct.getId()+" "+"получен выполненный объём "+amtDoneTotal);
            ct.setAmtDoneTotal(amtDoneTotal);
            //назначено раз всего
            return getRealRepeatCount(ct.getTask().getId());
        }).concatMap(timesTotal -> {
            Log.v("ZAD", ct.getId()+" "+"получено общее число раз "+timesTotal);
            ct.setTimesTotal(timesTotal);
            if (ct.getTask().getProgressTrackMode() == Task.ProgressTrackMode.MARK){
                ct.setAmtNeedTotal(timesTotal);
            }
            //назначено раз до этого дня
            if (ct.getDateTime() != null){
                return getRealRepeatCountUntil(ct.getTask().getId(), Util.justDate(ct.getDateTime()));
            }
            else {
                return Observable.just(0);
            }
        }).concatMap(timesBefore -> {
            Log.v("ZAD", ct.getId()+" "+"получено число раз ранее "+timesBefore);
            ct.setTimesBefore(timesBefore);
            return Observable.just(ct);
        });
    }

    private Observable<ConcreteTask> getProgressAndTaskObs(ConcreteTask ct){
        return TaskDAO.getDAO().get(ct.getTask().getId()).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).concatMap(task -> {
            ct.setTask(task);

            Observable<Integer> obs1;
            //общий объём выполнения
            if (task.getProgressTrackMode() == Task.ProgressTrackMode.LIST){
                //количество элементов списка
                obs1 = CheckListItemDAO.getDAO().getCountForTask(task.getId(), false);
            }
            else {
                //заданный объём
                obs1 =  Observable.just(task.getAmountTotal());
            }

            Observable<Integer> obs2;
            //весь выполненный объём
            if (task.getProgressTrackMode() == Task.ProgressTrackMode.LIST){
                //отмеченные элементы списка
                obs2 = CheckListItemDAO.getDAO().getCountDoneForTask(task.getId(), false);
            }
            else if (task.getProgressTrackMode() == Task.ProgressTrackMode.SEQUENCE){
                //длина серии
                obs2 = getCompletedSeriesLength(ct.getTask().getId());
            }
            else {
                //суммарный объём
                obs2 = ConcreteTaskDAO.getDAO().getTotalAmountDone(task.getId());
            }

            Observable<Integer> obs3 = getRealRepeatCount(ct.getTask().getId());

            Observable<Integer> obs4;
            //назначено раз до этого дня
            if (ct.getDateTime() != null){
                obs4 = getRealRepeatCountUntil(ct.getTask().getId(), Util.justDate(ct.getDateTime()));
            }
            else {
                obs4 = Observable.just(0);
            }

            return Observable.zip(Observable.just(ct), obs1, obs2, obs3, obs4,
                    (ctask, amtNeedTotal, amtDoneTotal, timesTotal, timesBefore) -> {
                        ctask.setAmtNeedTotal(amtNeedTotal);
                        ctask.setAmtDoneTotal(amtDoneTotal);
                        ctask.setTimesTotal(timesTotal);
                        if (ctask.getTask().getProgressTrackMode() == Task.ProgressTrackMode.MARK){
                            ctask.setAmtNeedTotal(timesTotal);
                        }
                        ctask.setTimesBefore(timesBefore);
                        /*Log.v("ZAD",ct.getId()+" needTotal = "+amtNeedTotal+", doneTotal = "+amtDoneTotal+
                        ", timesTotal = "+timesTotal+", timesBefore = "+timesBefore);*/
                        return ctask;
                    });

        });
    }

}
