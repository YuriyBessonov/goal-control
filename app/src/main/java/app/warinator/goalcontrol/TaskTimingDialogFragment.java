package app.warinator.goalcontrol;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;

public class TaskTimingDialogFragment extends DialogFragment {
    private static final int MAX_INTERVAL =366;
    private static final int MAX_REPEAT =1000;
    private static final int MIN_PICKER_VAL = 1;
    private static final int DIALOG_INTERVAL = 1;
    private static final int DIALOG_REPEAT = 2;
    private static final int POS_ASGN_ONCE = 0;
    private static final int POS_INT_WEEKDAYS = 0;
    private static final String TAG_DIALOG_DATE = "dialog_date";
    private static final String TAG_DIALOG_TIME = "dialog_time";
    @BindView(R.id.tv_interval)
    TextView tvInterval;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_repeat)
    TextView tvRepeat;
    @BindView(R.id.tv_date_lbl)
    TextView tvDateLbl;
    @BindView(R.id.tv_repeat_lbl)
    TextView tvRepeatLbl;

    @BindView(R.id.la_interval)
    RelativeLayout laInterval;
    @BindView(R.id.la_date)
    RelativeLayout laDate;
    @BindView(R.id.la_time)
    RelativeLayout laTime;
    @BindView(R.id.la_repeat)
    RelativeLayout laRepeat;
    @BindView(R.id.la_regular_options)
    RelativeLayout laRegularOptions;
    @BindView(R.id.la_weekdays)
    LinearLayout laWeekdays;

    @BindView(R.id.rb_assign_once)
    RadioRealButton rbAssignOnce;
    @BindView(R.id.rb_repeat_weekdays)
    RadioRealButton rbRepeatWeekdays;
    @BindView(R.id.rbg_assign)
    RadioRealButtonGroup rbgAssign;
    @BindView(R.id.rbg_repeat)
    RadioRealButtonGroup rbgRepeat;

    private Calendar date;

    public TaskTimingDialogFragment() {}

    public static TaskTimingDialogFragment newInstance(){
        return new TaskTimingDialogFragment();
    }

    private MaterialNumberPicker mNumberPicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_timing_dialog, container, false);
        ButterKnife.bind(this, v);
        laInterval.setOnClickListener(onIntervalOptionClick);
        laDate.setOnClickListener(onDateOptionClick);
        laTime.setOnClickListener(onTimeOptionClick);
        laRepeat.setOnClickListener(onRepeatOptionClick);
        rbgAssign.setOnPositionChangedListener(new RadioRealButtonGroup.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(RadioRealButton button, int position) {
                initControls(position, rbgRepeat.getPosition());
            }
        });
        rbgRepeat.setOnPositionChangedListener(new RadioRealButtonGroup.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(RadioRealButton button, int position) {
                initControls(rbgAssign.getPosition(), position);
            }
        });
        rbgAssign.setPosition(0);
        rbgRepeat.setPosition(0);
        return v;
    }

    private void initControls(int assignPos, int repeatPos){
        if (assignPos == POS_ASGN_ONCE){
            laRegularOptions.setVisibility(View.GONE);
            tvDateLbl.setText(R.string.date);
        }
        else {
            laRegularOptions.setVisibility(View.VISIBLE);
            tvDateLbl.setText(R.string.begin_date);
            if (repeatPos == POS_INT_WEEKDAYS){
                laWeekdays.setVisibility(View.VISIBLE);
                laInterval.setVisibility(View.GONE);
                tvRepeatLbl.setText(R.string.repeat_weeks);
            }
            else {
                laWeekdays.setVisibility(View.GONE);
                laInterval.setVisibility(View.VISIBLE);
                tvRepeatLbl.setText(R.string.repeat_times);
            }
        }
    }

    private View.OnClickListener onIntervalOptionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog(DIALOG_INTERVAL).show();
        }
    };

    private View.OnClickListener onRepeatOptionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog(DIALOG_REPEAT).show();
        }
    };

    private View.OnClickListener onDateOptionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (date == null){
                date = Calendar.getInstance();
            }
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    onDateSetListener,
                    date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH),
                    date.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show(getActivity().getFragmentManager(), TAG_DIALOG_DATE);
        }
    };

    private View.OnClickListener onTimeOptionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (date == null){
                date = Calendar.getInstance();
            }
            TimePickerDialog tpd = TimePickerDialog.newInstance(onTimeSetListener,
                    date.get(Calendar.HOUR_OF_DAY),
                    date.get(Calendar.MINUTE), true);
            tpd.show(getActivity().getFragmentManager(), TAG_DIALOG_TIME);
        }
    };

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

            date.set(year,monthOfYear,dayOfMonth);
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            String dateStr = formatter.format(date.getTime());
            Calendar today = Calendar.getInstance();
            if (today.get(Calendar.YEAR) == year && today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)){
                dateStr += String.format(" (%s)",getString(R.string.today));
            }
            tvDate.setText(dateStr);
        }
    };

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
            date.set(Calendar.HOUR_OF_DAY,hourOfDay);
            date.set(Calendar.MINUTE, minute);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            String timeStr = formatter.format(date.getTime());
            tvTime.setText(timeStr);
        }
    };

    private AlertDialog getDialog(int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        int value, maxVal;
        switch (id){
            case DIALOG_REPEAT:
                value = Integer.parseInt(tvRepeat.getText().toString());
                maxVal = MAX_REPEAT;
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvRepeat.setText(String.valueOf(mNumberPicker.getValue()));
                    }
                });
                builder.setTitle(tvRepeatLbl.getText().toString());
                break;
            case DIALOG_INTERVAL:
                value = Integer.parseInt(tvInterval.getText().toString());
                maxVal = MAX_INTERVAL;
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvInterval.setText(String.valueOf(mNumberPicker.getValue()));
                    }
                });
                builder.setTitle(getString(R.string.days_before_repeat));
                break;
            default:
                value = maxVal = 0;
        }
        MaterialNumberPicker mnp = getNumberPicker(MIN_PICKER_VAL, maxVal, value);
        if (mnp.getParent() != null){
            ((ViewGroup)mnp.getParent()).removeView(mnp);
        }
        builder.setView(mnp);
        return builder.create();
    }

    private MaterialNumberPicker getNumberPicker(int minValue, int maxValue, int value){
        if (mNumberPicker != null){
            mNumberPicker.setMaxValue(maxValue);
            mNumberPicker.setMinValue(minValue);
            mNumberPicker.setValue(value);
        }
        else {
            mNumberPicker = new MaterialNumberPicker.Builder(getContext())
                    .minValue(minValue)
                    .maxValue(maxValue)
                    .defaultValue(value)
                    .backgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight))
                    .separatorColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                    .textColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark))
                    .textSize(20)
                    .enableFocusability(false)
                    .build();
        }
        return mNumberPicker;
    }
}
