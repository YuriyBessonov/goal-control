package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.DATE_TIME;
import static app.warinator.goalcontrol.database.DbContract.ConcreteTaskCols.TASK_ID;

/**
 * Created by Warinator on 07.04.2017.
 */

public class ConcreteTaskDAO extends BaseDAO<ConcreteTask>{
    private static ConcreteTaskDAO instance;

    public ConcreteTaskDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.ConcreteTaskCols._TAB_NAME;
            mMapper = ConcreteTask.FROM_CURSOR;
        }
    }

    public static ConcreteTaskDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.ConcreteTaskCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.ConcreteTaskCols._TAB_NAME).execute(db);
        createTable(db);
    }

    //Все задачи, назначенные в дни не ранее, чем d1, но ранее, чем d2
    public Observable<List<ConcreteTask>> getAllForDateRange(Calendar d1, Calendar d2) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s >= %d AND %s < %d", mTableName, DATE_TIME,
                d1.getTimeInMillis(), DATE_TIME, d2.getTimeInMillis())).autoUpdates(true).run().mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Все задачи с неуказанной датой
    public Observable<List<ConcreteTask>> getAllWithNoDate() {
        return rawQuery(mTableName, String.format("SELECT * FROM %s WHERE %s IS NULL",
                mTableName, DATE_TIME)).autoUpdates(true).run().mapToList(mMapper)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Сумма единиц выполнения для задачи
    public Observable<Integer> getTotalAmountDone(Long taskId) {
        return rawQuery(mTableName, String.format("SELECT SUM(%s) FROM %s WHERE %s = %s",
                DbContract.ConcreteTaskCols.AMOUNT_DONE, mTableName, TASK_ID, String.valueOf(taskId)))
                .run().mapToOne(cursor -> cursor.getInt(0)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Добавить множество задач
    public Observable<Long> add(final ArrayList<ConcreteTask> items) {
        ArrayList<Observable<Long>> observables = new ArrayList<>();
        for (ConcreteTask t : items){
            ContentValues values = t.getContentValues();
            observables.add(insert(mTableName, values));
        }
        return Observable.merge(observables).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //Количество повторений задачи, начиная с сегодняшнего дня
    public Observable<Integer> getTimesLeftStartingToday(long taskId){
        Calendar now = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(0);
        today.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        long timeMs = today.getTimeInMillis();
        return rawQuery(mTableName, String.format(Locale.getDefault(), "SELECT COUNT(*) FROM %s WHERE %s = %d AND %s >= %d",
                mTableName, TASK_ID, taskId, DATE_TIME, timeMs)).run().mapToOne(cursor -> cursor.getInt(0))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
