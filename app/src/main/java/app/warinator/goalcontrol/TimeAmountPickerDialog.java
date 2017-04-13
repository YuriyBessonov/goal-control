package app.warinator.goalcontrol;

import android.widget.Toast;

import app.warinator.goalcontrol.utils.Util;
import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialogFragment;

/**
 * Диалог выбора интервала времени
 */
public class TimeAmountPickerDialog extends TimeDurationPickerDialogFragment {
    private DurationSetCallback mCaller;
    private int mDestId;
    private static final long INIT_DURATION = 15 * 60 * 1000;
    public static final long MAX_DURATION = (24*60 - 1)*60*1000;

    public TimeAmountPickerDialog(){}

    public static TimeAmountPickerDialog newInstance(DurationSetCallback caller, int destId){
        TimeAmountPickerDialog dialog = new TimeAmountPickerDialog();
        dialog.mCaller = caller;
        dialog.mDestId = destId;
        return dialog;
    }

    @Override
    protected long getInitialDuration() {
        return INIT_DURATION;
    }


    @Override
    protected int setTimeUnits() {
        return TimeDurationPicker.HH_MM;
    }



    @Override
    public void onDurationSet(TimeDurationPicker view, long duration) {
        if (duration <= MAX_DURATION){
            mCaller.onTimeAmountPicked(mDestId, duration);
        }
        else {
            Toast.makeText(getActivity(),getString(R.string.value_should_not_exceed)+ Util.getFormattedTime(MAX_DURATION),Toast.LENGTH_SHORT).show();
        }
    }

    public interface DurationSetCallback {
        void onTimeAmountPicked(int destId, long duration);
    }
}
