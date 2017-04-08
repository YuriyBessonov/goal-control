package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import app.warinator.goalcontrol.database.DbContract;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.COMPLETED;
import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.POSITION;
import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.TASK_ID;

/**
 * Created by Warinator on 29.03.2017.
 */

public class CheckListItem extends BaseModel {
    private long taskId;
    private int position;
    private boolean completed;

    public CheckListItem(long id, long taskId, int position, boolean completed) {
        this.id = id;
        this.taskId = taskId;
        this.position = position;
        this.completed = completed;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        contentValues.put(TASK_ID, taskId);
        contentValues.put(POSITION, position);
        contentValues.put(COMPLETED, completed);
        return contentValues;
    }

    public static final Func1<Cursor, CheckListItem> FROM_CURSOR = new Func1<Cursor, CheckListItem>() {
        @Override
        public CheckListItem call(Cursor cursor) {
            return new CheckListItem(
                    cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                    cursor.getLong(cursor.getColumnIndex(TASK_ID)),
                    cursor.getInt(cursor.getColumnIndex(POSITION)),
                    cursor.getInt(cursor.getColumnIndex(COMPLETED)) > 0
            );
        }
    };
}
