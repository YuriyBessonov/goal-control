package app.warinator.goalcontrol.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import rx.Subscription;

public class SplashActivity extends AppCompatActivity {

    private Subscription mSub;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        /*mSub = Util.timer(1000).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                mSub.unsubscribe();
                mSub = null;
                finish();
            }
        });*/
        finish();
    }
}
