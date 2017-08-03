package app.warinator.goalcontrol.fragment;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.ui_components.CompactNumberPicker;
import app.warinator.goalcontrol.ui_components.TimeAmountPickerDialog;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

/**
 * Фрагмент настройки хронометража задачи
 */
public class TaskChronoDialogFragment extends DialogFragment
        implements TimeAmountPickerDialog.DurationSetCallback {
    private static final String DIALOG_TIME_PICKER = "dialog_time_picker";
    private static final String ARG_MODE = "mode";
    private static final String ARG_WORK = "work";
    private static final String ARG_BREAK = "break";
    private static final String ARG_BIG_BREAK = "big_break";
    private static final String ARG_BIG_BREAK_EVERY = "big_break_every";
    private static final String ARG_INTERVALS = "intervals";
    private static final int MIN_INTERVAL_SEQ = 2;

    @BindView(R.id.tv_countdown)
    TextView tvCountdown;
    @BindView(R.id.tv_work_time)
    TextView tvWorkTime;
    @BindView(R.id.tv_small_break)
    TextView tvSmallBreak;
    @BindView(R.id.tv_big_break)
    TextView tvBigBreak;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.np_int_count)
    CompactNumberPicker npIntervalsCount;
    @BindView(R.id.la_countdown)
    RelativeLayout laCountdown;
    @BindView(R.id.la_work_time)
    RelativeLayout laWorkTime;
    @BindView(R.id.la_small_break)
    RelativeLayout laSmallBreak;
    @BindView(R.id.la_big_break)
    RelativeLayout laBigBreak;
    @BindView(R.id.la_track)
    RelativeLayout laTrack;
    @BindView(R.id.la_interval_group)
    LinearLayout laIntervalGroup;
    @BindView(R.id.sp_track)
    Spinner spTrackType;
    @BindView(R.id.sep_track_mode)
    View sepTrackMode;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.sb_big_break_every)
    SeekBar sbBigBreakEvery;
    @BindView(R.id.tv_big_break_every)
    TextView tvBigBreakEvery;
    @BindView(R.id.la_big_break_every)
    RelativeLayout laBigBreakEvery;

    private Task.ChronoTrackMode mTrackMode;
    private long mWorkTime;
    private long mBreakTime;
    private long mBigBreakTime;
    private int mIntervals;
    private int mBigBreakEvery;

    //Вывод диалога задания интервала
    private View.OnClickListener onTimeSetOptionClick = v -> {
        switch (v.getId()) {
            case R.id.la_work_time:
                showTimeAmountPickerDialog(R.id.tv_work_time);
                break;
            case R.id.la_small_break:
                showTimeAmountPickerDialog(R.id.tv_small_break);
                break;
            case R.id.la_big_break:
                showTimeAmountPickerDialog(R.id.tv_big_break);
                break;
            case R.id.la_countdown:
                showTimeAmountPickerDialog(R.id.tv_countdown);
                break;
        }
    };

    private View.OnClickListener onLaTrackTypeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            spTrackType.performClick();
        }
    };

    //Выбор режима учета
    private AdapterView.OnItemSelectedListener onTrackTypeSelected =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateMode(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };
    private OnChronoTrackSetListener mListener;
    private CompactNumberPicker.OnValueChangeListener mOnIntervalsSet =
            new CompactNumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(int newVal) {
                    if (newVal <= MIN_INTERVAL_SEQ) {
                        sbBigBreakEvery.setVisibility(View.INVISIBLE);
                        tvBigBreakEvery.setText(R.string.no_big_break);
                        return;
                    } else {
                        sbBigBreakEvery.setVisibility(View.VISIBLE);
                        tvBigBreakEvery.setText(String.valueOf(mBigBreakEvery));
                    }
                    int sbVal = sbBigBreakEvery.getProgress();
                    int sbMax = newVal - MIN_INTERVAL_SEQ;
                    if (sbVal > sbMax) {
                        sbBigBreakEvery.setProgress(sbMax);
                    }
                    sbBigBreakEvery.setMax(sbMax);
                }
            };
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mBigBreakEvery = progress + MIN_INTERVAL_SEQ;
            tvBigBreakEvery.setText(String.valueOf(mBigBreakEvery));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
    private View.OnClickListener onOkBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIntervals = npIntervalsCount.getValue();
            if (validateValues()) {
                mListener.onChronoTrackSet(mTrackMode, mWorkTime, mBreakTime, mBigBreakTime,
                        mIntervals, mBigBreakEvery);
                dismiss();
            }
        }
    };

    public TaskChronoDialogFragment() {
    }

    public static TaskChronoDialogFragment newInstance(Task.ChronoTrackMode mode, long workTime,
                                                       long breakTime, long bigBreakTime,
                                                       int intervals, int bigBreakEvery) {
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode.ordinal());
        args.putLong(ARG_WORK, workTime);
        args.putLong(ARG_BREAK, breakTime);
        args.putLong(ARG_BIG_BREAK, bigBreakTime);
        args.putInt(ARG_INTERVALS, intervals);
        args.putInt(ARG_BIG_BREAK_EVERY, bigBreakEvery);
        TaskChronoDialogFragment fragment = new TaskChronoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_chrono_dialog, container, false);
        ButterKnife.bind(this, v);

        tvDialogTitle.setText(R.string.task_option_chrono);
        laCountdown.setOnClickListener(onTimeSetOptionClick);
        laWorkTime.setOnClickListener(onTimeSetOptionClick);
        laSmallBreak.setOnClickListener(onTimeSetOptionClick);
        laBigBreak.setOnClickListener(onTimeSetOptionClick);
        spTrackType.setOnItemSelectedListener(onTrackTypeSelected);
        laTrack.setOnClickListener(onLaTrackTypeClick);
        btnCancel.setOnClickListener(v1 -> dismiss());
        btnOk.setOnClickListener(onOkBtnClick);
        sbBigBreakEvery.setOnSeekBarChangeListener(mOnSeekBarChanged);
        npIntervalsCount.setOnValueChangeListener(mOnIntervalsSet);
        npIntervalsCount.setMinValue(1);
        prepareTrackTypes();
        if (savedInstanceState != null) {
            applyBundle(savedInstanceState);
        } else if (getArguments() != null) {
            applyBundle(getArguments());
        }
        updateMode();
        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Util.disableTitle(dialog);
        return dialog;
    }

    //Проверить корректность пользовательского ввода
    private boolean validateValues() {
        if (mWorkTime == 0 &&
                (mTrackMode == Task.ChronoTrackMode.COUNTDOWN ||
                        mTrackMode == Task.ChronoTrackMode.INTERVAL)) {
            Toasty.error(getContext(),
                    getString(R.string.work_time_cannot_be_zero)).show();
            return false;
        }
        return true;
    }

    //Инициализировать view на основе занных из Bundle
    private void applyBundle(Bundle b) {
        mTrackMode = Task.ChronoTrackMode.values()[b.getInt(ARG_MODE)];
        spTrackType.setSelection(mTrackMode.ordinal(), true);
        mWorkTime = b.getLong(ARG_WORK);
        tvWorkTime.setText(Util.getFormattedTime(mWorkTime));
        tvCountdown.setText(Util.getFormattedTime(mWorkTime));
        mBreakTime = b.getLong(ARG_BREAK);
        tvSmallBreak.setText(Util.getFormattedTime(mBreakTime));
        mBigBreakTime = b.getLong(ARG_BIG_BREAK);
        tvBigBreak.setText(Util.getFormattedTime(mBigBreakTime));
        mIntervals = b.getInt(ARG_INTERVALS);
        npIntervalsCount.setValue(mIntervals);
        mBigBreakEvery = b.getInt(ARG_BIG_BREAK_EVERY);

        if (mIntervals > MIN_INTERVAL_SEQ) {
            sbBigBreakEvery.setProgress(mBigBreakEvery - MIN_INTERVAL_SEQ);
            tvBigBreakEvery.setText(String.valueOf(mBigBreakEvery));
        } else {
            sbBigBreakEvery.setProgress(0);
            tvBigBreakEvery.setText(R.string.no_big_break);
        }
    }

    @Override
    public void onTimeAmountPicked(int destId, long duration) {
        String s = Util.getFormattedTime(duration);
        if (getView() != null) {
            ((TextView) getView().findViewById(destId)).setText(s);
            switch (destId) {
                case R.id.tv_small_break:
                    mBreakTime = duration;
                    break;
                case R.id.tv_big_break:
                    mBigBreakTime = duration;
                    break;
                case R.id.tv_countdown:
                    mWorkTime = duration;
                    tvWorkTime.setText(s);
                    break;
                case R.id.tv_work_time:
                    mWorkTime = duration;
                    tvCountdown.setText(s);
                    break;
            }
        }
    }

    //Диалог задания интервала
    private void showTimeAmountPickerDialog(int destId) {
        TimeAmountPickerDialog dialog = TimeAmountPickerDialog.newInstance(this, destId);
        dialog.show(getActivity().getFragmentManager(), DIALOG_TIME_PICKER);
    }

    //Настройка типов учета
    private void prepareTrackTypes() {
        String[] trackTypes = getResources().getStringArray(R.array.chrono_track_mode);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.custom_spinner_item, trackTypes);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTrackType.setAdapter(spinnerArrayAdapter);
    }

    //Обновить текущий тип учета
    private void updateMode() {
        int pos = spTrackType.getSelectedItemPosition();
        updateMode(pos);
    }

    //Обновить тип учета
    private void updateMode(int pos) {
        mTrackMode = Task.ChronoTrackMode.values()[pos];
        switch (mTrackMode) {
            case COUNTDOWN:
                laCountdown.setVisibility(View.VISIBLE);
                laIntervalGroup.setVisibility(View.GONE);
                sepTrackMode.setVisibility(View.VISIBLE);
                laBigBreakEvery.setVisibility(View.GONE);
                break;
            case INTERVAL:
                laCountdown.setVisibility(View.GONE);
                laIntervalGroup.setVisibility(View.VISIBLE);
                sepTrackMode.setVisibility(View.VISIBLE);
                laBigBreakEvery.setVisibility(View.VISIBLE);
                break;
            default:
                laCountdown.setVisibility(View.GONE);
                laIntervalGroup.setVisibility(View.GONE);
                sepTrackMode.setVisibility(View.GONE);
                laBigBreakEvery.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChronoTrackSetListener) {
            mListener = (OnChronoTrackSetListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + getString(R.string.must_implement) +
                    OnChronoTrackSetListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnChronoTrackSetListener {
        void onChronoTrackSet(Task.ChronoTrackMode mode, long workTime,
                              long breakTime, long bigBreakTime, int intervals, int bigBreakEvery);
    }
}
