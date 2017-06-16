package app.warinator.goalcontrol.db_wrong;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import app.warinator.goalcontrol.model.Task;

/**
 * Created by Warinator on 16.06.2017.
 */

public class TasksAsyncTask extends AsyncTask<Object, Object, List<Task>> {

    private DbConnector mDbConnector;

    public TasksAsyncTask(Context context){
        mDbConnector = new DbConnector(context);
    }

    @Override
    protected List<Task> doInBackground(Object... params) {
        Cursor cursor = mDbConnector.getAllTasks();
        List<Task> tasks = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                tasks.add(Task.FROM_CURSOR.call(cursor));
                cursor.moveToNext();
            }
        }
        return  tasks;
    }

    @Override
    protected void onPostExecute(List<Task> tasks) {
        super.onPostExecute(tasks);
    }
}

