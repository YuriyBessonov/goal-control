package app.warinator.goalcontrol.model;

import android.content.ContentValues;

import app.warinator.goalcontrol.database.DbContract;

/**
 * Базовый класс модели таблицы БД
 */
public abstract class BaseModel {
    protected long id = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        if (id > 0) {
            contentValues.put(DbContract.ID, id);
        }
        return contentValues;
    }
}
