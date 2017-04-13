package app.warinator.goalcontrol.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import app.warinator.goalcontrol.R;

/**
 * Created by Warinator on 13.04.2017.
 */

public class ColorUtil {
    private ColorUtil(){}

    public static final int COLOR_DEFAULT = 8766462;

    public static int getCategoryColor(int colorInd, Context context){
        if (colorInd == COLOR_DEFAULT){
            return ContextCompat.getColor(context, R.color.colorGreyLight);
        }
        else {
            return context.getResources().getIntArray(R.array.palette_categories)[colorInd];
        }
    }


    public static int getProjectColor(int colorInd, Context context){
        if (colorInd == COLOR_DEFAULT){
            return ContextCompat.getColor(context, R.color.colorGreyVeryLight);
        }
        else {
            return context.getResources().getIntArray(R.array.palette_projects)[colorInd];
        }
    }
}
