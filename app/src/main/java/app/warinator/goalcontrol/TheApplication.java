package app.warinator.goalcontrol;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Warinator on 31.03.2017.
 */

public class TheApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
