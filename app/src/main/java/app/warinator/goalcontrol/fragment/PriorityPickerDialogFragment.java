package app.warinator.goalcontrol.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.PrioritiesRecyclerViewAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Фрагмент со списком приоритетов
 */
public class PriorityPickerDialogFragment extends DialogFragment implements PrioritiesRecyclerViewAdapter.ItemClickCallback {
    @BindView(R.id.rv_priorities)
    RecyclerView rvPriorities;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;

    @Override
    public void onItemClicked(int position) {
        ((OnPrioritySelectedListener)getContext()).onPrioritySelected(position);
        dismiss();
    }

    public static class Priority {
        public String name;
        public int color;
        public Priority(String name, int color){
            this.name = name;
            this.color = color;
        }
    }

    private PrioritiesRecyclerViewAdapter mAdapter;
    private ArrayList<Priority> mValues;


    public PriorityPickerDialogFragment() {}

    public static PriorityPickerDialogFragment newInstance() {
        return new PriorityPickerDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof OnPrioritySelectedListener)){
            throw new RuntimeException("Родительский объект должен реализовывать "+
                    OnPrioritySelectedListener.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_priority_dialog, container, false);
        ButterKnife.bind(this,view);

        rvPriorities.setLayoutManager(new LinearLayoutManager(getContext()));
        String[] prioNames = getResources().getStringArray(R.array.priorities);
        mValues = new ArrayList<>();
        for (int i=0; i<prioNames.length; i++){
            mValues.add(new Priority(prioNames[i],
                    getResources().getIntArray(R.array.palette_priorities)[i]));
        }
        mAdapter = new PrioritiesRecyclerViewAdapter(this, mValues);
        rvPriorities.setAdapter(mAdapter);
        btnOk.setVisibility(View.INVISIBLE);
        tvDialogTitle.setText(R.string.task_option_priority);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    public interface OnPrioritySelectedListener {
        void onPrioritySelected(int pos);
    }

}
