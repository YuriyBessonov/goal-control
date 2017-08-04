package app.warinator.goalcontrol.ui_components;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.utils.Util;
import es.dmoral.toasty.Toasty;
import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialogFragment;

/**
 * Диалог выбора интервала времени
 */
public class TimeAmountPickerDialog extends TimeDurationPickerDialogFragment {
    private static final long MAX_DURATION  = (24 * 60 - 1) * 60 * 1000;
    private long initDuration = 15 * 60 * 1000;
    private DurationSetCallback mCaller;
    private int mDestId;

    public TimeAmountPickerDialog() {
    }

    public static TimeAmountPickerDialog newInstance(DurationSetCallback caller, int destId) {
        TimeAmountPickerDialog dialog = new TimeAmountPickerDialog();
        dialog.mCaller = caller;
        dialog.mDestId = destId;
        return dialog;
    }

    public static TimeAmountPickerDialog newInstance(DurationSetCallback caller, long startDuration) {
        TimeAmountPickerDialog dialog = new TimeAmountPickerDialog();
        dialog.mCaller = caller;
        dialog.initDuration = startDuration;
        return dialog;
    }

    @Override
    protected long getInitialDuration() {
        return initDuration;
    }


    @Override
    protected int setTimeUnits() {
        return TimeDurationPicker.HH_MM;
    }


    @Override
    public void onDurationSet(TimeDurationPicker view, long duration) {
        if (duration <= MAX_DURATION) {
            mCaller.onTimeAmountPicked(mDestId, duration);
        } else {
            Toasty.error(getActivity(), getString(R.string.value_should_not_exceed) +
                    Util.getFormattedTime(MAX_DURATION)).show();
        }
    }

    public interface DurationSetCallback {
        void onTimeAmountPicked(int destId, long duration);
    }
}
