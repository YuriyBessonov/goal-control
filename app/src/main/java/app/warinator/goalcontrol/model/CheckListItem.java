package app.warinator.goalcontrol.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import app.warinator.goalcontrol.database.DbContract;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.COMPLETED;
import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.POSITION;
import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.TASK_ID;
import static app.warinator.goalcontrol.database.DbContract.CheckListItemCols.VALUE;

/**
 * Элемент чеклиста
 */
public class CheckListItem extends BaseModel implements Parcelable {
    public static final Func1<Cursor, CheckListItem> FROM_CURSOR = cursor -> new CheckListItem(
            cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
            cursor.getLong(cursor.getColumnIndex(TASK_ID)),
            cursor.getInt(cursor.getColumnIndex(POSITION)),
            cursor.getString(cursor.getColumnIndex(VALUE)),
            cursor.getInt(cursor.getColumnIndex(COMPLETED)) > 0
    );
    public static final Creator<CheckListItem> CREATOR = new Creator<CheckListItem>() {
        @Override
        public CheckListItem createFromParcel(Parcel in) {
            return new CheckListItem(in);
        }

        @Override
        public CheckListItem[] newArray(int size) {
            return new CheckListItem[size];
        }
    };
    private long taskId;
    private int position;
    private String value;
    private boolean completed;

    public CheckListItem(long id, long taskId, int position, String value, boolean completed) {
        this.id = id;
        this.value = value;
        this.taskId = taskId;
        this.position = position;
        this.completed = completed;
    }

    public CheckListItem(Parcel src) {
        id = src.readLong();
        taskId = src.readLong();
        position = src.readInt();
        completed = src.readByte() != 0;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        contentValues.put(TASK_ID, taskId);
        contentValues.put(POSITION, position);
        contentValues.put(COMPLETED, completed);
        contentValues.put(VALUE, value);
        return contentValues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(taskId);
        dest.writeInt(position);
        dest.writeByte((byte) (completed ? 1 : 0));
    }
}
