package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;

import java.util.List;
import java.util.Locale;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.BaseModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

/**
 * DAO таблиц с помечаемыми как удаленные записями
 */
public abstract class RemovableDAO<T extends BaseModel> extends BaseDAO<T> {
    protected String mColRemoved;

    //Отметить задачу как удаленную
    public Observable<Integer> markAsRemoved(long id) {
        ContentValues cv = new ContentValues();
        cv.put(mColRemoved, true);
        return update(mTableName, cv, CONFLICT_IGNORE, DbContract.ID + " = " + id)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Получить все записи, с учётом или без учета отмеченных как удаленные
    public Observable<List<T>> getAll(boolean autoUpdates, boolean withRemoved) {
        StringBuilder querySb = new StringBuilder();
        querySb.append("SELECT * FROM ").append(mTableName);
        if (!withRemoved) {
            querySb.append(String.format(Locale.getDefault(),
                    " WHERE %s = %d", mColRemoved, 0));
        }
        return rawQuery(mTableName, querySb.toString()).autoUpdates(autoUpdates)
                .run().mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Получить список записей по списку id, с учётом или без учета отмеченных как удаленные
    public Observable<List<T>> get(List<Long> ids, boolean withRemoved) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(mTableName).append(" WHERE ");
        if (!withRemoved) {
            sb.append(String.format(Locale.getDefault(),
                    "%s = %d AND ", mColRemoved, 0));
        }
        sb.append(DbContract.ID).append(" IN ( ");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(ids.get(i));
            if (i < ids.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(" )");
        return rawQuery(mTableName, sb.toString()).autoUpdates(false).run().mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
