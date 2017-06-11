package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.TrackUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static app.warinator.goalcontrol.database.DbContract.TrackUnitCols.NAME;

/**
 * Created by Warinator on 01.04.2017.
 */

public class TrackUnitDAO extends BaseDAO<TrackUnit> {
    private static TrackUnitDAO instance;

    public TrackUnitDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.TrackUnitCols._TAB_NAME;
            mMapper = TrackUnit.FROM_CURSOR;
        }
    }

    public static TrackUnitDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.TrackUnitCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.TrackUnitCols._TAB_NAME).execute(db);
        createTable(db);
    }

    public Observable<List<TrackUnit>> getAllStartingWith(String substr, boolean autoUpdates) {
        return rawQuery(mTableName, String.format("SELECT * FROM %s WHERE %s LIKE '%s%%'",
                mTableName, NAME, substr)).autoUpdates(autoUpdates)
                .run()
                .mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<TrackUnit> getByName(String name) {
        return rawQuery(mTableName, String.format("SELECT * FROM %s WHERE %s = '%s'", mTableName, NAME, name))
                .run()
                .mapToOne(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> exists(String name) {
        return rawQuery(mTableName, "SELECT COUNT(*) FROM "+ mTableName +
                " WHERE " + DbContract.TrackUnitCols.NAME + " = ?").args(name).autoUpdates(false)
                .run().mapToOne(new Func1<Cursor, Boolean>() {
                    @Override
                    public Boolean call(Cursor cursor) {
                        return cursor.getInt(0) > 0;
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
    public Observable<Long> addOrReplace(TrackUnit item){
        ContentValues values = item.getContentValues();
        return insert(mTableName, values, CONFLICT_REPLACE);
    }
}
