package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TASK_ID;

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

    public Observable<Integer> getTotalAmountDone(Long taskId) {
        return rawQuery(mTableName, "SELECT SUM("+ DbContract.ConcreteTaskCols.AMOUNT_DONE+") FROM "+ mTableName +
                " WHERE " + TASK_ID + " = " + String.valueOf(taskId)).run().mapToOne(new Func1<Cursor, Integer>() {
            @Override
            public Integer call(Cursor cursor) {
                return cursor.getInt(0);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Long> add(final ArrayList<ConcreteTask> items) {
        final ContentValues values = items.get(0).getContentValues();
        Observable<Long> obs = insert(mTableName, values);
        for (int i=1; i<items.size(); i++){
            final int pos = i;
            obs = obs.concatMap(new Func1<Long, Observable<Long>>() {
                @Override
                public Observable<Long> call(Long aLong) {
                    return insert(mTableName, items.get(pos).getContentValues());
                }
            });
        }
        return obs.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
