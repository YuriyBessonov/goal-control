package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Queued;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static app.warinator.goalcontrol.database.DbContract.QueuedCols.CONCRETE_TASK_ID;
import static app.warinator.goalcontrol.database.DbContract.QueuedCols.POSITION;

/**
 * Created by Warinator on 07.04.2017.
 */

public class QueuedDAO extends BaseDAO<Queued> {
    private static QueuedDAO instance;

    public QueuedDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.QueuedCols._TAB_NAME;
            mMapper = Queued.FROM_CURSOR;
        }
    }

    public static QueuedDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.QueuedCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.QueuedCols._TAB_NAME).execute(db);
        createTable(db);
    }


    //Получить все задачи в очереди
    public Observable<List<ConcreteTask>> getAllQueued(boolean autoUpdates) {
        return rawQuery(mTableName, String.format("SELECT * FROM %s", mTableName))
                .autoUpdates(autoUpdates).run().mapToList(mMapper)
                .onBackpressureLatest()
                .concatMap(new Func1<List<Queued>, Observable<List<ConcreteTask>>>() {
                    @Override
                    public Observable<List<ConcreteTask>> call(List<Queued> items) {
                        LongSparseArray<Integer> index = new LongSparseArray<>();
                        ArrayList<Long> ids = new ArrayList<>();
                        for (Queued i : items){
                            index.put(i.getTaskId(), i.getPosition());
                            ids.add(i.getTaskId());
                        }
                        return ConcreteTaskDAO.getDAO().getSpecified(ids).observeOn(Schedulers.computation())
                                .map(tasks -> {
                            Collections.sort(tasks, (task1, task2) -> {
                                long id1 = task1.getId(), id2 = task2.getId();
                                if (index.get(id1) < index.get(id2)){
                                    return -1;
                                }
                                else if (index.get(id1) > index.get(id2)){
                                    return 1;
                                }
                                else {
                                    return 0;
                                }
                            });
                            return tasks;
                        });
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    //TODO: удалить, если не будет проблем с новой версией
    public Observable<List<ConcreteTask>> getAllQueuedOld(boolean autoUpdates) {
        return rawQuery(mTableName, String.format("SELECT * FROM %s", mTableName))
                .autoUpdates(autoUpdates).run().mapToList(mMapper)
                .flatMap(new Func1<List<Queued>, Observable<List<ConcreteTask>>>() {
                    @Override
                    public Observable<List<ConcreteTask>> call(List<Queued> items) {
                        ArrayList<Observable<ConcreteTask>> obsList = new ArrayList<>();
                        LongSparseArray<Integer> index = new LongSparseArray<>();
                        for (Queued i : items){
                            index.put(i.getTaskId(), i.getPosition());
                            obsList.add(ConcreteTaskDAO.getDAO().get(i.getTaskId()).first());
                        }

                        return Observable.merge(obsList).toSortedList((task1, task2) -> {
                            long id1 = task1.getId(), id2 = task2.getId();
                            if (index.get(id1) < index.get(id2)){
                                return -1;
                            }
                            else if (index.get(id1) > index.get(id2)){
                                return 1;
                            }
                            else {
                                return 0;
                            }
                        });
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //получить последнюю позицию в очереди
    public Observable<Integer> getMaxPos(){
        return rawQuery(mTableName, "SELECT MAX("+POSITION+") FROM "+mTableName).autoUpdates(true).run()
                .mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //обновить позицию задачи в очереди
    public Observable<Integer> updatePos(long taskId, int newPos){
        ContentValues cv = new ContentValues();
        cv.put(POSITION, newPos);
        return update(mTableName, cv, CONFLICT_IGNORE, CONCRETE_TASK_ID+" = "+taskId);
    }


    //добавить в очередь все задачи на сегодня
    public Observable<List<Long>> addAllTodayTasks(){
        Calendar d1 = Util.justDate(Calendar.getInstance());
        Calendar d2 = Calendar.getInstance();
        d2.add(Calendar.DATE, 1);
        d2 = Util.justDate(d2);
        return ConcreteTaskDAO.getDAO().getAllForDateRange(d1, d2).observeOn(Schedulers.computation())
                .zipWith(getMaxPos(), (tasks, maxPos) -> {
            ArrayList<Observable<Long>> observables = new ArrayList<>();
            int pos = maxPos+1;
            for (ConcreteTask t : tasks){
                Queued q = new Queued(0, t.getId(), pos++);
                ContentValues values = q.getContentValues();
                observables.add(insert(mTableName, values, CONFLICT_IGNORE));
            }
            return observables;
        }).flatMap(new Func1<ArrayList<Observable<Long>>, Observable<List<Long>>>() {
            @Override
            public Observable<List<Long>> call(ArrayList<Observable<Long>> observables) {
                return Observable.merge(observables).toList();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //убрать задачу из очереди
    public Observable<Integer> removeTask(long taskId){
        return delete(mTableName, CONCRETE_TASK_ID+" = "+taskId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //добавть задачу в очередь
    public Observable<Long> addTask(long taskId){
        return QueuedDAO.getDAO().getMaxPos().concatMap(new Func1<Integer, Observable<Long>>() {
            @Override
            public Observable<Long> call(Integer maxPos) {
                Queued item = new Queued(0, taskId, maxPos+1);
                ContentValues cv = item.getContentValues();
                return insert(mTableName, cv, CONFLICT_IGNORE);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Задача содержится в очереди
    public Observable<Boolean> containsTask(long taskId){
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT COUNT(*) FROM %s WHERE %s = %d",
                mTableName, CONCRETE_TASK_ID, taskId)).autoUpdates(false).run().mapToOne(cursor -> cursor.getInt(0) > 0)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
