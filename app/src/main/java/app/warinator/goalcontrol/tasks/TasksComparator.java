package app.warinator.goalcontrol.tasks;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.utils.Util;

/**
 * Компаратор назначенных задач
 */
public class TasksComparator implements Comparator<ConcreteTask> {
    private ArrayList<SortCriterion> mCriteria;

    public TasksComparator(ArrayList<SortCriterion> criteria) {
        mCriteria = criteria;
    }

    public ArrayList<SortCriterion> getCriteria() {
        return mCriteria;
    }

    public void setCriteria(ArrayList<SortCriterion> criteria) {
        mCriteria = criteria;
    }

    @Override
    public int compare(ConcreteTask concreteTask1, ConcreteTask concreteTask2) {
        for (SortCriterion cr : mCriteria) {
            ConcreteTask t1, t2;
            String name1 = "", name2 = "";
            if (cr.order == SortCriterion.Order.ASC) {
                t1 = concreteTask1;
                t2 = concreteTask2;
            } else {
                t1 = concreteTask2;
                t2 = concreteTask1;
            }
            int pReal1 = t1.getProgressReal();
            int pExp1 = t1.getProgressExp();
            int pReal2 = t2.getProgressReal();
            int pExp2 = t2.getProgressExp();
            switch (cr.key) {
                case DATE:
                    Calendar date1 = Util.justDate(t1.getDateTime());
                    Calendar date2 = Util.justDate(t2.getDateTime());
                    if (date1.compareTo(date2) != 0) {
                        return date1.compareTo(date2);
                    }
                    break;
                case PRIORITY:
                    int prio1 = t1.getTask().getPriority().ordinal();
                    int prio2 = t2.getTask().getPriority().ordinal();
                    if (prio1 != prio2) {
                        return (prio1 < prio2) ? -1 : 1;
                    }
                    break;
                case PROGRESS_LACK:
                    int d1 = pExp1 - pReal1;
                    int d2 = pExp2 - pReal2;
                    if (d1 != d2) {
                        return (d1 < d2) ? -1 : 1;
                    }
                    break;
                case PROGRESS_EXP:
                    if (pExp1 != pExp2) {
                        return (pExp1 < pExp2) ? -1 : 1;
                    }
                    break;
                case PROGRESS_REAL:
                    if (pReal1 != pReal2) {
                        return (pReal1 < pReal2) ? -1 : 1;
                    }
                    break;
                case TASK_NAME:
                    name1 = t1.getTask().getName();
                    name2 = t2.getTask().getName();
                    if (name1.compareTo(name2) != 0) {
                        return name1.compareTo(name2);
                    }
                    break;
                case PROJECT_NAME:
                    if (t1.getTask().getProject() != null) {
                        name1 = t1.getTask().getProject().getName();
                    }
                    if (t2.getTask().getProject() != null) {
                        name2 = t2.getTask().getProject().getName();
                    }
                    if (name1.compareTo(name2) != 0) {
                        return name1.compareTo(name2);
                    }
                    break;
                case CATEGORY_NAME:
                    if (t1.getTask().getCategory() != null) {
                        name1 = t1.getTask().getCategory().getName();
                    }
                    if (t2.getTask().getCategory() != null) {
                        name2 = t2.getTask().getCategory().getName();
                    }
                    if (name1.compareTo(name2) != 0) {
                        return name1.compareTo(name2);
                    }
                    break;
            }
        }
        return 0;
    }

    public static class SortCriterion implements Parcelable {
        public static final Creator<SortCriterion> CREATOR = new Creator<SortCriterion>() {
            @Override
            public SortCriterion createFromParcel(Parcel in) {
                return new SortCriterion(in);
            }

            @Override
            public SortCriterion[] newArray(int size) {
                return new SortCriterion[size];
            }
        };

        ;
        public Key key;
        public Order order;
        public SortCriterion() {
            order = Order.ASC;
        }

        public SortCriterion(Key key, Order order) {
            this.key = key;
            this.order = order;
        }

        public SortCriterion(Parcel in) {
            key = Key.values()[in.readInt()];
            order = Order.values()[in.readInt()];
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(key.ordinal());
            dest.writeInt(order.ordinal());
        }

        //Критерии сортировки
        public enum Key {
            DATE, PRIORITY, PROGRESS_LACK, PROGRESS_EXP,
            PROGRESS_REAL, TASK_NAME, PROJECT_NAME, CATEGORY_NAME
        }

        //Порядок сортировки
        public enum Order {ASC, DESC}

    }
}
