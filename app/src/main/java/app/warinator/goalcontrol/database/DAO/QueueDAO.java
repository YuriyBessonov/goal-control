package app.warinator.goalcontrol.database.DAO;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Queue;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static app.warinator.goalcontrol.database.DbContract.QueueCols.POSITION;

/**
 * Created by Warinator on 07.04.2017.
 */

public class QueueDAO extends BaseDAO<Queue> {
    private static QueueDAO instance;

    public QueueDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.QueueCols._TAB_NAME;
            mMapper = Queue.FROM_CURSOR;
        }
    }

    public static QueueDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.QueueCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.QueueCols._TAB_NAME).execute(db);
        createTable(db);
    }


    public Observable<List<ConcreteTask>> getAllQueued(boolean autoUpdates) {
        return rawQuery(mTableName, String.format("SELECT * FROM %s ORDER BY %s",
                mTableName, POSITION)).autoUpdates(autoUpdates).run().mapToList(mMapper)
                .concatMap(new Func1<List<Queue>, Observable<List<ConcreteTask>>>() {
                    @Override
                    public Observable<List<ConcreteTask>> call(List<Queue> items) {
                        ArrayList<Observable<ConcreteTask>> obsList = new ArrayList<>();
                        for (Queue i : items){
                            obsList.add(ConcreteTaskDAO.getDAO().get(i.getId()));
                        }
                        return Observable.merge(obsList).toList();
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
