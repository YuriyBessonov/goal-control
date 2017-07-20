package app.warinator.goalcontrol.model;

import java.util.ArrayList;
import java.util.Calendar;

import app.warinator.goalcontrol.utils.Util;

/**
 * Помечаемые дни недели
 */
public class Weekdays {
    private int mBitMask;

    public Weekdays(int bitMask) {
        mBitMask = bitMask;
    }

    public int getBitMask() {
        return mBitMask;
    }

    public void setBitMask(int bitMask) {
        mBitMask = bitMask;
    }

    public void setDay(Day day, boolean val) {
        mBitMask = Util.setBit(mBitMask, day.ordinal(), val);
    }

    public boolean getDay(Day day) {
        return Util.getBit(mBitMask, day.ordinal());
    }

    //Получить день недели указанной даты
    public Day weekdayFromCalendar(Calendar calendar) {
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        Day d;
        switch (weekday) {
            case Calendar.TUESDAY:
                d = Day.TUESDAY;
                break;
            case Calendar.WEDNESDAY:
                d = Day.WEDNESDAY;
                break;
            case Calendar.THURSDAY:
                d = Day.THURSDAY;
                break;
            case Calendar.FRIDAY:
                d = Day.FRIDAY;
                break;
            case Calendar.SATURDAY:
                d = Day.SATURDAY;
                break;
            case Calendar.SUNDAY:
                d = Day.SUNDAY;
                break;
            default:
                d = Day.MONDAY;
                break;
        }
        return d;
    }

    //Установить или убрать отметку с дня недели
    public void setDay(int day, boolean val) {
        Day d;
        switch (day) {
            case Calendar.TUESDAY:
                d = Day.TUESDAY;
                break;
            case Calendar.WEDNESDAY:
                d = Day.WEDNESDAY;
                break;
            case Calendar.THURSDAY:
                d = Day.THURSDAY;
                break;
            case Calendar.FRIDAY:
                d = Day.FRIDAY;
                break;
            case Calendar.SATURDAY:
                d = Day.SATURDAY;
                break;
            case Calendar.SUNDAY:
                d = Day.SUNDAY;
                break;
            default:
                d = Day.MONDAY;
                break;
        }
        setDay(d, val);
    }

    //Получить список отмеченных дней
    public ArrayList<Day> getCheckedDays() {
        ArrayList<Day> days = new ArrayList<>();
        for (Day d : Day.values()) {
            if (getDay(d)) {
                days.add(d);
            }
        }
        return days;
    }

    //Дни недели
    public enum Day {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY}

}
