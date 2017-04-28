package app.warinator.goalcontrol.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Warinator on 26.04.2017.
 */

public class TimerBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_NEXT = "nextInterval";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case ACTION_START:
                Log.v("YABBA", "Start action received!");
                TimerManager.getInstance(context).startOrPauseTimer();
                break;
            case ACTION_STOP:
                Log.v("YABBA", "Stop action received!");
                TimerManager.getInstance(context).stopTimer();
                break;
            case ACTION_NEXT:
                Log.v("YABBA", "Next action received!");
                TimerManager.getInstance(context).nextInterval();
                break;
        }
    }
}
