package app.warinator.goalcontrol.db_wrong;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.database.DbManager;

/**
 * Created by Warinator on 16.06.2017.
 */


public class DbConnector {
    SQLiteDatabase db;

    public DbConnector(Context context){
        db = DbManager.getInstance(context).getDatabase()
                .getReadableDatabase();
    }

    public Cursor getAllTasks(){
        return db.query(DbContract.TaskCols._TAB_NAME, null, null, null, null, null, null);
    }

    public Cursor getAllProjects(){
        return db.query(DbContract.ProjectCols._TAB_NAME, null, null, null, null, null, null);
    }
}