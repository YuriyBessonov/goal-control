package app.warinator.goalcontrol;

import android.content.Context;
import android.util.AttributeSet;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Warinator on 11.04.2017.
 */

public class DelayAutocompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    public DelayAutocompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    CharSequence text;
    int keyCode;
    Subscription sub;
    private static final int DELAY_MILLIS = 400;

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (sub != null && !sub.isUnsubscribed()){
            sub.unsubscribe();
        }
        this.text = text;
        this.keyCode = keyCode;
        sub = rx.Observable.timer(DELAY_MILLIS, TimeUnit.MILLISECONDS, Schedulers.computation()).subscribe(performFilt);
    }

    private Action1<Long> performFilt = new Action1<Long>() {
        @Override
        public void call(Long aLong) {
            DelayAutocompleteTextView.super.performFiltering(text, keyCode);
        }
    };

}
