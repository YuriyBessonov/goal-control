package app.warinator.goalcontrol.model.misc;

import android.graphics.Color;

/**
 * Created by Warinator on 26.01.2017.
 */

public class DummyTask {
    public String name;
    public String project;
    public int donePercent;
    public int needPercent;
    public int doneToday;
    public int needToday;
    public int doneAll;
    public int needAll;
    public boolean repeat;
    public String dueTime;
    public String dueDate;
    public String icon;
    public int color;

    public DummyTask(){
        name = "TaskCols Name";
        project = "ProjectCols Name";
        donePercent = 37;
        needPercent = 48;
        doneToday = 0;
        needToday = 24;
        doneAll = 78;
        needAll = 212;
        dueDate = "вторник, 13.02";
        dueTime = "14:30";
        repeat = true;
        icon = "cmd_alarm";
        color = Color.rgb(113, 56, 79);
    }
}
