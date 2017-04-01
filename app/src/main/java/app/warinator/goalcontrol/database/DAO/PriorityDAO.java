package app.warinator.goalcontrol.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import com.hannesdorfmann.sqlbrite.dao.Dao;

import app.warinator.goalcontrol.database.DbContract;

/**
 * Created by Warinator on 01.04.2017.
 */

public class PriorityDAO extends Dao {
    private static PriorityDAO instance;

    public PriorityDAO() {
        if (instance == null){
            instance = this;
        }
    }

    public static PriorityDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.PriorityCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.PriorityCols._TAB_NAME).execute(db);
        createTable(db);
    }
}
