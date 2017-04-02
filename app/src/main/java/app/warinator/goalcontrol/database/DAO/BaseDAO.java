package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.Cursor;

import com.hannesdorfmann.sqlbrite.dao.Dao;

import java.util.List;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.BaseModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public abstract class BaseDAO<T extends BaseModel> extends Dao {
    protected String mTableName;
    protected Func1<Cursor, T> mMapper;

    public Observable<Long> add(T item) {
        ContentValues values = item.getContentValues();
        return insert(mTableName, values);
    }

    public Observable<List<T>> getAll() {
        return rawQuery(mTableName, "SELECT * FROM "+ mTableName).autoUpdates(false)
                .run()
                .mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<T> get(Long id) {
        return rawQuery(mTableName, "SELECT * FROM "+ mTableName +
                " WHERE " + DbContract.ID + " = " + String.valueOf(id))
                .run()
                .mapToOne(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> update(T item) {
        return update(mTableName, item.getContentValues(),
                DbContract.ID+" = "+item.getId(),null)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> delete(T item) {
        return delete(mTableName, DbContract.ID+" = "+item.getId(), null)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}