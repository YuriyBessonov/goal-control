package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
import static java.util.Calendar.DATE;

/**
 * DAO таблицы назначенных задач
 */
public class ConcreteTaskDAO extends RemovableDAO<ConcreteTask> {

    private static ConcreteTaskDAO instance;
    private Func1<List<ConcreteTask>, Observable<List<ConcreteTask>>> withProgressAndTask = tasks ->
            Observable.from(tasks).flatMap(this::getProgressAndTaskObs).take(tasks.size()).toList();

    public ConcreteTaskDAO() {
        if (instance == null) {
            instance = this;
            mTableName = DbContract.ConcreteTaskCols._TAB_NAME;
            mMapper = ConcreteTask.FROM_CURSOR;
            mColRemoved = IS_REMOVED;
        }
    }

    public static ConcreteTaskDAO getDAO() {
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

    //получить задачу по id
    @Override
    public Observable<ConcreteTask> get(Long id) {
        return rawQuery(mTableName, "SELECT * FROM " + mTableName +
                " WHERE " + DbContract.ID + " = " + String.valueOf(id)).autoUpdates(false)
                .run()
                .mapToOne(mMapper)
                .map(this::getProgressAndTaskObs)
                .flatMap(concreteTaskObservable -> concreteTaskObservable)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //отметить задачу с заданным id как удаленную
    public Observable<Integer> markAsRemoved(long id) {
        ContentValues cv = new ContentValues();
        cv.put(mColRemoved, true);
        cv.put(QUEUE_POS, -1);
        return update(mTableName, cv, CONFLICT_IGNORE, DbContract.ID + " = " + id)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить все задачи, назначенные в дни не ранее, чем d1, но ранее, чем d2
    public Observable<List<ConcreteTask>> getAllForDateRange(Calendar d1, Calendar d2,
                                                             boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d AND %s >= %d AND %s < %d", mTableName, IS_REMOVED, 0,
                DATE_TIME, d1.getTimeInMillis(), DATE_TIME, d2.getTimeInMillis()))
                .autoUpdates(autoUpdates).run()
                .mapToList(mMapper).flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить все назначения задачи на текущий день
    public Observable<List<ConcreteTask>> getAllForTaskToday(long taskId, boolean autoUpdates) {
        Calendar today = Util.justDate(Calendar.getInstance());
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(today.getTime());
        tomorrow.add(DATE, 1);
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d AND %s = %d AND %s >= %d AND %s < %d", mTableName,
                TASK_ID, taskId, IS_REMOVED, 0,
                DATE_TIME, today.getTimeInMillis(), DATE_TIME, tomorrow.getTimeInMillis()))
                .autoUpdates(autoUpdates).run()
                .mapToList(mMapper).flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить все задачи, назначенные в дни не ранее, чем d1, но ранее, чем d2, включая удаленные
    public Observable<List<ConcreteTask>> getAllForDateRangeInclRemoved(Calendar d1, Calendar d2,
                                                                        boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s >= %d AND %s < %d", mTableName,
                DATE_TIME, d1.getTimeInMillis(), DATE_TIME, d2.getTimeInMillis()))
                .autoUpdates(autoUpdates).run()
                .mapToList(mMapper).flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить все не выполненные задачи за сегодня и все предыдущие дни
    public Observable<List<ConcreteTask>> getAllNotDoneUntilTomorrow(boolean autoUpdates) {
        Calendar tomorrow = Util.justDate(Calendar.getInstance());
        tomorrow.add(DATE, 1);
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d AND %s = 0 AND %s < %d", mTableName, IS_REMOVED, 0,
                AMOUNT_DONE, DATE_TIME, tomorrow.getTimeInMillis())).autoUpdates(autoUpdates).run()
                .mapToList(mMapper).flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить все задачи с неуказанной датой
    public Observable<List<ConcreteTask>> getAllWithNoDate() {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d AND %s IS NULL",
                mTableName, IS_REMOVED, 0, DATE_TIME)).autoUpdates(true).run().mapToList(mMapper)
                .flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }



    //получить все задачи, не отмеченные как удаленные
    public Observable<List<ConcreteTask>> getAll(boolean autoUpdates, boolean withRemoved) {
        StringBuilder querySb = new StringBuilder();
        querySb.append("SELECT * FROM ").append(mTableName);
        if (!withRemoved) {
            querySb.append(String.format(Locale.getDefault(),
                    " WHERE %s = %d", mColRemoved, 0));
        }
        return rawQuery(mTableName, querySb.toString()).autoUpdates(autoUpdates)
                .run().mapToList(mMapper)
                .flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить сумму выполненных единиц для задачи
    public Observable<Integer> getTotalAmountDone(Long taskId) {
        return rawQuery(mTableName, String.format("SELECT SUM(%s) FROM %s WHERE %s = %s",
                DbContract.ConcreteTaskCols.AMOUNT_DONE, mTableName, TASK_ID, String.valueOf(taskId)))
                .run().mapToOne(cursor -> cursor.getInt(0))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //добавить список задач
    public Observable<List<Long>> add(final ArrayList<ConcreteTask> tasks) {
        return getMaxPos().observeOn(Schedulers.io())
                .concatMap(new Func1<Integer, Observable<List<Long>>>() {
            @Override
            public Observable<List<Long>> call(Integer maxPos) {
                Calendar today = Util.justDate(Calendar.getInstance());
                BriteDatabase.Transaction transaction = db.newTransaction();
                List<Long> ids = new ArrayList<>();
                try {
                    int pos = maxPos;
                    for (ConcreteTask t : tasks) {
                        if (t.getDateTime() != null && (Util.compareDays(today, t.getDateTime()) == 0)) {
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

    //получить количество повторений задачи, начиная с сегодняшнего дня
    public Observable<Integer> getTimesLeftStartingToday(long taskId) {
        Calendar now = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(0);
        today.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        long timeMs = today.getTimeInMillis();
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT COUNT(*) FROM %s WHERE %s = %d AND %s = %d AND %s >= %d",
                mTableName, IS_REMOVED, 0, TASK_ID, taskId, DATE_TIME, timeMs))
                .run().mapToOne(cursor -> cursor.getInt(0))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //отметить все назначения задачи с указанным id как удаленные
    public Observable<Integer> markAsRemovedForTask(long taskId) {
        ContentValues cv = new ContentValues();
        cv.put(IS_REMOVED, true);
        return update(mTableName, cv, TASK_ID + " = " + taskId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //обновить дату и время для заданной назначенной задачи
    public Observable<Integer> updateDateTime(long id, Calendar dateTime, int queuePos) {
        return getMaxPos().concatMap(maxPos -> {
            ContentValues cv = new ContentValues();
            if (dateTime != null) {
                cv.put(DATE_TIME, dateTime.getTimeInMillis());
                if (queuePos < 0 && Util.compareDays(Calendar.getInstance(), dateTime) == 0) {
                    cv.put(QUEUE_POS, ++maxPos);
                }
            } else {
                cv.putNull(DATE_TIME);
            }
            return update(mTableName, cv, DbContract.ID + " = " + id);
        })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить длину серии непрерывных выполнений задачи на данный момент
    public Observable<Integer> getCompletedSeriesLength(long taskId) {
        Calendar tomorrow = Util.justDate(Calendar.getInstance());
        tomorrow.add(DATE, 1);
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT %s, %s FROM %s WHERE %s = %d AND %s < %d ORDER BY %s DESC",
                AMOUNT_DONE, DATE_TIME, mTableName, TASK_ID, taskId, DATE_TIME,
                tomorrow.getTimeInMillis(), DATE_TIME))
                .autoUpdates(false).run().mapToList(cursor -> {
                    if (Util.compareDays(Util.justDate(cursor.getLong(1)),
                            Util.justDate(Calendar.getInstance())) == 0) {
                        if (cursor.getInt(0) > 0) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else {
                        return cursor.getInt(0);
                    }

                })
                .concatMap(values -> {
                    int len = 0;
                    for (int t : values) {
                        if (t < 0)
                            continue;
                        if (t == 0) {
                            break;
                        }
                        len++;
                    }
                    return Observable.just(len);
                });
    }

    //увеличить затраченное время назначенной задачи на заданное значение
    public Observable<Integer> addTimeSpent(long id, long timeSpent) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT %s FROM %s WHERE %s = %d",
                TIME_SPENT, mTableName, DbContract.ID, id))
                .autoUpdates(false).run().mapToOne(cursor -> cursor.getLong(0)).concatMap(oldTime -> {
                    long newTime = oldTime + timeSpent;
                    ContentValues cv = new ContentValues();
                    cv.put(TIME_SPENT, newTime);
                    return update(mTableName, cv, DbContract.ID + " = " + id);
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //список назначенных задач по id задачи
    public Observable<List<ConcreteTask>> getByTaskId(long taskId, boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d",
                mTableName, TASK_ID, taskId)).autoUpdates(autoUpdates).run().mapToList(mMapper)
                .flatMap(withProgressAndTask)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить статистику задачи
    public Observable<List<StatisticItem>> getStatistics(StatUnits units, Calendar from, Calendar to,
                                                         Group groupBy, boolean withRemoved,
                                                         long specificTask) {
        Observable<List<ConcreteTask>> obs = withRemoved ?
                getAllForDateRangeInclRemoved(from, to, false) : getAllForDateRange(from, to, false);
        return obs.concatMap(concreteTasks -> {
            List<StatisticItem> statItems = new ArrayList<>();
            LongSparseArray<Double> groups = new LongSparseArray<>();
            LongSparseArray<String> labels = new LongSparseArray<>();
            for (ConcreteTask ct : concreteTasks) {
                long groupId = 0;
                Task task = ct.getTask();
                switch (groupBy) {
                    case TASKS:
                        groupId = task.getId();
                        labels.put(groupId, task.getName());
                        break;
                    case PROJECTS:
                        if (task.getProject() == null) {
                            groupId = 0;
                        } else {
                            groupId = task.getProject().getId();
                            labels.put(groupId, task.getProject().getName());
                        }
                        break;
                    case CATEGORIES:
                        if (task.getCategory() == null) {
                            groupId = 0;
                        } else {
                            groupId = task.getCategory().getId();
                            labels.put(groupId, task.getCategory().getName());
                        }
                        break;
                    case DAY:
                        groupId = Util.justDate(ct.getDateTime()).getTimeInMillis();
                        break;
                    case NONE:
                        groupId = 0;
                        break;
                }
                double amt;
                if (units == StatUnits.TIME) {
                    amt = ct.getTimeSpent();
                } else {
                    amt = Util.fracToPercent((double) ct.getAmountDone() /
                            Math.max(ct.getAmtNeedTotal(), 1));
                }
                if (specificTask == 0 || task.getId() == specificTask) {
                    groups.put(groupId, groups.get(groupId, 0.0) + amt);
                }
            }

            for (int i = 0; i < groups.size(); i++) {
                StatisticItem item = new StatisticItem();
                item.groupId = groups.keyAt(i);
                item.groupAmount = groups.get(groups.keyAt(i));
                item.label = labels.get(groups.keyAt(i), "");
                statItems.add(item);
            }
            return Observable.just(statItems);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить объём выполнения задачи по дням
    public Observable<List<StatisticItem>> getTaskAmtByDays(Calendar from, Calendar to, long taskId) {
        StringBuilder sbQuery = new StringBuilder();
        sbQuery.append(String.format("SELECT SUM(%s), %s FROM %s", AMOUNT_DONE, DATE_TIME, mTableName));
        sbQuery.append(String.format(Locale.getDefault(), " WHERE %s = %d AND %s >= %d AND %s < %d",
                TASK_ID, taskId, DATE_TIME, from.getTimeInMillis(), DATE_TIME, to.getTimeInMillis()));
        sbQuery.append(" GROUP BY ").append("strftime('%Y-%m-%d', " + DATE_TIME +
                " / 1000, 'unixepoch', 'localtime')");
        sbQuery.append(" ORDER BY ").append(DATE_TIME);

        return rawQuery(mTableName, sbQuery.toString()).autoUpdates(false).run()
                .mapToList(cursor -> {
                    StatisticItem item = new StatisticItem();
                    item.groupAmount = cursor.getInt(0);
                    item.groupId = cursor.getLong(1);
                    return item;
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить все задачи в очереди
    public Observable<List<ConcreteTask>> getAllQueued(boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = 0 AND %s >= 0 ORDER BY %s",
                mTableName, IS_REMOVED, QUEUE_POS, QUEUE_POS)).autoUpdates(autoUpdates)
                .run().mapToList(mMapper)
                .flatMap(withProgressAndTask).observeOn(Schedulers.computation())
                .map(tasks -> {
                    Collections.sort(tasks, (o1, o2) -> {
                        if (o1.getQueuePos() < o2.getQueuePos()) {
                            return -1;
                        }
                        if (o1.getQueuePos() > o2.getQueuePos()) {
                            return 1;
                        }
                        return 0;
                    });
                    return tasks;
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить последнюю позицию в очереди
    private Observable<Integer> getMaxPos() {
        return rawQuery(mTableName, String.format("SELECT MAX(%s) FROM %s WHERE %s = 0",
                QUEUE_POS, mTableName, IS_REMOVED)).autoUpdates(false).run()
                .mapToOne(cursor -> cursor.getInt(0))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //добавить задачу в очередь
    public Observable<Integer> addTaskToQueue(long id) {
        return getMaxPos().concatMap(maxPos -> {
            ContentValues cv = new ContentValues();
            cv.put(QUEUE_POS, maxPos + 1);
            return update(mTableName, cv, DbContract.ID + " = " + id);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //добавить все невыполненные задачи на сегодня и ранее в очередь
    public Observable<Integer> addAllNecessaryToQueue() {
        return ConcreteTaskDAO.getDAO().getAllNotDoneUntilTomorrow(false).observeOn(Schedulers.io())
                .zipWith(getMaxPos(), (tasks, maxPos) -> {
                    BriteDatabase.Transaction transaction = db.newTransaction();
                    try {
                        int pos = maxPos;
                        for (ConcreteTask task : tasks) {
                            if (task.getQueuePos() < 0) {
                                ContentValues cv = new ContentValues();
                                cv.put(QUEUE_POS, ++pos);
                                db.update(mTableName, cv, DbContract.ID + " = " + task.getId());
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
    public Observable<Integer> updateQueuePositions(List<ConcreteTask> tasks) {
        return Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            BriteDatabase.Transaction transaction = db.newTransaction();
            try {
                int pos = 0;
                for (ConcreteTask task : tasks) {
                    if (task.getQueuePos() != pos) {
                        ContentValues cv = new ContentValues();
                        cv.put(QUEUE_POS, pos);
                        db.update(mTableName, cv, DbContract.ID + " = " + task.getId());
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
    public Observable<Integer> removeFromQueue(long taskId) {
        ContentValues cv = new ContentValues();
        cv.put(QUEUE_POS, -1);
        return update(mTableName, cv, DbContract.ID + " = " + taskId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить число фактических повторений задачи до заданной даты
    private Observable<Integer> getRealRepeatCountUntil(Long taskId, Calendar date) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT COUNT(*) FROM %s WHERE %s = %d AND %s < %d",
                mTableName, TASK_ID, taskId, DATE_TIME, date.getTimeInMillis()))
                .run().mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io());
    }

    //получить всё число фактических повторений задачи
    private Observable<Integer> getRealRepeatCount(Long taskId) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT COUNT(*) FROM %s WHERE %s = %d",
                mTableName, TASK_ID, taskId))
                .run().mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io());
    }

    //получить Observable, снабжающий назначенную задачу сведениями о самой задаче и прогрессе
    private Observable<ConcreteTask> getProgressAndTaskObs(ConcreteTask ct) {
        return TaskDAO.getDAO().get(ct.getTask().getId()).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).concatMap(task -> {
                    ct.setTask(task);

                    Observable<Integer> obs1;
                    //общий объём выполнения
                    if (task.getProgressTrackMode() == Task.ProgressTrackMode.LIST) {
                        //количество элементов списка
                        obs1 = CheckListItemDAO.getDAO().getCountForTask(task.getId(), false);
                    } else {
                        //заданный объём
                        obs1 = Observable.just(task.getAmountTotal());
                    }

                    Observable<Integer> obs2;
                    //весь выполненный объём
                    if (task.getProgressTrackMode() == Task.ProgressTrackMode.LIST) {
                        //отмеченные элементы списка
                        obs2 = CheckListItemDAO.getDAO().getCountDoneForTask(task.getId(), false);
                    } else if (task.getProgressTrackMode() == Task.ProgressTrackMode.SEQUENCE) {
                        //длина серии
                        obs2 = getCompletedSeriesLength(ct.getTask().getId());
                    } else {
                        //суммарный объём
                        obs2 = ConcreteTaskDAO.getDAO().getTotalAmountDone(task.getId());
                    }

                    Observable<Integer> obs3 = getRealRepeatCount(ct.getTask().getId());

                    Observable<Integer> obs4;
                    //назначено раз до этого дня
                    if (ct.getDateTime() != null) {
                        obs4 = getRealRepeatCountUntil(ct.getTask().getId(),
                                Util.justDate(ct.getDateTime()));
                    } else {
                        obs4 = Observable.just(0);
                    }

                    return Observable.zip(Observable.just(ct), obs1, obs2, obs3, obs4,
                            (ctask, amtNeedTotal, amtDoneTotal, timesTotal, timesBefore) -> {
                                ctask.setAmtNeedTotal(amtNeedTotal);
                                ctask.setAmtDoneTotal(amtDoneTotal);
                                ctask.setTimesTotal(timesTotal);
                                if (ctask.getTask().getProgressTrackMode() ==
                                        Task.ProgressTrackMode.MARK) {
                                    ctask.setAmtNeedTotal(timesTotal);
                                }
                                ctask.setTimesBefore(timesBefore);
                                return ctask;
                            });

                });
    }

    //удалить назначения задачи на даты, перечисленные в списке
    public Observable<Integer> deleteIfDateInList(long taskId, List<Calendar> dates) {
        StringBuilder sb = new StringBuilder();
        sb.append(TASK_ID + " = ").append(taskId).append(" AND ");
        sb.append(DATE_TIME).append(" / ").append(TimeUnit.DAYS.toMillis(1)).append(" IN ( ");
        for (int i = 0; i < dates.size(); i++) {
            sb.append(dates.get(i).getTimeInMillis() / TimeUnit.DAYS.toMillis(1));
            if (i < dates.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(" )");

        return delete(mTableName, sb.toString()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //удалить назначения задачи, начиная с заданной даты
    public Observable<Integer> deleteAllStartingFrom(long taskId, Calendar date) {
        StringBuilder sb = new StringBuilder();
        sb.append(TASK_ID + " = ").append(taskId).append(" AND ");
        sb.append(DATE_TIME).append(" >= ").append(Util.justDate(date).getTimeInMillis());
        return delete(mTableName, sb.toString())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //принудительно вызвать срабатывание триггера таблицы
    public void trigger() {
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            BriteDatabase.Transaction transaction = db.newTransaction();
            try {
                db.executeAndTrigger(mTableName, "SELECT * FROM " + mTableName + " WHERE " +
                        DbContract.ID + " = " + (-1));
                transaction.markSuccessful();
            } finally {
                transaction.end();
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(integer -> {
        });
    }


    public enum Group {
        TASKS, PROJECTS, CATEGORIES, DAY, NONE
    }


    public enum StatUnits {TIME, PROGRESS}

    public static class StatisticItem {
        public double groupAmount;
        public long groupId;
        public String label;
        public int color;
    }
}
