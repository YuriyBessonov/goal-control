package app.warinator.goalcontrol.database;

/**
 * Created by Warinator on 30.03.2017.
 */

public class DbContract {
    public static final String ID = "id_";

    public static final class CategoryCols {
        public static final String _TAB_NAME = "category";
        public static final String NAME = "name";
        public static final String COLOR = "color";
        public static final String TABLE_CREATE_QUERY =
                "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    NAME +" TEXT, "+
                    COLOR +" INTEGER"+ ")";
    }

    public static final class CheckListItemCols {
        public static final String _TAB_NAME = "checklist_item";
        public static final String TASK_ID = "task_id";
        public static final String POSITION = "position";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    TASK_ID +" INTEGER, "+
                    POSITION +" INTEGER, "+
                    "FOREIGN KEY("+TASK_ID+") REFERENCES " + TaskCols._TAB_NAME + "("+ID+") "+
                    ");";
    }

    public static final class ChronoModeCols {
        public static final String _TAB_NAME = "chrono_mode";
        public static final String MODE = "mode";
        public static final String NAME = "name";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    MODE +" INTEGER, "+
                    NAME +" TEXT "+
                    ");";
    }

    public static final class PriorityCols {
        public static final String _TAB_NAME = "priority";
        public static final String VALUE = "value";
        public static final String NAME = "name";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    VALUE +" INTEGER, "+
                    NAME +" TEXT "+
                    ");";
    }

    public static final class ProjectCols {
        public static final String _TAB_NAME = "project";
        public static final String NAME = "name";
        public static final String DEADLINE = "deadline";
        public static final String COLOR = "color";
        public static final String PARENT = "parent";
        public static final String CATEGORY_ID = "category_id";


        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    NAME +" TEXT, "+
                    DEADLINE +" INTEGER, "+
                    COLOR +" INTEGER, "+
                    PARENT +" INTEGER, "+
                    CATEGORY_ID +" INTEGER, "+
                    "FOREIGN KEY("+PARENT+") REFERENCES " + _TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+CATEGORY_ID+") REFERENCES " + CategoryCols._TAB_NAME + "("+ID+") "+
                    ");";
    }


    public static final class TrackModeCols {
        public static final String _TAB_NAME = "track_mode";
        public static final String MODE = "mode";
        public static final String NAME = "name";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    MODE +" INTEGER, "+
                    NAME +" TEXT "+
                    ");";
    }

    public static final class TrackUnitCols {
        public static final String _TAB_NAME = "track_unit";
        public static final String NAME = "name";
        public static final String SHORT_NAME = "short_name";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    NAME +" TEXT, "+
                    SHORT_NAME +" TEXT "+
                    ");";
    }

    public static final class WeekdaysCols {
        public static final String _TAB_NAME = "weekdays";
        public static final String MONDAY = "monday";
        public static final String TUESDAY = "tuesday";
        public static final String WEDNESDAY = "wednesday";
        public static final String THURSDAY = "thursday";
        public static final String FRIDAY = "friday";
        public static final String SATURDAY = "saturday";
        public static final String SUNDAY = "sunday";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    MONDAY +" TEXT, "+
                    TUESDAY +" TEXT, "+
                    WEDNESDAY +" TEXT, "+
                    THURSDAY +" TEXT, "+
                    FRIDAY +" TEXT, "+
                    SATURDAY +" TEXT, "+
                    SUNDAY +" TEXT "+
                    ");";
    }

    public static final class TaskCols {
        public static final String _TAB_NAME = "task";
        public static final String NAME = "name";
        public static final String PROJECT_ID = "project_id";
        public static final String PRIORITY_ID = "priority_id";
        public static final String CATEGORY_ID = "category_id";
        public static final String REMINDER = "reminder";
        public static final String NOTE = "note";
        public static final String ICON = "icon";

        public static final String IS_REPEATABLE = "is_repeatable";
        public static final String DATE_BEGIN = "date_begin";
        public static final String WITH_TIME = "with_time";
        public static final String WEEKDAYS_ID = "weekdays_id";
        public static final String REPEAT_COUNT = "repeat_count";
        public static final String IS_INTERVAL = "is_interval";
        public static final String INTERVAL_VALUE = "interval_value";

        public static final String TRACK_MODE_ID = "track_mode_id";
        public static final String UNITS_ID = "units_id";
        public static final String AMOUNT_TOTAL = "amount_total";
        public static final String AMOUNT_ONCE = "amount_once";

        public static final String CHRONO_MODE_ID = "chrono_mode_id";
        public static final String COUNTDOWN_TIME = "countdown_time";
        public static final String WORK_TIME = "work_time";
        public static final String SMALL_BREAK_TIME = "small_break_time";
        public static final String BIG_BREAK_TIME = "big_break_time";
        public static final String INTERVALS_COUNT = "intervals_count";


        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    NAME +" TEXT, "+
                    PROJECT_ID +" INTEGER, "+
                    PRIORITY_ID +" INTEGER, "+
                    CATEGORY_ID +" INTEGER, "+
                    REMINDER +" INTEGER, "+
                    NOTE +" TEXT, "+
                    ICON +" TEXT, "+
                    IS_REPEATABLE +" INTEGER, "+
                    DATE_BEGIN +" INTEGER, "+
                    WITH_TIME +" INTEGER, "+
                    WEEKDAYS_ID +" INTEGER, "+
                    REPEAT_COUNT +" INTEGER, "+
                    IS_INTERVAL +" INTEGER, "+
                    INTERVAL_VALUE +" INTEGER, "+
                    TRACK_MODE_ID +" INTEGER, "+
                    UNITS_ID +" INTEGER, "+
                    AMOUNT_TOTAL +" INTEGER, "+
                    AMOUNT_ONCE +" INTEGER, "+
                    CHRONO_MODE_ID +" INTEGER, "+
                    COUNTDOWN_TIME +" INTEGER, "+
                    WORK_TIME +" INTEGER, "+
                    SMALL_BREAK_TIME +" INTEGER, "+
                    BIG_BREAK_TIME +" INTEGER, "+
                    INTERVALS_COUNT +" INTEGER, "+

                    "FOREIGN KEY("+PROJECT_ID+") REFERENCES " + ProjectCols._TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+PRIORITY_ID+") REFERENCES " + PriorityCols._TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+CATEGORY_ID+") REFERENCES " + CategoryCols._TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+WEEKDAYS_ID+") REFERENCES " + WeekdaysCols._TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+TRACK_MODE_ID+") REFERENCES " + TrackModeCols._TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+UNITS_ID+") REFERENCES " + TrackUnitCols._TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+CHRONO_MODE_ID+") REFERENCES " + ChronoModeCols._TAB_NAME + "("+ID+") "
                    + ");";
    }

}
