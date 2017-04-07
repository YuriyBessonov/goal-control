package app.warinator.goalcontrol.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.CheckListItem;

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

}
