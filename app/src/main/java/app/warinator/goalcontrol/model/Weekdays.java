package app.warinator.goalcontrol.model;

import java.util.ArrayList;
import java.util.Calendar;

import app.warinator.goalcontrol.utils.Util;

/**
 * Created by Warinator on 29.03.2017.
 */

public class Weekdays {
    public int getBitMask() {
        return mBitMask;
    }

    public void setBitMask(int bitMask) {
        mBitMask = bitMask;
    }

    private int mBitMask;

    public enum Day {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY}

    public void setDay(Day day, boolean val) {
        mBitMask = Util.setBit(mBitMask, day.ordinal(), val);
    }

    public boolean getDay(Day day) {
        return Util.getBit(mBitMask, day.ordinal());
    }

    public Day weekdayFromCalendar(Calendar calendar){
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        Day d;
        switch (weekday){
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

    public void setDay(int day, boolean val){
        Day d;
        switch (day){
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



    public Weekdays(int bitMask){
        mBitMask = bitMask;
    }

    public ArrayList<Day> getCheckedDays(){
        ArrayList<Day> days = new ArrayList<>();
        for (Day d : Day.values()){
            if (getDay(d)){
                days.add(d);
            }
        }
        return days;
    }

}
