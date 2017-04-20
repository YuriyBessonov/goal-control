package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import app.warinator.goalcontrol.database.DbContract;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.QueuedCols.CONCRETE_TASK_ID;
import static app.warinator.goalcontrol.database.DbContract.QueuedCols.POSITION;
/**
 * Created by Warinator on 07.04.2017.
 */

public class Queued extends BaseModel {
    public long getTaskId() {
        return concreteTaskId;
    }

    public void setTaskId(long concreteTaskId) {
        this.concreteTaskId = concreteTaskId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private long concreteTaskId;
    private int position;

    public Queued(long id, long concreteTaskId, int position) {
        this.id = id;
        this.concreteTaskId = concreteTaskId;
        this.position = position;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        contentValues.put(CONCRETE_TASK_ID, concreteTaskId);
        contentValues.put(POSITION, position);
        return contentValues;
    }

    public static final Func1<Cursor, Queued> FROM_CURSOR = cursor -> new Queued(
            cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
            cursor.getLong(cursor.getColumnIndex(CONCRETE_TASK_ID)),
            cursor.getInt(cursor.getColumnIndex(POSITION))
    );
}
