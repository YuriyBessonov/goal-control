package app.warinator.goalcontrol.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import app.warinator.goalcontrol.R;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.functions.Action1;

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
        return formatter.format(date.getTime());
    }

    public static String getFormattedTime(long timeInterval){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(timeInterval);
    }

    public static String getFormattedTimeWithUnits(long timeInterval,Context context){
        String formatStr;
        if (timeInterval > 59*60*1000){
            formatStr = String.format("H %s m %s",
                    context.getString(R.string.hours_short),
                    context.getString(R.string.minutes_short));
        }
        else {
            formatStr = String.format("m %s",
                    context.getString(R.string.minutes_short));
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

}
