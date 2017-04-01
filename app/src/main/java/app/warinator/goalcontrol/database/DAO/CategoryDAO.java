package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.hannesdorfmann.sqlbrite.dao.Dao;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.Category;
import rx.Observable;

/**
 * Created by Warinator on 01.04.2017.
 */

public class CategoryDAO extends Dao {

    private static CategoryDAO instance;

    public CategoryDAO() {
        if (instance == null){
            instance = this;
        }
    }

    public static CategoryDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.CategoryCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.CategoryCols._TAB_NAME).execute(db);
        createTable(db);
    }

    public Observable<Long> add(Category category) {
        ContentValues values = category.getContentValues();
        return insert(DbContract.CategoryCols._TAB_NAME, values);
    }

}
