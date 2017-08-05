package app.warinator.goalcontrol.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.Weekdays;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Утилитный класс общего назначения
 */
public class Util {
    private Util() {
    }

    //Получить строковое представление даты
    public static String getFormattedDate(Calendar date, Context context, boolean verbose) {
        if (date != null) {
            String format;
            if (verbose){
                format = "dd.MM.yyyy";
            }
            else if (dayIsToday(date)){
                format = "E, d MMM";
            }
            else {
                format = "E, d MMMM";
            }
            SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
            String dateStr = formatter.format(date.getTime());
            if (dayIsToday(date)){
                dateStr += String.format(" (%s)", context.getString(R.string.today));
            }
            return dateStr;
        }
        return null;
    }

    //Получить строковое представление времени
    public static String getFormattedTime(Calendar date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(date.getTime());
    }

    //Получить строковое представление времени
    public static String getFormattedTime(long timeIntervalMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(timeIntervalMillis);
    }

    //Получить строковое представление интервала времени с указанием единиц
    public static String getFormattedTimeWithUnits(long timeInterval, Context context) {
        String formatStr;
        if (timeInterval >= TimeUnit.HOURS.toMillis(1)) {
            if (timeInterval % TimeUnit.HOURS.toMillis(1) == 0) {
                formatStr = String.format("H %s", context.getString(R.string.hours_short));
            } else {
                formatStr = String.format("H %s m %s",
                        context.getString(R.string.hours_short),
                        context.getString(R.string.minutes_short));
            }

        } else {
            formatStr = String.format("m %s", context.getString(R.string.minutes_short));
        }
        SimpleDateFormat formatter = new SimpleDateFormat(formatStr, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(timeInterval);
    }

    //Получить строковое представление интервала времени с указанием единиц
    public static String getFormattedTimeAmt(long timeAmtMillis, Context context) {
        long hours = timeAmtMillis / TimeUnit.HOURS.toMillis(1);
        timeAmtMillis -= hours * TimeUnit.HOURS.toMillis(1);
        long minutes = timeAmtMillis / TimeUnit.MINUTES.toMillis(1);
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(String.format(Locale.getDefault(), "%d %s",
                    hours, context.getString(R.string.hours_short)));
        }
        if (minutes > 0 || hours == 0) {
            sb.append(String.format(Locale.getDefault(), " %d %s",
                    minutes, context.getString(R.string.minutes_short)));
        }
        return sb.toString();
    }

    //Проверить, пусто ли текущее введенное значение в EditText
    public static boolean editTextIsEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;
        return true;
    }

    //Абсолютная разница между датами в днях
    public static int daysDifference(Calendar d1, Calendar d2) {
        return (int) Math.floor((d2.getTimeInMillis() -
                d1.getTimeInMillis()) / TimeUnit.DAYS.toMillis(1));
    }

    //Отобразить диалог подтверждения действия с указанием обработчика
    public static void showConfirmationDialog(String doWhat, Context context,
                                              DialogInterface.OnClickListener onClick) {
        new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.do_you_really_want_to) + doWhat +
                        context.getString(R.string.question_mark))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, onClick)
                .setNegativeButton(android.R.string.no, null).show();
    }

    //Скрыть клавиатуру
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputManager = (InputMethodManager) context.
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //Получить экземпляр Calendar с заданным временем
    public static Calendar calendarFromMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c;
    }

    //Сравнить экземпляры Calendar
    public static int compareDays(Calendar d1, Calendar d2) {
        return Util.justDate(d1).compareTo(Util.justDate(d2));
    }

    //Получить дату без информации о времени
    public static Calendar justDate(Calendar dateTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.getTime();
        if (dateTime != null) {
            cal.set(dateTime.get(Calendar.YEAR), dateTime.get(Calendar.MONTH),
                    dateTime.get(Calendar.DAY_OF_MONTH), 0, 0);
            cal.getTime();
        }
        return cal;
    }

    //Получить дату без информации о времени
    public static Calendar justDate(long timeMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.getTime();
        Calendar arg = Calendar.getInstance();
        arg.setTimeInMillis(timeMillis);
        arg.getTime();
        cal.set(arg.get(Calendar.YEAR), arg.get(Calendar.MONTH),
                arg.get(Calendar.DAY_OF_MONTH), 0, 0);
        cal.getTime();
        return cal;
    }

    //Прошла ли указанная дата
    public static boolean dayIsInThePast(Calendar date) {
        Calendar today = Calendar.getInstance();
        if (date.get(Calendar.YEAR) < today.get(Calendar.YEAR) ||
                date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        date.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR)) {
            return true;
        }
        return false;
    }

    //Дата сегодняшняя
    public static boolean dayIsToday(Calendar date){
        if (date == null){
            return false;
        }
        Calendar today = Calendar.getInstance();
        return compareDays(today, date) == 0;
    }

    //Задать значение бита в битовой маске
    public static int setBit(int bits, int bitPos, boolean bitVal) {
        int val = bitVal ? 1 : 0;
        int mask = ~(1 << bitPos);
        bits = (bits & mask) | (val << bitPos);
        return bits;
    }

    //Получить значение бита в битовой маске
    public static boolean getBit(int bits, int bitPos) {
        return ((bits >> bitPos) & 1) > 0;
    }

    //Получить строку со списком отмеченных дней недели
    public static String weekdaysStr(Weekdays weekdays, Context context) {
        StringBuilder sb = new StringBuilder();
        String[] arr = context.getResources().getStringArray(R.array.weekdays_short);
        for (int i = 0; i < Weekdays.Day.values().length; i++) {
            if (weekdays.getDay(Weekdays.Day.values()[i])) {
                sb.append(arr[i]);
                sb.append(", ");
            }
        }
        return sb.substring(0, sb.length() - 2);
    }

    //Укоротить строку до 3 или менее символов, добавив в конец точку
    public static String makeShortName(String fullName) {
        StringBuilder sb = new StringBuilder(fullName);
        if (sb.length() > 3) {
            sb.delete(3, sb.length());
        }
        sb.append(".");
        return sb.toString();
    }

    //Представить дробь как значение в процентах
    public static int fracToPercent(double frac) {
        return (int) (frac * 100.0);
    }

    public static void disableTitle(Dialog dialog){
        if (dialog.getWindow() != null){
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
    }

    //Таймер, срабатывающий через заданное число секунд
    public static Observable<Long> timer(int milliSeconds){
        return Observable.timer(milliSeconds, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

}
