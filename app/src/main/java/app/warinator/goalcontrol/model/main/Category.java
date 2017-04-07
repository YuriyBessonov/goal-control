package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import app.warinator.goalcontrol.database.DbContract;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.CategoryCols.COLOR;
import static app.warinator.goalcontrol.database.DbContract.CategoryCols.NAME;

/**
 * Created by Warinator on 29.03.2017.
 */
public class Category extends BaseModel {
    private int color = 0;
    private String name;

    public Category() {}

    public Category(long id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        contentValues.put(NAME, name);
        contentValues.put(COLOR, color);
        return contentValues;
    }

    public static final Func1<Cursor, Category> FROM_CURSOR = new Func1<Cursor, Category>() {
        @Override
        public Category call(Cursor cursor) {
            return new Category(
                    cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                    cursor.getString(cursor.getColumnIndex(NAME)),
                    cursor.getInt(cursor.getColumnIndex(COLOR))
            );
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
