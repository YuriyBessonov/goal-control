package app.warinator.goalcontrol;

import java.util.ArrayList;
import java.util.Calendar;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.model.main.Weekdays;

/**
 * Created by Warinator on 14.04.2017.
 */

public class TaskScheduler {
    public static void createConcreteTasks(Task task){
        ConcreteTask ct = new ConcreteTask();
        ct.setTask(task);
        if (task.getBeginDate() == null){//дата не задана
            ConcreteTaskDAO.getDAO().add(ct).subscribe();
            return;
        }
        if (!task.isRepeatable()){//однократно
            ct.setDateTime(task.getBeginDate());
            ConcreteTaskDAO.getDAO().add(ct).subscribe();
            return;
        }
        ArrayList<ConcreteTask> concreteTasks = new ArrayList<>();
        int count = task.getRepeatCount();
        Calendar date = task.getBeginDate();
        //повторяющаяся
        if (task.isInterval()){//через несколько дней
            int interval = task.getIntervalValue();
            for (int i=0; i<count; i++){
                Calendar concreteDate = Calendar.getInstance();
                concreteDate.setTimeInMillis(date.getTimeInMillis());
                concreteTasks.add(new ConcreteTask(0, task, concreteDate,  0, 0, 0));
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
                        concreteTasks.add(new ConcreteTask(0, task, concreteDate,  0, 0, 0));
                    }
                    date.add(Calendar.DATE, 1);
                }
            }
        }
        ConcreteTaskDAO.getDAO().add(concreteTasks).subscribe();
    }
}