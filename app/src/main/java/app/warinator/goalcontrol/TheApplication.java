package app.warinator.goalcontrol;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;

import app.warinator.goalcontrol.database.DbManager;

/**
 * Класс приложения
 */
public class TheApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase db = DbManager.getInstance(getApplicationContext()).getDatabase().getReadableDatabase();
       // db.execSQL("DROP TABLE "+ DbContract.ProjectCols._TAB_NAME);
        //DbManager.getInstance(getApplicationContext()).delete(getApplicationContext());
        /*
        Task task = new Task();
        task.setName("Todo dodo");
        Project p = new Project();
        p.setId(1);
        task.setProject(p);
        task.setPriority(Task.Priority.HIGH);
        Category c = new Category();
        c.setId(2);
        task.setCategory(c);
        task.setReminder(Calendar.getInstance());
        task.setNote("Tsssss");
        task.setIcon(3);
        task.setRepeatable(true);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR,10);
        task.setBeginDate(cal);
        task.setWithTime(false);
        task.setRepeatCount(4);
        task.setInterval(true);
        task.setIntervalValue(5);
        task.setProgressTrackMode(Task.ProgressTrackMode.UNITS);
        task.setAmountOnce(6);
        task.setAmountTotal(7);
        task.setChronoTrackMode(Task.ChronoTrackMode.COUNTDOWN);
        task.setWorkTime(8);
        task.setSmallBreakTime(9);
        task.setBigBreakTime(10);
        task.setIntervalsCount(11);
        */

        Stetho.InitializerBuilder initializerBuilder =
                Stetho.newInitializerBuilder(this);
        initializerBuilder.enableWebKitInspector(
                Stetho.defaultInspectorModulesProvider(this)
        );
        initializerBuilder.enableDumpapp(
                Stetho.defaultDumperPluginsProvider(this)
        );
        Stetho.Initializer initializer = initializerBuilder.build();
        Stetho.initialize(initializer);
    }
}
