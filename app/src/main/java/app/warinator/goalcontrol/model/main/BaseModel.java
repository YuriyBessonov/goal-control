package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;

/**
 * Created by Warinator on 02.04.2017.
 */

public abstract class BaseModel {
    protected long id = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public abstract ContentValues getContentValues();
}
