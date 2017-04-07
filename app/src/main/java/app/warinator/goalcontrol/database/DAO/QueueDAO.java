package app.warinator.goalcontrol.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.Queue;

/**
 * Created by Warinator on 07.04.2017.
 */

public class QueueDAO extends BaseDAO<Queue> {
    private static QueueDAO instance;

    public QueueDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.QueueCols._TAB_NAME;
            mMapper = Queue.FROM_CURSOR;
        }
    }

    public static QueueDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.QueueCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.QueueCols._TAB_NAME).execute(db);
        createTable(db);
    }

}
