package app.warinator.goalcontrol.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.CheckListItem;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.TASK_ID;

/**
 * Created by Warinator on 01.04.2017.
 */

public class CheckListItemDAO extends BaseDAO<CheckListItem> {
    private static CheckListItemDAO instance;

    public CheckListItemDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.CheckListItemCols._TAB_NAME;
            mMapper = CheckListItem.FROM_CURSOR;
        }
    }

    public static CheckListItemDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.CheckListItemCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.CheckListItemCols._TAB_NAME).execute(db);
        createTable(db);
    }

    public Observable<List<CheckListItem>> getAllForTask(long taskId, boolean autoUpdates) {
        return rawQuery(mTableName, String.format("SELECT * FROM %s WHERE %s = %d", mTableName, TASK_ID, taskId))
                .autoUpdates(autoUpdates)
                .run()
                .mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
