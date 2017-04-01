package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;

import app.warinator.goalcontrol.database.DbContract;

/**
 * Created by Warinator on 29.03.2017.
 */

public class Category {
    private int id;
    private int color;

    public Category(int id, int color) {
        this.id = id;
        this.color = color;
    }

    public ContentValues getContentValues(){
        ContentValues contentValues = new ContentValues();
        if (id > 0){
            contentValues.put(DbContract.ID, id);
        }
        contentValues.put(DbContract.CategoryCols.COLOR, color);
        return contentValues;
    }
}
