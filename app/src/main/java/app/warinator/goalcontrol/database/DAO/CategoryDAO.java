package app.warinator.goalcontrol.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import java.util.Locale;

import app.warinator.goalcontrol.database.DbContract.CategoryCols;
import app.warinator.goalcontrol.model.main.Category;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static app.warinator.goalcontrol.database.DbContract.CategoryCols.IS_REMOVED;

/**
 * Created by Warinator on 01.04.2017.
 */

public class CategoryDAO extends RemovableDAO<Category> {
    private static CategoryDAO instance;

    public CategoryDAO() {
        if (instance == null){
            instance = this;
            mTableName = CategoryCols._TAB_NAME;
            mMapper = Category.FROM_CURSOR;
            mColRemoved = IS_REMOVED;
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

    public Observable<Boolean> exists(String name) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT COUNT(*) FROM %s WHERE %s = '%s' AND %s = %d",
                mTableName, CategoryCols.NAME, name, mColRemoved, 0)).autoUpdates(false)
                .run().mapToOne(cursor -> cursor.getInt(0) > 0)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
