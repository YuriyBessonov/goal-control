package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.Task;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Warinator on 01.04.2017.
 */

public class TaskDAO extends BaseDAO<Task> {
    private static TaskDAO instance;

    public TaskDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.TaskCols._TAB_NAME;
            mMapper = Task.FROM_CURSOR;
        }
    }

    public static TaskDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.TaskCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.TaskCols._TAB_NAME).execute(db);
        createTable(db);
    }

    public Observable<Boolean> exists(String name) {
        return rawQuery(mTableName, "SELECT COUNT(*) FROM "+ mTableName +
                " WHERE " + DbContract.TaskCols.NAME + " = ?").args(name).autoUpdates(false)
                .run().mapToOne(new Func1<Cursor, Boolean>() {
                    @Override
                    public Boolean call(Cursor cursor) {
                        return cursor.getInt(0) > 0;
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> replaceProject(long oldId, long newId)
    {
        ContentValues cv = new ContentValues();
        if (newId > 0){
            cv.put(DbContract.TaskCols.PROJECT_ID, newId);
        }
        else {
            cv.putNull(DbContract.TaskCols.PROJECT_ID);
        }
        return update(mTableName, cv, String.format("%s = %d",DbContract.TaskCols.PROJECT_ID, oldId))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
