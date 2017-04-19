package app.warinator.goalcontrol.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.fragment.TaskSortDialogFragment.SortCriterionItem;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Warinator on 18.04.2017.
 */

public class SortCriteriaListAdapter extends RecyclerView.Adapter<SortCriteriaListAdapter.ViewHolder> {

    private ArrayList<SortCriterionItem> mItems;
    private OnItemClickListener mListener;

    public SortCriteriaListAdapter(ArrayList<SortCriterionItem> items, OnItemClickListener listener){
        mItems = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sort_criterion_simple, parent, false);
        return new SortCriteriaListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SortCriterionItem item = mItems.get(position);
        holder.icon.setIcon(item.getIconStr());
        holder.name.setText(item.getName());
        if (item.isSelected()){
            holder.itemView.setBackgroundResource(R.color.colorPrimaryLight);
        }
        else {
            holder.itemView.setBackgroundResource(R.color.colorWhite);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    public interface OnItemClickListener {
        void onItemClicked(int pos);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iiv_icon)
        IconicsImageView icon;
        @BindView(R.id.tv_name)
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClicked(getAdapterPosition());
        }
    }

}
