package app.warinator.goalcontrol.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.TasksComparator;
import app.warinator.goalcontrol.adapter.SortCriteriaActiveAdapter;
import app.warinator.goalcontrol.adapter.SortCriteriaListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskSortDialogFragment extends DialogFragment implements SortCriteriaListAdapter.OnItemClickListener, SortCriteriaActiveAdapter.OnItemClickListener {
    @BindView(R.id.rv_active_cr)
    RecyclerView rvActiveCr;
    @BindView(R.id.rv_all_cr)
    RecyclerView rvAllCr;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_reset)
    ImageButton btnReset;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;

    SortCriteriaListAdapter mAllAdapter;
    SortCriteriaActiveAdapter mActiveAdapter;
    ArrayList<SortCriterionItem> mAllItems;
    ArrayList<SortCriterionItem> mActiveItems;
    private OnSortCriteriaSetListener mListener;

    public TaskSortDialogFragment() {
    }

    private static final String ARG_CRITERIA = "criteria";
    public static TaskSortDialogFragment getInstance(ArrayList<TasksComparator.SortCriterion> sortCriteria){
        TaskSortDialogFragment fragment = new TaskSortDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CRITERIA, sortCriteria);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_sort_dialog, container, false);
        ButterKnife.bind(this,v);
        tvDialogTitle.setText(R.string.sort);

        mAllItems = new ArrayList<>();
        for (TasksComparator.SortCriterion.Key c : TasksComparator.SortCriterion.Key.values()){
            int i = c.ordinal();
            String name = getResources().getStringArray(R.array.sort_criterion_names)[i];
            String icon = getResources().getStringArray(R.array.sort_criterion_icons)[i];
            SortCriterionItem item = new SortCriterionItem(name);
            item.setIconStr(icon);
            item.getCriterion().key = c;
            mAllItems.add(item);
        }

        rvAllCr.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAllCr.setItemAnimator(new DefaultItemAnimator());
        mAllAdapter = new SortCriteriaListAdapter(mAllItems, this);
        rvAllCr.setAdapter(mAllAdapter);

        rvActiveCr.setLayoutManager(new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false));
        rvActiveCr.setItemAnimator(new DefaultItemAnimator());
        mActiveItems = new ArrayList<>();
        mActiveAdapter = new SortCriteriaActiveAdapter(mActiveItems, getContext(), this);
        ArrayList<TasksComparator.SortCriterion> criteria;
        if (savedInstanceState != null){
           criteria = savedInstanceState.getParcelableArrayList(ARG_CRITERIA);
        }
        else {
            criteria = getArguments().getParcelableArrayList(ARG_CRITERIA);
        }
        if (criteria != null){
            for (TasksComparator.SortCriterion cr : criteria){
                SortCriterionItem item = mAllItems.get(cr.key.ordinal());
                item.getCriterion().order = cr.order;
                item.setSelected(true);
                mActiveItems.add(item);
                mAllAdapter.notifyItemChanged(cr.key.ordinal());
            }
            mActiveAdapter.notifyDataSetChanged();
        }
        else {
            setDefaultActive();
        }
        rvActiveCr.setAdapter(mActiveAdapter);

        btnCancel.setOnClickListener(v1 -> dismiss());
        btnOk.setOnClickListener(v1 -> {
            ArrayList<TasksComparator.SortCriterion> critArr = new ArrayList<>();
            for (SortCriterionItem item : mActiveItems){
                critArr.add(item.getCriterion());
            }
            mListener.onSortCriteriaSet(critArr);
            dismiss();
        });
        btnReset.setOnClickListener(v1 -> setDefaultActive());
        return v;
    }

    private void setDefaultActive(){
        mActiveItems.clear();
        for (int i=0; i<mAllItems.size(); i++){
            SortCriterionItem item = mAllItems.get(i);
            if (item.getCriterion().key == TasksComparator.SortCriterion.Key.PROGRESS_LACK ||
                    item.getCriterion().key == TasksComparator.SortCriterion.Key.PROGRESS_EXP ||
                    item.getCriterion().key == TasksComparator.SortCriterion.Key.PRIORITY){
                item.getCriterion().order = TasksComparator.SortCriterion.Order.DESC;
            }
            else {
                item.getCriterion().order = TasksComparator.SortCriterion.Order.ASC;
            }
            mActiveItems.add(item);
            item.setSelected(true);
            mAllAdapter.notifyItemChanged(i);
        }
        mActiveAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOrderBtnClicked(int pos) {
        TasksComparator.SortCriterion.Order order = mActiveItems.get(pos)
                .getCriterion().order;
        if (order == TasksComparator.SortCriterion.Order.ASC){
            order = TasksComparator.SortCriterion.Order.DESC;
        }
        else {
            order = TasksComparator.SortCriterion.Order.ASC;
        }
        mActiveItems.get(pos).getCriterion().order = order;
        mActiveAdapter.notifyItemChanged(pos);
    }

    @Override
    public void onLabelClicked(int pos) {
        SortCriterionItem item = mActiveItems.get(pos);
        mActiveItems.remove(pos);
        mActiveAdapter.notifyItemRemoved(pos);
        int ind = item.getCriterion().key.ordinal();
        mAllItems.get(ind).setSelected(false);
        mAllAdapter.notifyItemChanged(ind);
    }

    @Override
    public void onLabelLongClicked(int pos) {
        SortCriterionItem item = mActiveItems.get(pos);
        mActiveItems.remove(pos);
        mActiveItems.add(0, item);
        mActiveAdapter.notifyItemMoved(pos, 0);
    }

    @Override
    public void onItemClicked(int pos) {
        SortCriterionItem item = mAllItems.get(pos);
        item.setSelected(!item.isSelected());
        mAllAdapter.notifyItemChanged(pos);
        if (item.isSelected()){
            mActiveItems.add(item);
            mActiveAdapter.notifyItemInserted(mActiveAdapter.getItemCount()-1);
        }
        else {
            for (int i=0; i<mActiveItems.size(); i++){
                item = mActiveItems.get(i);
                if (item.getCriterion().key.ordinal() == pos){
                    mActiveItems.remove(i);
                    mActiveAdapter.notifyItemRemoved(i);
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof OnSortCriteriaSetListener) {
            mListener = (OnSortCriteriaSetListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " должен реализовывать " + OnSortCriteriaSetListener.class.getSimpleName());
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public interface OnSortCriteriaSetListener {
        void onSortCriteriaSet(ArrayList<TasksComparator.SortCriterion> criteria);
    }

    public static class SortCriterionItem {
        String name;
        String iconStr;

        public TasksComparator.SortCriterion getCriterion() {
            return mCriterion;
        }

        public void setCriterion(TasksComparator.SortCriterion criterion) {
            mCriterion = criterion;
        }

        TasksComparator.SortCriterion mCriterion = new TasksComparator.SortCriterion();

        boolean selected;

        public SortCriterionItem(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIconStr() {
            return iconStr;
        }

        public void setIconStr(String iconStr) {
            this.iconStr = iconStr;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

}
