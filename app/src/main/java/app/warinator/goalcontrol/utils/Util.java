package app.warinator.goalcontrol.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.main.Weekdays;
import butterknife.ButterKnife;

/**
 * Created by Warinator on 24.03.2017.
 */

public class Util {
    private Util(){}
    public static String getFormattedDate(Calendar date, Context context){
        if (date != null){
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            String dateStr = formatter.format(date.getTime());
            Calendar today = Calendar.getInstance();
            if (today.get(Calendar.YEAR) == date.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)){
                dateStr += String.format(" (%s)", context.getString(R.string.today));
            }
            return dateStr;
        }
        return null;
    }

    public static String getFormattedTime(Calendar date){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(date.getTime());
    }

    public static String getFormattedTime(long timeIntervalMillis){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(timeIntervalMillis);
    }

    public static String getFormattedTimeSeconds(long timeIntervalMillis){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(timeIntervalMillis);
    }

    public static final int TIME_MINUTE = 60*1000;
    public static final int TIME_HOUR = 60*60*1000;

    public static String getFormattedTimeWithUnits(long timeInterval,Context context){
        String formatStr;
        if (timeInterval >= TIME_HOUR ){
            if (timeInterval % TIME_HOUR == 0){
                formatStr = String.format("H %s", context.getString(R.string.hours_short));
            }
            else {
                formatStr = String.format("H %s m %s",
                        context.getString(R.string.hours_short),
                        context.getString(R.string.minutes_short));
            }

        }
        else {
            formatStr = String.format("m %s", context.getString(R.string.minutes_short));
        }
        SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(timeInterval);
    }

    public static boolean editTextIsEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;
        return true;
    }

    public static void showConfirmationDialog(String doWhat, Context context, DialogInterface.OnClickListener onClick){
        new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.do_you_really_want_to)+doWhat+"?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, onClick)
                .setNegativeButton(android.R.string.no, null).show();
    }

    public static void hideKeyboard(Context context, View view){
        InputMethodManager inputManager = (InputMethodManager) context.
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    public static final ButterKnife.Setter<View, Integer> VISIBILITY = new ButterKnife.Setter<View,Integer>() {
        @Override public void set(View view, Integer value, int index) {
            view.setVisibility(value);
        }
    };

    public static int intBool(boolean b){
        return b ? 1 : 0;
    }

    public static Calendar calendarFromMillis(long millis){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c;
    }

    public static int compareDays(Calendar d1, Calendar d2){
        return Util.justDate(d1).compareTo(Util.justDate(d2));
    }

    public static Calendar justDate(Calendar dateTime){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.getTime();
        cal.set(dateTime.get(Calendar.YEAR), dateTime.get(Calendar.MONTH), dateTime.get(Calendar.DAY_OF_MONTH), 0, 0);
        cal.getTime();
        return cal;
    }

    public static Calendar justDate(long timeMillis){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.getTime();
        Calendar arg = Calendar.getInstance();
        arg.setTimeInMillis(timeMillis);
        arg.getTime();
        cal.set(arg.get(Calendar.YEAR), arg.get(Calendar.MONTH), arg.get(Calendar.DAY_OF_MONTH), 0, 0);
        cal.getTime();
        return cal;
    }

    public static boolean dayIsInThePast(Calendar date){
        Calendar today = Calendar.getInstance();
        if (date.get(Calendar.YEAR) < today.get(Calendar.YEAR) ||
                date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        date.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR)){
            return true;
        }
        return false;
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    public static int setBit(int bits, int bitPos, boolean bitVal){
        int val = bitVal ? 1 : 0;
        int mask = ~(1<<bitPos);
        bits = (bits & mask) | (val << bitPos);
        return bits;
    }

    public static boolean getBit(int bits, int bitPos){
        return ((bits >> bitPos) & 1) > 0;
    }

    public static String weekdaysStr(Weekdays weekdays, Context context){
        StringBuilder sb = new StringBuilder();
        String[] arr = context.getResources().getStringArray(R.array.weekdays_short);
        for (int i = 0; i < Weekdays.Day.values().length; i++){
            if (weekdays.getDay(Weekdays.Day.values()[i])){
                sb.append(arr[i]);
                sb.append(", ");
            }
        }
        return sb.substring(0, sb.length()-2);
    }

    public static String getMonthYear(Calendar date){
        if (date != null){
            SimpleDateFormat formatter = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            return formatter.format(date.getTime());
        }
        return null;
    }

    public static String makeShortName(String fullName){
        StringBuilder sb = new StringBuilder(fullName);
        if (sb.length() > 3){
            sb.delete(3, sb.length());
        }
        sb.append(".");
        return sb.toString();
    }

    public static int fracToPercent(double frac){
        return (int)(frac*100.0);
    }


}
