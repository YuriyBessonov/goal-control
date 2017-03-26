package app.warinator.goalcontrol;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class TaskChronoDialogFragment extends DialogFragment {

    public TaskChronoDialogFragment() {}

    public static TaskChronoDialogFragment newInstance(){
        TaskChronoDialogFragment fragment = new TaskChronoDialogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_chrono_dialog, container, false);
        return v;
    }

}
