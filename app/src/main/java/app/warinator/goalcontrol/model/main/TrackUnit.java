package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import app.warinator.goalcontrol.database.DbContract;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.TrackUnitCols.NAME;
import static app.warinator.goalcontrol.database.DbContract.TrackUnitCols.SHORT_NAME;

/**
 * Created by Warinator on 29.03.2017.
 */

public class TrackUnit  extends BaseModel{
    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    private String name;
    private String shortName;

    public TrackUnit(){}

    public TrackUnit(long id, String name, String shortName) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        contentValues.put(NAME, name);
        contentValues.put(SHORT_NAME, shortName);
        return contentValues;
    }

    public static final Func1<Cursor, TrackUnit> FROM_CURSOR = new Func1<Cursor, TrackUnit>() {
        @Override
        public TrackUnit call(Cursor cursor) {
            return new TrackUnit(
                    cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                    cursor.getString(cursor.getColumnIndex(NAME)),
                    cursor.getString(cursor.getColumnIndex(SHORT_NAME))
            );
        }
    };
}
