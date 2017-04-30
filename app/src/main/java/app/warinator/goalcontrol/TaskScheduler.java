package app.warinator.goalcontrol;

import java.util.ArrayList;
import java.util.Calendar;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.model.main.Weekdays;
import app.warinator.goalcontrol.utils.Util;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Warinator on 14.04.2017.
 */

public class TaskScheduler {
    private static Subscription tasksAddSub;
    private static Subscription todayTasksAddSub;
    private static CompositeSubscription getTaskSub;

    public static void createConcreteTasks(Task task){
        ConcreteTask ct = new ConcreteTask();
        ct.setTask(task);
        if (task.getBeginDate() == null){//дата не задана
            ConcreteTaskDAO.getDAO().add(ct).subscribe();
            return;
        }


        ArrayList<ConcreteTask> concreteTasks = new ArrayList<>();
        int count = task.getRepeatCount();
        Calendar date = task.getBeginDate();

        if (!task.isRepeatable()){//однократно
            ct.setDateTime(task.getBeginDate());
            concreteTasks.add(ct);
        }
        //повторяющаяся
        else if (task.isInterval()){//через несколько дней
            int interval = task.getIntervalValue();
            for (int i=0; i<count; i++){
                Calendar concreteDate = Calendar.getInstance();
                concreteDate.setTimeInMillis(date.getTimeInMillis());
                concreteTasks.add(new ConcreteTask(0, task, concreteDate,  0, 0, 0, false));
                date.add(Calendar.DATE, interval);
            }
        }
        else {//по дням недели
            Weekdays wd = task.getWeekdays();
            for (int i=0; i<count; i++){
                for (int j=0; j<7; j++){
                    if (wd.getDay(wd.weekdayFromCalendar(date))){
                        Calendar concreteDate = Calendar.getInstance();
                        concreteDate.setTimeInMillis(date.getTimeInMillis());
                        concreteTasks.add(new ConcreteTask(0, task, concreteDate,  0, 0, 0, false));
                    }
                    date.add(Calendar.DATE, 1);
                }
            }
        }
        
        ArrayList<Long> taskIds = new ArrayList<>();
        taskIds.ensureCapacity(concreteTasks.size());

        tasksAddSub = ConcreteTaskDAO.getDAO().add(concreteTasks).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                tasksAddSub.unsubscribe();
                todayTasksAddSub = QueuedDAO.getDAO().addAllTodayTasks()
                        .subscribe(longs -> todayTasksAddSub.unsubscribe());
                if (task.isWithTime()){
                    for (int i=0; i < taskIds.size(); i++){
                        ConcreteTask t = concreteTasks.get(i);
                        Calendar today = Util.justDate(Calendar.getInstance());
                        if (Util.compareDays(today, t.getDateTime()) == 0){
                            long id = taskIds.get(i);
                            t.setId(id);
                            RemindersManager.scheduleReminder(t);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Long aLong) {
                taskIds.add(aLong);
            }
        });

    }
}
