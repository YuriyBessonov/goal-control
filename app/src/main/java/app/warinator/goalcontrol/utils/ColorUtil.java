package app.warinator.goalcontrol.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import app.warinator.goalcontrol.R;

/**
 * Утилитный класс для работы с цветами
 */
public class ColorUtil {
    public static final int COLOR_DEFAULT = 8766462;

    private ColorUtil() {
    }

    //Получить цвет категории по индексу в массиве палитры цветов
    public static int getCategoryColor(int colorInd, Context context) {
        if (colorInd == COLOR_DEFAULT) {
            return ContextCompat.getColor(context, R.color.colorGreyLight);
        } else {
            return context.getResources().getIntArray(R.array.palette_categories)[colorInd];
        }
    }

    //Получить цвет проекта по индексу в массиве палитры цветов
    public static int getProjectColor(int colorInd, Context context) {
        if (colorInd == COLOR_DEFAULT) {
            return ContextCompat.getColor(context, R.color.colorGreyVeryLight);
        } else {
            return context.getResources().getIntArray(R.array.palette_projects)[colorInd];
        }
    }
}
