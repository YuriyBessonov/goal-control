package app.warinator.goalcontrol.model.main;

import app.warinator.goalcontrol.util.Util;

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

    public Weekdays(int bitMask){
        mBitMask = bitMask;
    }


}
