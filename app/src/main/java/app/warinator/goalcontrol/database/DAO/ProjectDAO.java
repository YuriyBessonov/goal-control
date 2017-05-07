package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.Locale;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.Project;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static app.warinator.goalcontrol.database.DbContract.ProjectCols.IS_REMOVED;
import static app.warinator.goalcontrol.database.DbContract.ProjectCols.NAME;

/**
 * Created by Warinator on 01.04.2017.
 */


public class ProjectDAO extends RemovableDAO<Project>  {
    private static ProjectDAO instance;

    public ProjectDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.ProjectCols._TAB_NAME;
            mMapper = Project.FROM_CURSOR;
            mColRemoved = IS_REMOVED;
        }
    }

    public static ProjectDAO getDAO(){
        return instance;
    }


    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.ProjectCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.ProjectCols._TAB_NAME).execute(db);
        createTable(db);
    }

    public Observable<Boolean> exists(String name) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT COUNT(*) FROM %s WHERE %s = '%s' AND %s = %d",
                mTableName, NAME, name, mColRemoved, 0)).autoUpdates(false)
                .run().mapToOne(cursor -> cursor.getInt(0) > 0)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<Integer> replaceParent(long oldId, long newId) {
        ContentValues cv = new ContentValues();
        if (newId > 0){
            cv.put(DbContract.ProjectCols.PARENT, newId);
        }
        else {
            cv.putNull(DbContract.ProjectCols.PARENT);
        }
        return update(mTableName, cv, String.format("%s = %d",DbContract.ProjectCols.PARENT, oldId))
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
