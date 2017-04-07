package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import app.warinator.goalcontrol.database.DbContract;
import app.warinator.goalcontrol.util.Util;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.WeekdaysCols.FRIDAY;
import static app.warinator.goalcontrol.database.DbContract.WeekdaysCols.MONDAY;
import static app.warinator.goalcontrol.database.DbContract.WeekdaysCols.SATURDAY;
import static app.warinator.goalcontrol.database.DbContract.WeekdaysCols.SUNDAY;
import static app.warinator.goalcontrol.database.DbContract.WeekdaysCols.THURSDAY;
import static app.warinator.goalcontrol.database.DbContract.WeekdaysCols.TUESDAY;
import static app.warinator.goalcontrol.database.DbContract.WeekdaysCols.WEDNESDAY;

/**
 * Created by Warinator on 29.03.2017.
 */

public class Weekdays extends BaseModel{
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();;
        contentValues.put(MONDAY, Util.intBool(monday));
        contentValues.put(TUESDAY, Util.intBool(tuesday));
        contentValues.put(WEDNESDAY, Util.intBool(wednesday));
        contentValues.put(THURSDAY, Util.intBool(thursday));
        contentValues.put(FRIDAY, Util.intBool(friday));
        contentValues.put(SATURDAY, Util.intBool(saturday));
        contentValues.put(SUNDAY, Util.intBool(sunday));
        return contentValues;
    }

    public Weekdays( long id, boolean monday, boolean tuesday, boolean wednesday,
                     boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        this.id = id;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }

    public static final Func1<Cursor, Weekdays> FROM_CURSOR = new Func1<Cursor, Weekdays>() {
        @Override
        public Weekdays call(Cursor cursor) {
            return new Weekdays(
                    cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                    cursor.getInt(cursor.getColumnIndex(MONDAY)) > 0,
                    cursor.getInt(cursor.getColumnIndex(TUESDAY)) > 0,
                    cursor.getInt(cursor.getColumnIndex(WEDNESDAY)) > 0,
                    cursor.getInt(cursor.getColumnIndex(THURSDAY)) > 0,
                    cursor.getInt(cursor.getColumnIndex(FRIDAY)) > 0,
                    cursor.getInt(cursor.getColumnIndex(SATURDAY)) > 0,
                    cursor.getInt(cursor.getColumnIndex(SUNDAY)) > 0
            );
        }
    };
}
