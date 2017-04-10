package app.warinator.goalcontrol.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Warinator on 30.03.2017.
 */

public class DbHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "tasks.db";
    private static final int SCHEMA = 1;

    private final Context mContext;

    private static DbHelper mInstance;

    public static DbHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new DbHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbContract.CategoryCols.TABLE_CREATE_QUERY);
        db.execSQL(DbContract.ProjectCols.TABLE_CREATE_QUERY);
        db.execSQL(DbContract.TrackUnitCols.TABLE_CREATE_QUERY);
        db.execSQL(DbContract.TaskCols.TABLE_CREATE_QUERY);
        db.execSQL(DbContract.CheckListItemCols.TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.CategoryCols._TAB_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.ProjectCols._TAB_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.TrackUnitCols._TAB_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.TaskCols._TAB_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.CheckListItemCols._TAB_NAME);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.CategoryCols._TAB_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.ProjectCols._TAB_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.TrackUnitCols._TAB_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.TaskCols._TAB_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ DbContract.CheckListItemCols._TAB_NAME);
        onCreate(db);
    }

    public void deleteDatabase(){
        mContext.deleteDatabase(DATABASE_NAME);
    }
}
