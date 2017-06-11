package app.warinator.goalcontrol.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.fragment.TaskSortDialogFragment.SortCriterionItem;
import app.warinator.goalcontrol.tasks.TasksComparator;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Warinator on 18.04.2017.
 */

public class SortCriteriaActiveAdapter extends RecyclerView.Adapter<SortCriteriaActiveAdapter.ViewHolder> {


    private ArrayList<SortCriterionItem> mItems;
    private Context mContext;
    private OnItemClickListener mListener;

    public SortCriteriaActiveAdapter(ArrayList<SortCriterionItem> items, Context context, OnItemClickListener listener){
        mItems = items;
        mContext = context;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sort_criterion, parent, false);
        return new SortCriteriaActiveAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SortCriterionItem item = mItems.get(position);
        holder.name.setText(item.getName());
        int resId = (item.getCriterion().order == TasksComparator.SortCriterion.Order.ASC) ? R.drawable.ic_up : R.drawable.ic_down;
        holder.btnOrder.setImageDrawable(ContextCompat.getDrawable(mContext, resId));
        int primCol = ContextCompat.getColor(mContext, R.color.colorPrimary);
        holder.itemView.getBackground().setColorFilter(primCol, PorterDuff.Mode.SRC_ATOP);
        int darkCol = ContextCompat.getColor(mContext, R.color.colorPrimaryDark);
        holder.btnOrder.getBackground().setColorFilter(darkCol, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface OnItemClickListener {
        void onOrderBtnClicked(int pos);
        void onLabelClicked(int pos);
        void onLabelLongClicked(int pos);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.btn_order)
        ImageButton btnOrder;
        @BindView(R.id.tv_name)
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            btnOrder.setOnClickListener(this);
            name.setOnClickListener(this);
            name.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_order){
                mListener.onOrderBtnClicked(getAdapterPosition());
            }
            else {
                mListener.onLabelClicked(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            mListener.onLabelLongClicked(getAdapterPosition());
            return false;
        }
    }
}
