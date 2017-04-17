package app.warinator.goalcontrol;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.QueueDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
import rx.Subscription;

/**
 * Created by Warinator on 17.04.2017.
 */

public class TasksProvider {
    private enum Date { QUEUE, WEEK, DATE, NO_DATE }
    public enum Criterion { TASK_NAME, PROJECT_NAME, CATEGORY_NAME,
        PRIORITY, DATE, PROGRESS_REAL, PROGRESS_EXP, PROGRESS_NEED};
    public enum Order {ASC, DESC};

    private Date mDateFilter;
    private Calendar mDate;
    private int[] mSortCriterion = new int[Criterion.values().length];
    private List<ConcreteTask> mConcreteTasks = new ArrayList<>();
    private Subscription mSub;
    private OnTasksUpdatedListener mListener;


    public TasksProvider(){
        mDateFilter = Date.QUEUE;
    }

    private Observable<List<ConcreteTask>> getObservable(){
        Observable<List<ConcreteTask>> obs = null;
        Calendar cal = Calendar.getInstance();
        switch (mDateFilter){
            case QUEUE:
                obs = QueueDAO.getDAO().getAllQueued(true);
                break;
            case WEEK:
                Calendar today = Util.justDate(Calendar.getInstance());
                cal.setTimeInMillis(today.getTimeInMillis());
                cal.add(Calendar.DATE, 7);
                obs = ConcreteTaskDAO.getDAO().getAllForDateRange(today, cal);
                break;
            case DATE:
                cal.setTimeInMillis(mDate.getTimeInMillis());
                cal.add(Calendar.DATE, 1);
                obs = ConcreteTaskDAO.getDAO().getAllForDateRange(mDate, cal);
                break;
            case NO_DATE:
                obs = ConcreteTaskDAO.getDAO().getAllWithNoDate();
                break;
        }
        return obs;
    }

    public TasksProvider resetSortCriterion(Criterion c){
        mSortCriterion[c.ordinal()] = 0;
        return this;
    }
    public TasksProvider setSortCriterion(Criterion c, int sortPriority, Order o){
        if (o == Order.DESC){
            sortPriority *= -1;
        }
        mSortCriterion[c.ordinal()] = sortPriority;
        return this;
    }

    public TasksProvider tasksQueued(){
        mDateFilter = Date.QUEUE;
        return this;
    }

    public TasksProvider tasksToday(){
        return tasksForDate(Calendar.getInstance());
    }

    public TasksProvider tasksForWeek(){
        mDateFilter = Date.WEEK;
        return this;
    }

    public TasksProvider tasksForDate(Calendar date){
        mDateFilter = Date.DATE;
        mDate = Util.justDate(date);
        return this;
    }

    public TasksProvider tasksWithNoDate(){
        mDateFilter = Date.NO_DATE;
        return this;
    }

    public interface OnTasksUpdatedListener{
        void onTasksUpdated(List<ConcreteTask> cTasks);
    }

    private void receiveTasks(){
        if (mSub != null && !mSub.isUnsubscribed()){
            mSub.unsubscribe();
        }
        mSub = getObservable().subscribe(tasks -> {
            mConcreteTasks = tasks;
            mListener.onTasksUpdated(tasks);
        });
    }

    public void subscribe(OnTasksUpdatedListener listener){
        mListener = listener;
        receiveTasks();
    }

    public void unsibscribe(){
        mListener = null;
        if (mSub != null && !mSub.isUnsubscribed()){
            mSub.unsubscribe();
        }
    }

}
