package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.CheckListItem;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.COMPLETED;
import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.POSITION;
import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.TASK_ID;

/**
 * DAO таблицы чеклистов
 */
public class CheckListItemDAO extends BaseDAO<CheckListItem> {
    private static CheckListItemDAO instance;

    public CheckListItemDAO() {
        if (instance == null) {
            instance = this;
            mTableName = DbContract.CheckListItemCols._TAB_NAME;
            mMapper = CheckListItem.FROM_CURSOR;
        }
    }

    public static CheckListItemDAO getDAO() {
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

    //Получить все записи для указанной задачи
    public Observable<List<CheckListItem>> getAllForTask(long taskId, boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = %d ORDER BY %s",
                mTableName, TASK_ID, taskId, POSITION))
                .autoUpdates(autoUpdates)
                .run()
                .mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Получить количество записей для указанной задачи
    public Observable<Integer> getCountForTask(long taskId, boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT COUNT(*) FROM %s WHERE %s = %d",
                mTableName, TASK_ID, taskId))
                .autoUpdates(autoUpdates)
                .run()
                .mapToOne(cursor -> cursor.getInt(0))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Получить количество отмеченных как выполненные записей для указанной задачи
    public Observable<Integer> getCountDoneForTask(long taskId, boolean autoUpdates) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT COUNT(*) FROM %s WHERE %s = %d AND %s > 0",
                mTableName, TASK_ID, taskId, COMPLETED))
                .autoUpdates(autoUpdates)
                .run()
                .mapToOne(cursor -> cursor.getInt(0))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Добавить записи для задачи
    public Observable<List<Long>> addForTask(ArrayList<CheckListItem> items, long taskId) {
        ArrayList<Observable<Long>> observables = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            CheckListItem item = items.get(i);
            item.setId(0);
            item.setTaskId(taskId);
            item.setPosition(i);
            ContentValues values = item.getContentValues();
            observables.add(insert(mTableName, values));
        }
        return Observable.merge(observables).toList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Удалить все записи для задачи
    public Observable<Integer> deleteForTask(long taskId) {
        return delete(mTableName, TASK_ID + " = " + taskId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Заменить все записи для задачи
    public Observable<List<Long>> replaceForTask(long taskId, ArrayList<CheckListItem> newItems) {
        return deleteForTask(taskId).concatMap(new Func1<Integer, Observable<List<Long>>>() {
            @Override
            public Observable<List<Long>> call(Integer integer) {
                return addForTask(newItems, taskId);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
