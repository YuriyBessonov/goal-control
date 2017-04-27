package app.warinator.goalcontrol.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Warinator on 26.04.2017.
 */

public class TimerNotificationHelperActivity extends AppCompatActivity{
    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_NEXT = "next";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        switch (action){
            case ACTION_START:
                Log.v("BARABO","START");
                break;
        }
        finish();
    }
}
