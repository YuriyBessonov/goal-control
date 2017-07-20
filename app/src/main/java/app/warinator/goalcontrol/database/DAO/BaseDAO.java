package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.Cursor;

import com.hannesdorfmann.sqlbrite.dao.Dao;

import java.util.List;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.BaseModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Базовый DAO
 */
public abstract class BaseDAO<T extends BaseModel> extends Dao {
    protected String mTableName;
    protected Func1<Cursor, T> mMapper;

    //Получить функциональный объект, отображающий курсор в объект типа паремтра
    public Func1<Cursor, T> getMapper() {
        return mMapper;
    }

    //Добавить запись в таблицу
    public Observable<Long> add(T item) {
        ContentValues values = item.getContentValues();
        return insert(mTableName, values);
    }

    //Получить все записи
    //если autoUpdates == true, повторно выполнить запрос и вызывать onNext
    public Observable<List<T>> getAll(boolean autoUpdates) {
        return rawQuery(mTableName, "SELECT * FROM " + mTableName).autoUpdates(autoUpdates)
                .run()
                .mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Получить запись по id
    public Observable<T> get(Long id) {
        return rawQuery(mTableName, "SELECT * FROM " + mTableName +
                " WHERE " + DbContract.ID + " = " + String.valueOf(id)).autoUpdates(false)
                .run()
                .mapToOne(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Обновить запись
    public Observable<Integer> update(T item) {
        return update(mTableName, item.getContentValues(),
                DbContract.ID + " = " + item.getId())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Удалить запись
    public Observable<Integer> delete(T item) {
        return delete(mTableName, DbContract.ID + " = " + item.getId())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


}
