package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import app.warinator.goalcontrol.database.DbContract;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.QueueCols.CONCRETE_TASK_ID;
import static app.warinator.goalcontrol.database.DbContract.QueueCols.POSITION;
/**
 * Created by Warinator on 07.04.2017.
 */

public class Queue extends BaseModel {
    private long concreteTaskId;
    private int position;

    public Queue(long id, long concreteTaskId, int position) {
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

    public static final Func1<Cursor, Queue> FROM_CURSOR = new Func1<Cursor, Queue>() {
        @Override
        public Queue call(Cursor cursor) {
            return new Queue(
                    cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                    cursor.getLong(cursor.getColumnIndex(CONCRETE_TASK_ID)),
                    cursor.getInt(cursor.getColumnIndex(POSITION))
            );
        }
    };
}
