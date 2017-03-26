package app.warinator.goalcontrol.util;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import app.warinator.goalcontrol.R;

/**
 * Created by Warinator on 24.03.2017.
 */

public class Util {
    private Util(){}
    public static String getFormattedDate(Calendar date, Context context){
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        String dateStr = formatter.format(date.getTime());
        Calendar today = Calendar.getInstance();
        if (today.get(Calendar.YEAR) == date.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)){
            dateStr += String.format(" (%s)", context.getString(R.string.today));
        }
        return dateStr;
    }

    public static String getFormattedTime(Calendar date){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String timeStr = formatter.format(date.getTime());
        return timeStr;
    }

}
