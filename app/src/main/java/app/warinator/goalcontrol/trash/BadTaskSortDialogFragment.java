package app.warinator.goalcontrol.trash;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.robertlevonyan.views.chip.Chip;

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.TasksComparator;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class BadTaskSortDialogFragment extends DialogFragment {
    @BindView(R.id.la_active)
    LinearLayout laActive;
    @BindView(R.id.la_inactive)
    LinearLayout laInactive;
    ArrayList<Chip> mChips = new ArrayList<>();
    ArrayList<TasksComparator.SortCriterion.Order> mOrders = new ArrayList<>();

    public BadTaskSortDialogFragment() {
        // Required empty public constructor
    }


    private void onChipSelected(int tag, boolean selected){
        if (selected){
            laActive.addView(mChips.get(tag));
        }
        else {
            View target = laActive.findViewWithTag(tag);
            if (target != null){
                laActive.removeView(target);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bad_fragment_task_sort_dialog, container, false);
        ButterKnife.bind(this,v);
        String names[] = getResources().getStringArray(R.array.sort_criterion_names);
        for (TasksComparator.SortCriterion.Key c : TasksComparator.SortCriterion.Key.values()){
            Chip selChip = createChip(names[c.ordinal()]);
            selChip.setSelectable(true);
            selChip.setHasIcon(false);
            selChip.setOnSelectClickListener((selView, selected) -> {
                Chip ch = (Chip) selView.getParent();
                int tag = (int) ch.getTag();
                onChipSelected(tag, selected);
            });
            selChip.setTag(c.ordinal());
            laInactive.addView(selChip);

            Chip chip = createChip(names[c.ordinal()]);
            chip.setTag(c.ordinal());
            chip.setHasIcon(false);
            mChips.add(chip);
            mOrders.add(TasksComparator.SortCriterion.Order.ASC);
        }
        return v;
    }

    private Chip createChip(String text){

        Chip chip = new Chip(getContext());
        chip.setChipText(text);
        chip.setHasIcon(true);

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, sizeInDp(8), sizeInDp(8));
        chip.setLayoutParams(params);
        return chip;
    }

    private int sizeInDp(int size){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, size, getResources()
                        .getDisplayMetrics());
    }
}
