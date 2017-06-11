package app.warinator.goalcontrol.model;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Calendar;

import app.warinator.goalcontrol.database.DbContract;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.ProjectCols.CATEGORY_ID;
import static app.warinator.goalcontrol.database.DbContract.ProjectCols.COLOR;
import static app.warinator.goalcontrol.database.DbContract.ProjectCols.DEADLINE;
import static app.warinator.goalcontrol.database.DbContract.ProjectCols.IS_REMOVED;
import static app.warinator.goalcontrol.database.DbContract.ProjectCols.NAME;
import static app.warinator.goalcontrol.database.DbContract.ProjectCols.PARENT;

/**
 * Created by Warinator on 29.03.2017.
 */

public class Project extends BaseModel implements Serializable {
    private String name;
    private Calendar deadline;
    private int color;
    private long categoryId;
    private long parentId;
    private boolean isRemoved;


    public Project(Project p) {
        id = p.getId();
        name = p.getName();
        deadline = p.getDeadline();
        color = p.getColor();
        categoryId = p.getCategoryId();
        parentId = p.getParentId();
        isRemoved = p.isRemoved();
    }

    public Project(long id, String name, Calendar deadline, int color, long categoryId, long parentId, boolean isRemoved){
        this.id = id;
        this.name = name;
        this.deadline = deadline;
        this.color = color;
        this.categoryId = categoryId;
        this.parentId = parentId;
        this.isRemoved = isRemoved;
    }

    public Project() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getDeadline() {
        return deadline;
    }

    public void setDeadline(Calendar deadline) {
        this.deadline = deadline;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        contentValues.put(NAME, name);
        if (deadline != null){
            contentValues.put(DEADLINE, deadline.getTimeInMillis());
        }
        contentValues.put(COLOR, color);
        if (categoryId > 0){
            contentValues.put(CATEGORY_ID, categoryId);
        }
        else {
            contentValues.putNull(CATEGORY_ID);
        }
        if (parentId > 0){
            contentValues.put(PARENT, parentId);
        }
        else {
            contentValues.putNull(PARENT);
        }
        contentValues.put(IS_REMOVED, isRemoved);
        return contentValues;
    }


    public static final Func1<Cursor, Project> FROM_CURSOR = new Func1<Cursor, Project>() {
        @Override
        public Project call(Cursor cursor) {
            Calendar calendar = null;
            long deadline = cursor.getLong(cursor.getColumnIndex(DEADLINE));
            if (deadline > 0){
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(deadline);
            }
            return new Project(
                    cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                    cursor.getString(cursor.getColumnIndex(NAME)),
                    calendar,
                    cursor.getInt(cursor.getColumnIndex(COLOR)),
                    cursor.getLong(cursor.getColumnIndex(CATEGORY_ID)),
                    cursor.getLong(cursor.getColumnIndex(PARENT)),
                    cursor.getInt(cursor.getColumnIndex(IS_REMOVED)) > 0
                    );
        }
    };
}

