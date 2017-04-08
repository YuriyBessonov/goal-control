package app.warinator.goalcontrol.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.Task;

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

}
