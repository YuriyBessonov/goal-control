package app.warinator.goalcontrol.model.main;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Calendar;

import app.warinator.goalcontrol.database.DbContract;
import rx.functions.Func1;

import static app.warinator.goalcontrol.database.DbContract.ProjectCols.CATEGORY_ID;
import static app.warinator.goalcontrol.database.DbContract.ProjectCols.COLOR;
import static app.warinator.goalcontrol.database.DbContract.ProjectCols.DEADLINE;
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

    public Project(Project p) {
        name = p.getName();
        deadline = p.getDeadline();
        color = p.getColor();
        categoryId = p.getCategoryId();
        parentId = p.getParentId();
    }

    public Project(long id, String name, Calendar deadline, int color, long categoryId, long parentId){
        this.id = id;
        this.name = name;
        this.deadline = deadline;
        this.color = color;
        this.categoryId = categoryId;
        this.parentId = parentId;
    }

    /*
    public Project(Builder builder) {
        name = builder.name;
        deadline = builder.deadline;
        color = builder.color;
        categoryId = builder.categoryId;
        parentId = builder.parentId;
    }
    */
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

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = super.getContentValues();
        contentValues.put(NAME, name);
        contentValues.put(DEADLINE, deadline.getTimeInMillis());
        contentValues.put(COLOR, color);
        if (categoryId > 0){
            contentValues.put(CATEGORY_ID, categoryId);
        }
        if (parentId > 0){
            contentValues.put(PARENT, parentId);
        }
        return contentValues;
    }

    /*
    public static class Builder {
        private final String name;
        private Calendar deadline = null;
        private int color = 0;
        private long categoryId = 0;
        private long parentId = 0;

        public Builder(String name) {
            this.name = name;
        }

        public Builder deadline(Calendar val) {
            deadline = val;
            return this;
        }

        public Builder color(int val) {
            color = val;
            return this;
        }

        public Builder categoryId(long val) {
            categoryId = val;
            return this;
        }

        public Builder parentId(long val) {
            parentId = val;
            return this;
        }

        public Project build() {
            return new Project(this);
        }
    }
    */

    public static final Func1<Cursor, Project> FROM_CURSOR = new Func1<Cursor, Project>() {
        @Override
        public Project call(Cursor cursor) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DEADLINE)));
            return new Project(
                    cursor.getLong(cursor.getColumnIndex(DbContract.ID)),
                    cursor.getString(cursor.getColumnIndex(NAME)),
                    calendar,
                    cursor.getInt(cursor.getColumnIndex(COLOR)),
                    cursor.getLong(cursor.getColumnIndex(CATEGORY_ID)),
                    cursor.getLong(cursor.getColumnIndex(PARENT))
                    );
        }
    };
}

