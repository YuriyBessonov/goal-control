package app.warinator.goalcontrol.database.DAO;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.Project;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.model.main.TrackUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Warinator on 01.04.2017.
 */

public class TaskDAO extends BaseDAO<Task> {
    private static TaskDAO instance;

    public TaskDAO() {
        if (instance == null){
            instance = this;
            mTableName = DbContract.TaskCols._TAB_NAME;
            mMapper = Task.FROM_CURSOR;
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

    public Observable<Boolean> exists(String name) {
        return rawQuery(mTableName, "SELECT COUNT(*) FROM "+ mTableName +
                " WHERE " + DbContract.TaskCols.NAME + " = ?").args(name).autoUpdates(false)
                .run().mapToOne(cursor -> cursor.getInt(0) > 0)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> replaceProject(long oldId, long newId)
    {
        ContentValues cv = new ContentValues();
        if (newId > 0){
            cv.put(DbContract.TaskCols.PROJECT_ID, newId);
        }
        else {
            cv.putNull(DbContract.TaskCols.PROJECT_ID);
        }
        return update(mTableName, cv, String.format("%s = %d",DbContract.TaskCols.PROJECT_ID, oldId))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Task>> getAll(boolean autoUpdates) {
      return rawQuery(mTableName, "SELECT * FROM "+ mTableName).autoUpdates(autoUpdates)
                .run()
                .mapToList(mMapper)
                .map(new Func1<List<Task>, Observable<List<Task>>>() {
                    @Override
                    public Observable<List<Task>> call(List<Task> tasks) {
                        List<Observable<Task>> observables = new ArrayList<>();
                        for (Task task : tasks){
                            observables.add(getObservableWithForeign(task));
                        }
                        return Observable.merge(observables).toList();
                    }
                }).flatMap(new Func1<Observable<List<Task>>, Observable<List<Task>>>() {
                  @Override
                  public Observable<List<Task>> call(Observable<List<Task>> listObservable) {
                      return listObservable;
                  }
              }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
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


}
