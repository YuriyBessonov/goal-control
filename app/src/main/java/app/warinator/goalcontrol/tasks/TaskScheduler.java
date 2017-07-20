package app.warinator.goalcontrol.tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.job.RemindersManager;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.model.Weekdays;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
import rx.Subscription;

/**
 * Класс для создания экземпляров назначенных задач
 */
public class TaskScheduler {
    private static Subscription tasksAddSub;
    private static Calendar mBeginDate;

    //Запланировать выполнение новой задачи
    public static void createConcreteTasks(Task task) {
        createConcreteTasks(task, UpdateMethod.LEFT_ALL);
    }

    //Запланировать выполнение задачи с указанным режимом обновления назначенных ранее
    public static void createConcreteTasks(Task task, UpdateMethod updMethod) {
        ConcreteTask ct = new ConcreteTask();
        ct.setTask(task);

        if (task.getBeginDate() == null) {//дата не задана
            ConcreteTaskDAO.getDAO().add(ct).subscribe();
            return;
        } else {
            mBeginDate = Util.justDate(task.getBeginDate());
        }

        ArrayList<ConcreteTask> concreteTasks = new ArrayList<>();
        int count = task.getRepeatCount();
        Calendar date = task.getBeginDate();

        if (!task.isRepeatable()) {//однократно
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(task.getBeginDate().getTimeInMillis());
            ct.setDateTime(cal);
            ct.setQueuePos(-1);
            concreteTasks.add(ct);
        }
        //повторяющаяся
        else if (task.isInterval()) {//через несколько дней
            int interval = task.getIntervalValue();
            for (int i = 0; i < count; i++) {
                Calendar concreteDate = Calendar.getInstance();
                concreteDate.setTimeInMillis(date.getTimeInMillis());
                concreteTasks.add(new ConcreteTask(0, task, concreteDate, 0, 0, -1, false));
                date.add(Calendar.DATE, interval);
            }
        } else {//по дням недели
            Weekdays wd = task.getWeekdays();
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < 7; j++) {
                    if (wd.getDay(wd.weekdayFromCalendar(date))) {
                        Calendar concreteDate = Calendar.getInstance();
                        concreteDate.setTimeInMillis(date.getTimeInMillis());
                        concreteTasks.add(new ConcreteTask(0, task, concreteDate, 0, 0, -1, false));
                    }
                    date.add(Calendar.DATE, 1);
                }
            }
        }

        Observable<Integer> obs = Observable.just(0);

        if (task.getBeginDate() != null) {
            if (updMethod == UpdateMethod.REMOVE_ALL) {
                obs = ConcreteTaskDAO.getDAO().deleteAllStartingFrom(task.getId(), mBeginDate);
            } else if (updMethod == UpdateMethod.REMOVE_CONFLICTS) {
                List<Calendar> dates = new ArrayList<>();
                for (ConcreteTask t : concreteTasks) {
                    dates.add(t.getDateTime());
                }
                obs = ConcreteTaskDAO.getDAO().deleteIfDateInList(task.getId(), dates);
            }
        }

        tasksAddSub = obs.concatMap(integer -> ConcreteTaskDAO.getDAO()
                .add(concreteTasks)).subscribe(taskIds -> {
            tasksAddSub.unsubscribe();
            if (task.isWithTime()) {
                for (int i = 0; i < taskIds.size(); i++) {
                    ConcreteTask t = concreteTasks.get(i);
                    Calendar today = Util.justDate(Calendar.getInstance());
                    if (Util.compareDays(today, t.getDateTime()) == 0) {
                        long id = taskIds.get(i);
                        t.setId(id);
                        RemindersManager.scheduleReminder(t);
                    }
                }
            }
        });

    }

    //Действия с назначенными ранеее задачами при обновлении параметров назначени
    public enum UpdateMethod {
        LEFT_ALL, //ничего не делать
        REMOVE_ALL, //удалить все назначенные ранее
        REMOVE_CONFLICTS //удалять только при совпадении даты с новыми
    }
}
