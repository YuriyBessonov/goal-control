package app.warinator.goalcontrol.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import app.warinator.goalcontrol.database.DbContract.CategoryCols;
import app.warinator.goalcontrol.model.main.Category;

/**
 * Created by Warinator on 01.04.2017.
 */

public class CategoryDAO extends BaseDAO<Category> {
    private static CategoryDAO instance;

    public CategoryDAO() {
        if (instance == null){
            instance = this;
            mTableName = CategoryCols._TAB_NAME;
            mMapper = Category.FROM_CURSOR;
        }
    }

    public static CategoryDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(CategoryCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(CategoryCols._TAB_NAME).execute(db);
        createTable(db);
    }

}
