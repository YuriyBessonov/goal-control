package app.warinator.goalcontrol;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DbManager;
import app.warinator.goalcontrol.job.TasksDailyJob;
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

        /*
        TaskDAO.getDAO().onUpgrade(db, 1, 1);
        ConcreteTaskDAO.getDAO().onUpgrade(db, 1, 1);
         ProjectDAO.getDAO().onUpgrade(db, 1, 1);
        TrackUnitDAO.getDAO().onUpgrade(db, 1, 1);
        CategoryDAO.getDAO().onUpgrade(db, 1, 1);
        CheckListItemDAO.getDAO().onUpgrade(db, 1, 1);

        QueuedDAO.getDAO().onUpgrade(db, 1, 1);

          db.execSQL("drop table "+ DbContract.QueuedCols._TAB_NAME);
        */





        /*mQueuedSub = QueuedDAO.getDAO().addAllTodayTasks().subscribe(longs -> {
            mQueuedSub.unsubscribe();
            mQueuedSub = null;
        });*/

        //добавление задач на сегодня в очередь, которые не были добавлены ранее и удаление неактуальных
        mQueuedSub = ConcreteTaskDAO.getDAO().addAllForTodayToQueue().subscribe(integer -> {
            mQueuedSub.unsubscribe();
            mQueuedSub = null;
        });


        //Инициализация JobCreator'a
        JobManager.create(this).addJobCreator(new TasksJobCreator());

        //создание напоминаний для всех задач на сегодня
        RemindersManager.scheduleTodayReminders(getApplicationContext());

        //Планирование ежедневной работы
        TasksDailyJob.schedule();

        //Запуск службы уведомления таймера
        Intent serviceIntent = new Intent(this, TimerNotificationService.class);
        serviceIntent.setAction(ACTION_SHOW_NOTIFICATION);
        startService(serviceIntent);
    }

}

