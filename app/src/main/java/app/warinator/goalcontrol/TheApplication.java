package app.warinator.goalcontrol;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;

import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.database.DbManager;
import rx.Subscription;

/**
 * Класс приложения
 */
public class TheApplication extends Application {
    private Subscription mSub;

    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase db = DbManager.getInstance(getApplicationContext()).getDatabase().getReadableDatabase();
        mSub = QueuedDAO.getDAO().addAllTodayTasks().subscribe(longs -> {
            mSub.unsubscribe();
            mSub = null;
        });
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
}
