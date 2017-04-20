package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Queued;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
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
        return rawQuery(mTableName, String.format("SELECT * FROM %s ORDER BY %s",
                mTableName, POSITION)).autoUpdates(autoUpdates).run().mapToList(mMapper)
                .flatMap(new Func1<List<Queued>, Observable<List<ConcreteTask>>>() {
                    @Override
                    public Observable<List<ConcreteTask>> call(List<Queued> items) {
                        ArrayList<Observable<ConcreteTask>> obsList = new ArrayList<>();
                        for (Queued i : items){
                            obsList.add(ConcreteTaskDAO.getDAO().get(i.getTaskId()).first());
                        }
                        return Observable.merge(obsList).toList();
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> getMaxPos(){
        return rawQuery(mTableName, "SELECT MAX("+POSITION+") FROM "+mTableName).autoUpdates(false).run()
                .mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Добавить в очередь все задачи на сегодня
    public Observable<Long> addAllTodayTasks(){
        Calendar d1 = Util.justDate(Calendar.getInstance());
        Calendar d2 = Calendar.getInstance();
        d2.add(Calendar.DATE, 1);
        d2 = Util.justDate(d2);
        return ConcreteTaskDAO.getDAO().getAllForDateRange(d1, d2).zipWith(getMaxPos(), (tasks, maxPos) -> {
            ArrayList<Observable<Long>> observables = new ArrayList<>();
            int pos = maxPos+1;
            for (ConcreteTask t : tasks){
                Queued q = new Queued(0, t.getId(), pos++);
                ContentValues values = q.getContentValues();
                observables.add(insert(mTableName, values, CONFLICT_IGNORE));
            }
            return observables;
        }).flatMap(new Func1<ArrayList<Observable<Long>>, Observable<Long>>() {
            @Override
            public Observable<Long> call(ArrayList<Observable<Long>> observables) {
                return Observable.merge(observables);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
