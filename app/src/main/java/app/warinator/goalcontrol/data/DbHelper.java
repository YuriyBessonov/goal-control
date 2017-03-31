package app.warinator.goalcontrol.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Warinator on 30.03.2017.
 */

public class DbHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "tasks.db";
    private static final int SCHEMA = 1;

    private final Context context;

    private static DbHelper mInstance;

    public static DbHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new DbHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE constants (title TEXT, value REAL);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }
}
