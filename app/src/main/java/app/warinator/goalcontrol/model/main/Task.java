package app.warinator.goalcontrol.model.main;

import java.util.Date;

/**
 * Created by Warinator on 29.03.2017.
 */

public class Task {
    private int id;
    private String name;
    private int projectId;
    private int priorityId;
    private int categoryId;
    private Date reminder;
    private String note;
    private String icon;

    private boolean is_repeatable;
    private Date beginDate;
    private boolean withTime;
    private int weekdaysId;
    private int repeatCount;
    private boolean isInterval;
    private int intervalValue;

    private int trackModeId;
    private int unitsId;
    private int amountTotal;
    private int amountOnce;

    private int chronoModeId;
    private int countdownTime;
    private int workTime;
    private int smallBreakTime;
    private int bigBreakTime;
    private int intervalsCount;
}
