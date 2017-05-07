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
        public static final String IS_REMOVED = "removed";

        public static final String TABLE_CREATE_QUERY =
                "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    NAME +" TEXT, "+
                    COLOR +" INTEGER, "+
                    IS_REMOVED +" INTEGER "+
                    ")";
    }

    public static final class CheckListItemCols {
        public static final String _TAB_NAME = "checklist_item";
        public static final String TASK_ID = "task_id";
        public static final String POSITION = "position";
        public static final String VALUE = "value";
        public static final String COMPLETED = "completed";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    TASK_ID +" INTEGER NOT NULL, "+
                    POSITION +" INTEGER, "+
                    VALUE +" TEXT, "+
                    COMPLETED +" INTEGER, "+
                    "FOREIGN KEY("+TASK_ID+") REFERENCES " + TaskCols._TAB_NAME + "("+ID+") ON DELETE CASCADE "+
                    ");";
    }

    public static final class ProjectCols {
        public static final String _TAB_NAME = "project";
        public static final String NAME = "name";
        public static final String DEADLINE = "deadline";
        public static final String COLOR = "color";
        public static final String PARENT = "parent";
        public static final String CATEGORY_ID = "category_id";
        public static final String IS_REMOVED = "removed";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    NAME +" TEXT, "+
                    DEADLINE +" INTEGER, "+
                    COLOR +" INTEGER, "+
                    PARENT +" INTEGER, "+
                    CATEGORY_ID +" INTEGER, "+
                    IS_REMOVED +" INTEGER, "+
                    "FOREIGN KEY("+PARENT+") REFERENCES " + _TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+CATEGORY_ID+") REFERENCES " + CategoryCols._TAB_NAME + "("+ID+") "+
                    ");";
    }

    public static final class TrackUnitCols {
        public static final String _TAB_NAME = "track_unit";
        public static final String NAME = "name";
        public static final String SHORT_NAME = "short_name";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    NAME +" TEXT UNIQUE, "+
                    SHORT_NAME +" TEXT "+
                    ");";
    }

    public static final class TaskCols {
        public static final String _TAB_NAME = "task";
        public static final String NAME = "name";
        public static final String PROJECT_ID = "project_id";
        public static final String PRIORITY = "priority";
        public static final String CATEGORY_ID = "category_id";
        public static final String REMINDER = "reminder";
        public static final String NOTE = "note";
        public static final String ICON = "icon";

        public static final String IS_REPEATABLE = "is_repeatable";
        public static final String DATE_BEGIN = "date_begin";
        public static final String WITH_TIME = "with_time";
        public static final String WEEKDAYS = "weekdays";
        public static final String REPEAT_COUNT = "repeat_count";
        public static final String IS_INTERVAL = "is_interval";
        public static final String INTERVAL_VALUE = "interval_value";

        public static final String TRACK_MODE = "track_mode";
        public static final String UNITS_ID = "units_id";
        public static final String AMOUNT_TOTAL = "amount_total";
        public static final String AMOUNT_ONCE = "amount_once";

        public static final String CHRONO_MODE = "chrono_mode";
        public static final String WORK_TIME = "work_time";
        public static final String SMALL_BREAK_TIME = "small_break_time";
        public static final String BIG_BREAK_TIME = "big_break_time";
        public static final String INTERVALS_COUNT = "intervals_count";
        public static final String BIG_BREAK_EVERY = "big_break_every";

        public static final String IS_REMOVED = "removed";

        public static final String TABLE_CREATE_QUERY =
                    "CREATE TABLE "+ _TAB_NAME +
                    " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    NAME +" TEXT, "+
                    PROJECT_ID +" INTEGER, "+
                    PRIORITY +" INTEGER, "+
                    CATEGORY_ID +" INTEGER, "+
                    REMINDER +" INTEGER, "+
                    NOTE +" TEXT, "+
                    ICON +" INTEGER, "+
                    IS_REPEATABLE +" INTEGER, "+
                    DATE_BEGIN +" INTEGER, "+
                    WITH_TIME +" INTEGER, "+
                            WEEKDAYS +" INTEGER, "+
                    REPEAT_COUNT +" INTEGER, "+
                    IS_INTERVAL +" INTEGER, "+
                    INTERVAL_VALUE +" INTEGER, "+
                    TRACK_MODE +" INTEGER, "+
                    UNITS_ID +" INTEGER, "+
                    AMOUNT_TOTAL +" INTEGER, "+
                    AMOUNT_ONCE +" INTEGER, "+
                    CHRONO_MODE +" INTEGER, "+
                    WORK_TIME +" INTEGER, "+
                    SMALL_BREAK_TIME +" INTEGER, "+
                    BIG_BREAK_TIME +" INTEGER, "+
                    INTERVALS_COUNT +" INTEGER, "+
                    BIG_BREAK_EVERY +" INTEGER, "+
                    IS_REMOVED +" INTEGER, "+
                    "FOREIGN KEY("+PROJECT_ID+") REFERENCES " + ProjectCols._TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+CATEGORY_ID+") REFERENCES " + CategoryCols._TAB_NAME + "("+ID+"), "+
                    "FOREIGN KEY("+UNITS_ID+") REFERENCES " + TrackUnitCols._TAB_NAME + "("+ID+") "+
                     ");";
    }

    public static final class ConcreteTaskCols {
        public static final String _TAB_NAME = "concrete_task";
        public static final String TASK_ID = "task_id";
        public static final String DATE_TIME = "date_time";
        public static final String DELAY = "delay";
        public static final String AMOUNT_DONE = "amount_done";
        public static final String TIME_SPENT = "time_spent";
        public static final String IS_REMOVED = "removed";

        public static final String TABLE_CREATE_QUERY =
                "CREATE TABLE "+ _TAB_NAME +
                        " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        TASK_ID +" INTEGER NOT NULL, "+
                        DATE_TIME +" INTEGER, "+
                        DELAY +" INTEGER, "+
                        AMOUNT_DONE +" INTEGER, "+
                        TIME_SPENT +" INTEGER, "+
                        IS_REMOVED +" INTEGER, "+
                        "FOREIGN KEY("+TASK_ID+") REFERENCES " + TaskCols._TAB_NAME + "("+ID+") ON DELETE CASCADE "+
                        ");";
    }

    public static final class QueuedCols {
        public static final String _TAB_NAME = "queued";
        public static final String CONCRETE_TASK_ID = "concrete_task_id";
        public static final String POSITION = "position";

        public static final String TABLE_CREATE_QUERY =
                "CREATE TABLE "+ _TAB_NAME +
                        " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        CONCRETE_TASK_ID +" INTEGER UNIQUE NOT NULL, "+
                        POSITION +" INTEGER, "+
                        "FOREIGN KEY("+CONCRETE_TASK_ID+") REFERENCES " + ConcreteTaskCols._TAB_NAME + "("+ID+") ON DELETE CASCADE "+
                        ");";
    }
}
