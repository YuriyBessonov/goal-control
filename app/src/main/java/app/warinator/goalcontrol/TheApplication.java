package app.warinator.goalcontrol;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;

import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.database.DbManager;
import app.warinator.goalcontrol.job.QueuedTasksJob;
import app.warinator.goalcontrol.job.TasksJobCreator;
import app.warinator.goalcontrol.timer.TimerManager;
import rx.Subscription;

/**
 * Класс приложения
 */
public class TheApplication extends Application {
    private Subscription mSub;
    private TimerManager mTimerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase db = DbManager.getInstance(getApplicationContext()).getDatabase().getReadableDatabase();
        mSub = QueuedDAO.getDAO().addAllTodayTasks().subscribe(longs -> {
            mSub.unsubscribe();
            mSub = null;
        });
        mTimerManager = TimerManager.getInstance(this);
        mTimerManager.restoreTimer();
        Log.v("THE_TIMER", "TIMER RESTORED");

        //ConcreteTaskDAO.getDAO().onUpgrade(db,1,1);
        //TaskDAO.getDAO().onUpgrade(db,1,1);
        //QueuedDAO.getDAO().onUpgrade(db,1,1);
        //DbManager.getInstance(getApplicationContext()).delete(getApplicationContext());

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
        JobManager.create(this).addJobCreator(new TasksJobCreator());
        QueuedTasksJob.schedule();
    }

    @Override
    public void onLowMemory() {
        Log.v("THE_TIMER", "LOW MEMORY");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.v("THE_TIMER", "TRIM MEMORY");
        super.onTrimMemory(level);
    }
}
