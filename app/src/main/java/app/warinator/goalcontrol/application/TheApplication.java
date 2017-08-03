package app.warinator.goalcontrol.application;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DbManager;
import app.warinator.goalcontrol.job.RemindersManager;
import app.warinator.goalcontrol.job.TasksDailyJob;
import app.warinator.goalcontrol.job.TasksJobCreator;
import app.warinator.goalcontrol.timer.TimerNotificationService;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static app.warinator.goalcontrol.timer.TimerNotificationService.ACTION_SHOW_NOTIFICATION;

/**
 * Класс приложения
 */
public class TheApplication extends Application {
    private Subscription mQueuedSub, mJobSub;

    @Override
    public void onCreate() {
        super.onCreate();
        initApp();
        initStetho();
    }

    private void initApp() {
        //обеспечение доступа к БД
        SQLiteDatabase db = DbManager.getInstance(getApplicationContext()).getDatabase().getReadableDatabase();

        //добавление задач на сегодня в очередь, которые не были добавлены ранее и удаление неактуальных
        mQueuedSub = ConcreteTaskDAO.getDAO().addAllNecessaryToQueue().subscribe(integer -> {
            mQueuedSub.unsubscribe();
            mQueuedSub = null;
        });

        //Инициализация JobCreator'a
        JobManager.create(this).addJobCreator(new TasksJobCreator());

        mJobSub = Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            //создание напоминаний для всех задач на сегодня
            RemindersManager.scheduleTodayReminders(getApplicationContext());
            //Планирование ежедневной работы
            TasksDailyJob.schedule();
        }).subscribeOn(Schedulers.io()).subscribe(integer -> {
            mJobSub.unsubscribe();
            mJobSub = null;
        }, Throwable::printStackTrace);


        //Запуск службы уведомления таймера
        Intent serviceIntent = new Intent(this, TimerNotificationService.class);
        serviceIntent.setAction(ACTION_SHOW_NOTIFICATION);
        startService(serviceIntent);
    }

    private void initStetho(){
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

