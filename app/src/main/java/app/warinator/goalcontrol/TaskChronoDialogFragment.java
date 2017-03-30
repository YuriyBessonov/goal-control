package app.warinator.goalcontrol;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import app.warinator.goalcontrol.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;


public class TaskChronoDialogFragment extends DialogFragment implements TimeAmountPickerDialog.DurationSetCallback{
    private static final String DIALOG_TIME_PICKER = "dialog_time_picker";
    public enum TrackMode {
        DIRECT, COUNTDOWN, INTERVAL
    };
    private TrackMode mode;
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

    final int[] trackTypesIds = {R.string.direct_countdown, R.string.countdown,R.string.interval_timer};

    public TaskChronoDialogFragment() {}

    public static TaskChronoDialogFragment newInstance(){
        TaskChronoDialogFragment fragment = new TaskChronoDialogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_chrono_dialog, container, false);
        ButterKnife.bind(this,v);
        tvDialogTitle.setText(R.string.task_option_chrono);
        laCountdown.setOnClickListener(onTimeSetOptionClick);
        laWorkTime.setOnClickListener(onTimeSetOptionClick);
        laSmallBreak.setOnClickListener(onTimeSetOptionClick);
        laBigBreak.setOnClickListener(onTimeSetOptionClick);
        spTrackType.setOnItemSelectedListener(onTrackTypeSelected);
        laTrack.setOnClickListener(onLaTrackTypeClick);
        npIntervalsCount.setMinValue(1);
        prepareTrackTypes();
        updateMode();
        return v;
    }

    @Override
    public void onTimeAmountPicked(int destId, long duration) {
        String s = Util.getFormattedTime(duration);
        Log.v("TIME",String.valueOf(duration));
        if (getView() != null){
            ((TextView)getView().findViewById(destId)).setText(s);
        }
    }

    private View.OnClickListener onTimeSetOptionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
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
        }
    };
    private void showTimeAmountPickerDialog(int destId){
        TimeAmountPickerDialog dialog = TimeAmountPickerDialog.newInstance(this,destId);
        dialog.show(getActivity().getFragmentManager(), DIALOG_TIME_PICKER);
    }

    private void prepareTrackTypes(){
        String[] trackTypes = new String[trackTypesIds.length];
        for (int i=0; i< trackTypes.length; i++){
            trackTypes[i] = getString(trackTypesIds[i]);
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.custom_spinner_item, trackTypes); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTrackType.setAdapter(spinnerArrayAdapter);
    }

    private void updateMode(){
        int pos = spTrackType.getSelectedItemPosition();
        updateMode(pos);
    }

    private View.OnClickListener onLaTrackTypeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            spTrackType.performClick();
        }
    };
    private AdapterView.OnItemSelectedListener onTrackTypeSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            updateMode(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
    private void updateMode(int pos){
        mode = TrackMode.values()[pos];
        switch (mode){
            case DIRECT:
                laCountdown.setVisibility(View.GONE);
                laIntervalGroup.setVisibility(View.GONE);
                sepTrackMode.setVisibility(View.GONE);
                break;
            case COUNTDOWN:
                laCountdown.setVisibility(View.VISIBLE);
                laIntervalGroup.setVisibility(View.GONE);
                sepTrackMode.setVisibility(View.VISIBLE);
                break;
            case INTERVAL:
                laCountdown.setVisibility(View.GONE);
                laIntervalGroup.setVisibility(View.VISIBLE);
                sepTrackMode.setVisibility(View.VISIBLE);
                break;
        }
    }

}
