package app.warinator.goalcontrol;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;

import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.database.DbManager;
import app.warinator.goalcontrol.job.TasksJobCreator;
import app.warinator.goalcontrol.timer.TimerNotificationService;
import rx.Subscription;

import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_SHOW_NOTIFICATION;

/**
 * Класс приложения
 */
public class TheApplication extends Application {
    private Subscription mQueuedSub;
    private Subscription mNotifSub;

    @Override
    public void onCreate() {
        super.onCreate();
        initApp();
        //TimerManager.getInstance(this).restoreTimer();
        //Log.v("THE_TIMER", "TIMER RESTORED");

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
    }

    private void initApp(){
        //обеспечение доступа к БД
        SQLiteDatabase db = DbManager.getInstance(getApplicationContext()).getDatabase().getReadableDatabase();

        //добавление задач на сегодня в очередь
        mQueuedSub = QueuedDAO.getDAO().addAllTodayTasks().subscribe(longs -> {
            mQueuedSub.unsubscribe();
            mQueuedSub = null;
        });

        //Инициализация JobCreator'a
        JobManager.create(this).addJobCreator(new TasksJobCreator());

        //создание напоминаний для всех задач на сегодня
        RemindersManager.scheduleTodayReminders(getApplicationContext());

        //Запуск службы уведомления таймера
        Intent serviceIntent = new Intent(this, TimerNotificationService.class);
        serviceIntent.setAction(ACTION_SHOW_NOTIFICATION);
        startService(serviceIntent);
    }



}

