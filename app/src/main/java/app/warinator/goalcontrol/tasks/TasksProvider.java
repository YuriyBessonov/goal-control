package app.warinator.goalcontrol.tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.utils.Util;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Класс, предоставлябщий доступ к списку назначенных задач
 */
public class TasksProvider {
    private QueryMode mQueryMode;
    //Целевая дата, если режим - DATE
    private Calendar mDate;
    //Задачи
    private List<ConcreteTask> mConcreteTasks = new ArrayList<>();
    private Subscription mSub;
    private OnTasksUpdatedListener mListener;
    private TasksComparator mComparator;
    private TasksFilter mFilter;
    private boolean mFilterChanged;
    private boolean mCriteriaChanged;

    public TasksProvider() {
        mQueryMode = QueryMode.QUEUE;
        ArrayList<TasksComparator.SortCriterion> sortCriteria = new ArrayList<>();
        for (TasksComparator.SortCriterion.Key key : TasksComparator.SortCriterion.Key.values()) {
            TasksComparator.SortCriterion cr = new TasksComparator.SortCriterion();
            cr.key = key;
            if (key == TasksComparator.SortCriterion.Key.PROGRESS_LACK ||
                    key == TasksComparator.SortCriterion.Key.PRIORITY ||
                    key == TasksComparator.SortCriterion.Key.PROGRESS_EXP) {
                cr.order = TasksComparator.SortCriterion.Order.DESC;
            } else {
                cr.order = TasksComparator.SortCriterion.Order.ASC;
            }
            sortCriteria.add(cr);
        }
        mComparator = new TasksComparator(sortCriteria);
        mFilter = new TasksFilter();
    }

    public Calendar getDate() {
        return mDate;
    }

    public void setDate(Calendar date) {
        mDate = date;
    }

    private Observable<List<ConcreteTask>> getObservable() {
        Observable<List<ConcreteTask>> obs = null;
        Calendar cal = Calendar.getInstance();
        switch (mQueryMode) {
            case QUEUE:
                obs = ConcreteTaskDAO.getDAO().getAllQueued(true);
                break;
            case WEEK:
                Calendar today = Util.justDate(Calendar.getInstance());
                cal.setTimeInMillis(today.getTimeInMillis());
                cal.add(Calendar.DATE, 7);
                obs = ConcreteTaskDAO.getDAO().getAllForDateRange(today, cal, true);
                break;
            case DATE:
                cal.setTimeInMillis(mDate.getTimeInMillis());
                cal.add(Calendar.DATE, 1);
                obs = ConcreteTaskDAO.getDAO().getAllForDateRange(mDate, cal, true);
                break;
            case NO_DATE:
                obs = ConcreteTaskDAO.getDAO().getAllWithNoDate();
                break;
            case ALL:
                obs = ConcreteTaskDAO.getDAO().getAll(true, false);
                break;
        }
        return obs;
    }

    //Установка режима запрашиваемых задач
    public TasksProvider tasksQueued() {
        mQueryMode = QueryMode.QUEUE;
        return this;
    }

    public TasksProvider tasksToday() {
        return tasksForDate(Calendar.getInstance());
    }

    public TasksProvider tasksForWeek() {
        mQueryMode = QueryMode.WEEK;
        return this;
    }

    public TasksProvider tasksForDate(Calendar date) {
        mQueryMode = QueryMode.DATE;
        mDate = Util.justDate(date);
        return this;
    }

    public TasksProvider tasksWithNoDate() {
        mQueryMode = QueryMode.NO_DATE;
        return this;
    }

    public TasksProvider tasksAll() {
        mQueryMode = QueryMode.ALL;
        return this;
    }

    //Подписаться на обновление списка задач,
    //предоставляя обновленный список своему подписчику
    private void observeTasks() {
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
        mSub = getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(tasks -> {
                    List<ConcreteTask> filtered;
                    if (mFilterChanged || mQueryMode != QueryMode.QUEUE) {
                        filtered = new ArrayList<>();
                        ((ArrayList) filtered).ensureCapacity(filtered.size());
                        for (ConcreteTask ct : tasks) {
                            if (mFilter.matches(ct)) {
                                filtered.add(ct);
                            }
                        }
                        mFilterChanged = false;
                    } else {
                        filtered = tasks;
                    }
                    if (mCriteriaChanged || mQueryMode != QueryMode.QUEUE) {
                        Collections.sort(filtered, mComparator);
                        mCriteriaChanged = false;
                    }
                    return filtered;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tasks -> {
                    mConcreteTasks = tasks;
                    mListener.onTasksUpdated(getTasks());
                });
    }

    //Задать подписчика на обновление задач
    public void subscribe(OnTasksUpdatedListener listener) {
        mListener = listener;
        observeTasks();
    }

    //Прекратить получать обновления списка задач
    public void unsibscribe() {
        mListener = null;
        if (mSub != null && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
    }

    public List<ConcreteTask> getTasks() {
        return mConcreteTasks;
    }

    public TasksFilter getFilter() {
        return mFilter;
    }

    public void setFilter(TasksFilter filter) {
        mFilter = filter;
        mFilterChanged = true;
    }

    public ArrayList<TasksComparator.SortCriterion> getSortCriteria() {
        return mComparator.getCriteria();
    }

    public void setSortCriteria(ArrayList<TasksComparator.SortCriterion> sortCriteria) {
        mComparator.setCriteria(sortCriteria);
        mCriteriaChanged = true;
    }

    //Режим запрашиваемых задач
    private enum QueryMode {
        QUEUE, WEEK, DATE, NO_DATE, ALL
    }

    public interface OnTasksUpdatedListener {
        void onTasksUpdated(List<ConcreteTask> cTasks);
    }

}
