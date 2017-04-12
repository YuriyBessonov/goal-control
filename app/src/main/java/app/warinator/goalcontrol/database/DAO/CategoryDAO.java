package app.warinator.goalcontrol.database.DAO;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.database.DbContract.CategoryCols;
import app.warinator.goalcontrol.model.main.Category;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

    public Observable<Boolean> exists(String name) {
        return rawQuery(mTableName, "SELECT COUNT(*) FROM "+ mTableName +
                " WHERE " + DbContract.CategoryCols.NAME + " = ?").args(name).autoUpdates(false)
                .run().mapToOne(new Func1<Cursor, Boolean>() {
                    @Override
                    public Boolean call(Cursor cursor) {
                        return cursor.getInt(0) > 0;
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
