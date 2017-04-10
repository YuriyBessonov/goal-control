package app.warinator.goalcontrol.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.main.Weekdays;
import app.warinator.goalcontrol.util.Util;
import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;

/**
 * Настройки даты и времени назначения задачи
 */
public class TaskAppointDialogFragment extends DialogFragment {
    private static final int MAX_INTERVAL = 366;
    private static final int MAX_REPEAT = 1000;
    private static final int MIN_PICKER_VAL = 1;
    private static final int DIALOG_INTERVAL = 1;
    private static final int DIALOG_REPEAT = 2;
    private static final int POS_ASGN_ONCE = 0;
    private static final int POS_ASGN_REP = 1;
    private static final int POS_INT_WEEKDAYS = 0;
    private static final int POS_INT_DAYS = 1;
    private static final String TAG_DIALOG_DATE = "dialog_date";
    private static final String TAG_DIALOG_TIME = "dialog_time";
    private static final String ARG_DATE = "mDate";
    private static final String ARG_WITH_TIME = "with_time";
    private static final String ARG_WEEKDAYS = "weekdays";
    private static final String ARG_REP_INTERVAL = "rep_interval";
    private static final String ARG_REP_COUNT = "rep_count";
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
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
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
    @BindView(R.id.cb_monday)
    CheckBox cbMonday;
    @BindView(R.id.cb_tuesday)
    CheckBox cbTuesday;
    @BindView(R.id.cb_wednesday)
    CheckBox cbWednesday;
    @BindView(R.id.cb_thursday)
    CheckBox cbThursday;
    @BindView(R.id.cb_friday)
    CheckBox cbFriday;
    @BindView(R.id.cb_saturday)
    CheckBox cbSaturday;
    @BindView(R.id.cb_sunday)
    CheckBox cbSunday;
    @BindView(R.id.rb_assign_once)
    RadioRealButton rbAssignOnce;
    @BindView(R.id.rb_assign_regular)
    RadioRealButton rbAssignRegular;
    @BindView(R.id.rb_repeat_weekdays)
    RadioRealButton rbRepeatWeekdays;
    @BindView(R.id.rb_repeat_interval)
    RadioRealButton rbRepeatInterval;
    @BindView(R.id.rbg_assign)
    RadioRealButtonGroup rbgAssign;
    @BindView(R.id.rbg_repeat)
    RadioRealButtonGroup rbgRepeat;
    @BindView(R.id.btn_reset_date)
    ImageButton btnResetDate;
    @BindView(R.id.btn_inverse_mark)
    ImageButton btnInverseWeekdays;
    @BindView(R.id.btn_remove_time)
    ImageButton btnRemoveTime;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;

    private Calendar mDate;
    private boolean mIsWithTime;
    private Weekdays mWeekdays;
    private int mRepeatInterval;
    private int mRepeatCount;
    private boolean mIsRepeatable;
    private boolean mIsInterval;

    private MaterialNumberPicker mNumberPicker;
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
    //Обновить дату
    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            mDate.set(year, monthOfYear, dayOfMonth);
            tvDate.setText(Util.getFormattedDate(mDate, getContext()));
        }
    };
    //Выбрать дату
    private View.OnClickListener onDateOptionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    onDateSetListener,
                    mDate.get(Calendar.YEAR),
                    mDate.get(Calendar.MONTH),
                    mDate.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show(getActivity().getFragmentManager(), TAG_DIALOG_DATE);
        }
    };
    //Обновить время
    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
            mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mDate.set(Calendar.MINUTE, minute);
            mIsWithTime = true;
            btnRemoveTime.setVisibility(View.VISIBLE);
            tvTime.setText(Util.getFormattedTime(mDate));
        }
    };
    //Выбрать время
    private View.OnClickListener onTimeOptionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog tpd = TimePickerDialog.newInstance(onTimeSetListener,
                    mDate.get(Calendar.HOUR_OF_DAY),
                    mDate.get(Calendar.MINUTE), true);
            tpd.show(getActivity().getFragmentManager(), TAG_DIALOG_TIME);
        }
    };
    //Убрать время
    private View.OnClickListener onRemoveTimeBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsWithTime = false;
            btnRemoveTime.setVisibility(View.INVISIBLE);
            tvTime.setText(R.string.not_specified);
        }
    };
    //Установить дату на сегодня
    private View.OnClickListener onResetDateBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar cal = Calendar.getInstance();
            mDate.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            tvDate.setText(Util.getFormattedDate(mDate, getContext()));
        }
    };
    //Инвертировать выбранные дги недели
    private View.OnClickListener onInverseWeekdaysBtnClick = new View.OnClickListener() {
        final int[] checkBoxIds = {R.id.cb_monday, R.id.cb_tuesday, R.id.cb_wednesday, R.id.cb_thursday,
                R.id.cb_friday, R.id.cb_saturday, R.id.cb_sunday};

        @Override
        public void onClick(View v) {
            for (int id : checkBoxIds) {
                if (getView() != null){
                    CheckBox cb = (CheckBox) getView().findViewById(id);
                    cb.setChecked(!cb.isChecked());
                }
            }
        }
    };
    private View.OnClickListener onOkBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mIsInterval){
                mRepeatInterval = 0;
            }
            if (!mIsRepeatable){
                mRepeatCount = 0;
            }
            mListener.onTaskAppointSet(mDate, mIsWithTime, mWeekdays, mRepeatInterval, mRepeatCount);
            dismiss();
        }
    };
    private OnTaskAppointSetListener mListener;

    public TaskAppointDialogFragment() {
    }

    public static TaskAppointDialogFragment newInstance(Calendar date, boolean isWithTime, Weekdays weekdays,
                                                        int repInterval, int repCount) {
        TaskAppointDialogFragment fragment = new TaskAppointDialogFragment();
        Bundle args = new Bundle();
        if (date != null) {
            args.putLong(ARG_DATE, date.getTimeInMillis());
        }
        args.putBoolean(ARG_WITH_TIME, isWithTime);
        args.putInt(ARG_WEEKDAYS, weekdays.getBitMask());
        args.putInt(ARG_REP_INTERVAL, repInterval);
        args.putInt(ARG_REP_COUNT, repCount);
        fragment.setArguments(args);
        return fragment;
    }


    private void applyBundle(Bundle b) {
        //дата
        mDate = Calendar.getInstance();
        long date = b.getLong(ARG_DATE,0);
        if (date > 0){
            mDate.setTimeInMillis(date);
        }
        tvDate.setText(Util.getFormattedDate(mDate, getContext()));
        //время
        mIsWithTime = b.getBoolean(ARG_WITH_TIME);
        if (mIsWithTime){
            tvTime.setText(Util.getFormattedTime(mDate));
        }
        else {
            tvTime.setText(R.string.not_specified);
            btnRemoveTime.setVisibility(View.INVISIBLE);
        }
        //дни недели
        mWeekdays = new Weekdays(b.getInt(ARG_WEEKDAYS));
        cbMonday.setChecked(mWeekdays.getDay(Weekdays.Day.MONDAY));
        cbTuesday.setChecked(mWeekdays.getDay(Weekdays.Day.TUESDAY));
        cbWednesday.setChecked(mWeekdays.getDay(Weekdays.Day.WEDNESDAY));
        cbThursday.setChecked(mWeekdays.getDay(Weekdays.Day.THURSDAY));
        cbFriday.setChecked(mWeekdays.getDay(Weekdays.Day.FRIDAY));
        cbSaturday.setChecked(mWeekdays.getDay(Weekdays.Day.SATURDAY));
        cbSunday.setChecked(mWeekdays.getDay(Weekdays.Day.SUNDAY));
        //повторений
        mRepeatCount = b.getInt(ARG_REP_COUNT);
        if (mRepeatCount > 0){
            mIsRepeatable = true;
        }
        else {
            mIsRepeatable = false;
            mRepeatCount = 1;
        }
        tvRepeat.setText(String.valueOf(mRepeatCount));
        //интервал
        mRepeatInterval = b.getInt(ARG_REP_INTERVAL);
        if (mRepeatInterval > 0){
            mIsInterval = true;
        }
        else {
            mIsInterval = false;
            mRepeatInterval = 1;
        }
        tvInterval.setText(String.valueOf(mRepeatInterval));

        rbgRepeat.setPosition(mIsRepeatable ? POS_ASGN_REP : POS_ASGN_ONCE);
        rbgAssign.setPosition(mIsInterval ? POS_INT_DAYS : POS_INT_WEEKDAYS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_appoint_dialog, container, false);
        ButterKnife.bind(this, v);
        tvDialogTitle.setText(R.string.task_option_appoint);
        laInterval.setOnClickListener(onIntervalOptionClick);
        laDate.setOnClickListener(onDateOptionClick);
        laTime.setOnClickListener(onTimeOptionClick);
        laRepeat.setOnClickListener(onRepeatOptionClick);
        btnResetDate.setOnClickListener(onResetDateBtnClick);
        btnRemoveTime.setOnClickListener(onRemoveTimeBtnClick);
        btnInverseWeekdays.setOnClickListener(onInverseWeekdaysBtnClick);

        btnOk.setOnClickListener(onOkBtnClick);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        rbgAssign.setPosition(0);
        rbgRepeat.setPosition(0);
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

        if (savedInstanceState != null){
            applyBundle(savedInstanceState);
        }
        else if (getArguments() != null){
            applyBundle(getArguments());
        }

        return v;
    }

    //Настроить элементы
    private void initControls(int assignPos, int repeatPos) {
        if (assignPos == POS_ASGN_ONCE) {
            mIsRepeatable = false;
            laRegularOptions.setVisibility(View.GONE);
            tvDateLbl.setText(R.string.date);
        } else {
            mIsRepeatable = true;
            laRegularOptions.setVisibility(View.VISIBLE);
            tvDateLbl.setText(R.string.begin_date);
            if (repeatPos == POS_INT_WEEKDAYS) {
                mIsInterval = false;
                laWeekdays.setVisibility(View.VISIBLE);
                laInterval.setVisibility(View.GONE);
                tvRepeatLbl.setText(R.string.repeat_weeks);
            } else {
                mIsInterval = true;
                laWeekdays.setVisibility(View.GONE);
                laInterval.setVisibility(View.VISIBLE);
                tvRepeatLbl.setText(R.string.repeat_times);
            }
        }
    }

    private AlertDialog getDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        int value, maxVal;
        switch (id) {
            case DIALOG_REPEAT:
                value = mRepeatCount;
                maxVal = MAX_REPEAT;
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRepeatCount = mNumberPicker.getValue();
                        tvRepeat.setText(String.valueOf(mRepeatCount));
                    }
                });
                builder.setTitle(tvRepeatLbl.getText().toString());
                break;
            case DIALOG_INTERVAL:
                value = mRepeatInterval;
                maxVal = MAX_INTERVAL;
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRepeatInterval = mNumberPicker.getValue();
                        tvInterval.setText(String.valueOf(mRepeatInterval));
                    }
                });
                builder.setTitle(getString(R.string.days_before_repeat));
                break;
            default:
                value = maxVal = 0;
        }
        MaterialNumberPicker mnp = getNumberPicker(MIN_PICKER_VAL, maxVal, value);
        if (mnp.getParent() != null) {
            ((ViewGroup) mnp.getParent()).removeView(mnp);
        }
        builder.setView(mnp);
        return builder.create();
    }

    private MaterialNumberPicker getNumberPicker(int minValue, int maxValue, int value) {
        if (mNumberPicker != null) {
            mNumberPicker.setMaxValue(maxValue);
            mNumberPicker.setMinValue(minValue);
            mNumberPicker.setValue(value);
        } else {
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

    public interface OnTaskAppointSetListener {
        void onTaskAppointSet(Calendar date, boolean isWithTime, Weekdays weekdays,
                              int repInterval, int repCount);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTaskAppointSetListener) {
            mListener = (OnTaskAppointSetListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " должен реализовывать "+OnTaskAppointSetListener.class.getSimpleName() );
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
