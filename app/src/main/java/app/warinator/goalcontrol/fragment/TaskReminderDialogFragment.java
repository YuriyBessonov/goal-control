package app.warinator.goalcontrol.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Calendar;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.ui_components.TimeAmountPickerDialog;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;


public class TaskReminderDialogFragment extends DialogFragment implements TimeAmountPickerDialog.DurationSetCallback {
    public static final long INTERVAL_5_MIN = 5*60*1000;
    public static final long INTERVAL_10_MIN = 10*60*1000;
    public static final long INTERVAL_15_MIN = 15*60*1000;
    private static final String DIALOG_TIME_PICKER = "time_picker";
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.rb_5)
    RadioButton rb5min;
    @BindView(R.id.rb_10)
    RadioButton rb10min;
    @BindView(R.id.rb_15)
    RadioButton rb15min;
    @BindView(R.id.rb_specified_time)
    RadioButton rbSpecifiedTime;
    @BindView(R.id.rb_custom)
    RadioButton rbCustom;
    @BindView(R.id.btn_edit_custom)
    ImageButton btnEditCustom;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;

    private static final String ARG_TIME_BEFORE = "time_before";
    private static final String ARG_SPECIFIED_TIME = "specified_time";

    private long mTimeBefore;
    private long mSpecifiedTime;
    private long mCustomValue = 0;
    private OnReminderSetListener mListener;

    public TaskReminderDialogFragment() {
    }

    public static TaskReminderDialogFragment newInstance(long specifiedTime, long timeBefore) {
        TaskReminderDialogFragment fragment = new TaskReminderDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TIME_BEFORE, timeBefore);
        args.putLong(ARG_SPECIFIED_TIME, specifiedTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTimeBefore = getArguments().getLong(ARG_TIME_BEFORE);
            mSpecifiedTime = getArguments().getLong(ARG_SPECIFIED_TIME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reminder_dialog, container, false);
        ButterKnife.bind(this,v);
        tvDialogTitle.setText(R.string.task_option_reminder);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(mSpecifiedTime);
        rbSpecifiedTime.setText(String.format(getString(R.string.in_time_x),Util.getFormattedTime(time)));
        if (mTimeBefore == 0){
            rbSpecifiedTime.setChecked(true);
        }
        else if (mTimeBefore == INTERVAL_5_MIN){
            rb5min.setChecked(true);
        }
        else if (mTimeBefore == INTERVAL_10_MIN){
            rb10min.setChecked(true);
        }
        else if (mTimeBefore == INTERVAL_15_MIN){
            rb15min.setChecked(true);
        }
        else {
            rbCustom.setChecked(true);
            mCustomValue = mTimeBefore;
            rbCustom.setText(String.format(getString(R.string.before_x),
                    Util.getFormattedTimeWithUnits(mTimeBefore,getContext())));
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmReminder();
            }
        });
        btnEditCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeAmountPickerDialog();
            }
        });
        return v;
    }

    private void showTimeAmountPickerDialog() {
        TimeAmountPickerDialog dialog = TimeAmountPickerDialog.newInstance(this, 0);
        dialog.show(getActivity().getFragmentManager(), DIALOG_TIME_PICKER);
    }

    public void confirmReminder() {
        if (mListener == null) {
            return;
        }
        if (rbSpecifiedTime.isChecked()){
            mTimeBefore = 0;
        }
        else if (rb5min.isChecked()){
            mTimeBefore = INTERVAL_5_MIN;
        }
        else if (rb10min.isChecked()){
            mTimeBefore = INTERVAL_10_MIN;
        }
        else if (rb15min.isChecked()){
            mTimeBefore = INTERVAL_15_MIN;
        }
        else {
            if (mCustomValue > 0){
                mTimeBefore = mCustomValue;
            }
            else {
                rbSpecifiedTime.setChecked(true);
                return;
            }
        }
        mListener.onReminderSet(mTimeBefore);
        dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnReminderSetListener) {
            mListener = (OnReminderSetListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " должен реализовывать "+OnReminderSetListener.class.getSimpleName() );
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onTimeAmountPicked(int destId, long duration) {
        mCustomValue = duration;
        rbCustom.setText(String.format(getString(R.string.before_x),
                Util.getFormattedTimeWithUnits(mCustomValue,getContext())));
    }


    public interface OnReminderSetListener {
        void onReminderSet(long timeBefore);
    }
}
