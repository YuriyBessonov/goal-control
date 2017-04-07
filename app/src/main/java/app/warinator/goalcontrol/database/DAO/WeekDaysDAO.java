package app.warinator.goalcontrol.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import com.hannesdorfmann.sqlbrite.dao.Dao;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.Weekdays;

/**
 * Created by Warinator on 01.04.2017.
 */


public class WeekDaysDAO extends BaseDAO<Weekdays> {
    private static WeekDaysDAO instance;

    public WeekDaysDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.WeekdaysCols._TAB_NAME;
            mMapper = Weekdays.FROM_CURSOR;
        }
    }

    public static WeekDaysDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.WeekdaysCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.WeekdaysCols._TAB_NAME).execute(db);
        createTable(db);
    }
}
