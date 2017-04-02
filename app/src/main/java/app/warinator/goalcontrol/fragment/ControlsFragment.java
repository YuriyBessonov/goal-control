package app.warinator.goalcontrol.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.warinator.goalcontrol.R;

/**
 * Фрагмент таймера задач
 */
public class ControlsFragment extends Fragment {

    public ControlsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_controls, container, false);
        return rootView;
    }

}
