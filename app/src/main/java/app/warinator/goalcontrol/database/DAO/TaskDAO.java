package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.Project;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.model.main.TrackUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.IS_REMOVED;
import static app.warinator.goalcontrol.database.DbContract.TaskCols.NAME;

/**
 * Created by Warinator on 01.04.2017.
 */

public class TaskDAO extends RemovableDAO<Task> {
    private static TaskDAO instance;

    public TaskDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.TaskCols._TAB_NAME;
            mMapper = Task.FROM_CURSOR;
            mColRemoved = IS_REMOVED;
        }
    }

    public static TaskDAO getDAO(){
        return instance;
    }

    @Override
    public void createTable(SQLiteDatabase database) {
        database.execSQL(DbContract.TaskCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DROP_TABLE(DbContract.TaskCols._TAB_NAME).execute(db);
        createTable(db);
    }

    @Override
    public Observable<Integer> markAsRemoved(long id){
        ContentValues cv = new ContentValues();
        cv.put(IS_REMOVED, true);
        return update(mTableName, cv, CONFLICT_IGNORE ,DbContract.ID+" = "+id)
                .concatMap(integer -> ConcreteTaskDAO.getDAO().markAsRemovedForTask(id))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> exists(String name) {
        return rawQuery(mTableName, String.format(Locale.getDefault(),
                "SELECT COUNT(*) FROM %s WHERE %s = '%s' AND %s = %d",
                mTableName, NAME, name, mColRemoved, 0)).autoUpdates(false)
                .run().mapToOne(cursor -> cursor.getInt(0) > 0)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> replaceProject(long oldId, long newId) {
        ContentValues cv = new ContentValues();
        if (newId > 0){
            cv.put(DbContract.TaskCols.PROJECT_ID, newId);
        }
        else {
            cv.putNull(DbContract.TaskCols.PROJECT_ID);
        }
        return update(mTableName, cv, DbContract.TaskCols.PROJECT_ID+" = "+oldId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Task>> getAll(boolean autoUpdates, boolean withRemoved) {
        StringBuilder querySb = new StringBuilder();
        querySb.append("SELECT * FROM ").append(mTableName);
        if (!withRemoved){
            querySb.append(String.format(Locale.getDefault(),
                    " WHERE %s = %d", IS_REMOVED, 0));
        }
        return rawQuery(mTableName, querySb.toString()).autoUpdates(autoUpdates)
                .run()
                .mapToList(mMapper)
                .map(tasks -> {
                    List<Observable<Task>> observables = new ArrayList<>();
                    for (Task task : tasks){
                        observables.add(getObservableWithForeign(task));
                    }
                    return Observable.merge(observables).toList();
                }).flatMap(listObservable -> listObservable)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<Task> getObservableWithForeign(Task task){
        Observable<Category> categoryObs = (task.getCategory() != null) ?
                CategoryDAO.getDAO().get(task.getCategory().getId()) :
                Observable.just(null);
        Observable<Project>  projectObs = (task.getProject() != null) ?
                ProjectDAO.getDAO().get(task.getProject().getId()) :
                Observable.just(null);
        Observable<TrackUnit> unitsObs = (task.getUnits() != null) ?
                TrackUnitDAO.getDAO().get(task.getUnits().getId()) :
                Observable.just(null);
       return Observable.zip(Observable.just(task), categoryObs,
                projectObs, unitsObs, (task1, category, project, trackUnit) -> {
                    task1.setCategory(category);
                    task1.setProject(project);
                    task1.setUnits(trackUnit);
                    return task1;
                });
    }

    @Override
    public Observable<Task> get(Long id) {
        return rawQuery(mTableName, "SELECT * FROM "+ mTableName +
                " WHERE " + DbContract.ID + " = " + String.valueOf(id)).autoUpdates(false)
                .run()
                .mapToOne(mMapper).map(this::getObservableWithForeign)
                .flatMap(new Func1<Observable<Task>, Observable<Task>>() {
                    @Override
                    public Observable<Task> call(Observable<Task> taskObservable) {
                        return taskObservable;
                    }
                });
    }

    @Override
    public Observable<List<Task>> get(List<Long> ids, boolean withRemoved) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(mTableName).append(" WHERE ");
        if (!withRemoved){
            sb.append(String.format(Locale.getDefault(),
                    "%s = %d AND ", mColRemoved, 0));
        }
        sb.append(DbContract.ID).append(" IN ( ");
        for (int i=0; i < ids.size(); i++){
            sb.append(ids.get(i));
            if (i < ids.size() - 1){
                sb.append(", ");
            }
        }
        sb.append(" )");
        return rawQuery(mTableName, sb.toString()).autoUpdates(false).run().mapToList(mMapper)
                .map(tasks -> {
                    List<Observable<Task>> observables = new ArrayList<>();
                    for (Task task : tasks){
                        observables.add(getObservableWithForeign(task));
                    }
                    return Observable.merge(observables).toList();
                }).flatMap(listObservable -> listObservable)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


}
