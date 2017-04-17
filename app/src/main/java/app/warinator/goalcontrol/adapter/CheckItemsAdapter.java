package app.warinator.goalcontrol.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.main.CheckListItem;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Warinator on 15.04.2017.
 */

public class CheckItemsAdapter extends RecyclerView.Adapter<CheckItemsAdapter.ViewHolder>{
    private final List<CheckListItem> mItems;

    private final OnItemRemovedListener mListener;
    private Context mContext;
    private boolean mIsEditable;

    public CheckItemsAdapter(Context context, OnItemRemovedListener listener, List<CheckListItem> items, boolean isEditable) {
        mListener = listener;
        mContext = context;
        mItems = items;
        mIsEditable = isEditable;
    }

    @Override
    public CheckItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_check, parent, false);
        return new CheckItemsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CheckItemsAdapter.ViewHolder holder, int position) {
        holder.tvItem.setText(mItems.get(position).getValue());
        holder.cbItem.setChecked(mItems.get(position).isCompleted());
        if (mIsEditable){
            holder.cbItem.setEnabled(false);
        }
        else {
            holder.cbItem.setEnabled(true);
            if (holder.cbItem.isChecked()){
                holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite));
            }
            else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorGreyLight));
            }
        }
        holder.mListener = mListener;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface OnItemRemovedListener {
        void onItemRemoved(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnLongClickListener,
            View.OnClickListener{
        private OnItemRemovedListener mListener;
        @BindView(R.id.tv_item)
        TextView tvItem;
        @BindView(R.id.cb_item)
        CheckBox cbItem;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            cbItem.setOnClickListener(this);
            if (mIsEditable){
                view.setOnLongClickListener(this);
            }
            else {
                view.setOnClickListener(this);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            mItems.remove(pos);
            notifyItemRemoved(pos);
            mListener.onItemRemoved(pos);
            return false;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() != R.id.cb_item){
                cbItem.setChecked(!cbItem.isChecked());
            }
            int pos = getAdapterPosition();
            mItems.get(pos).setCompleted(cbItem.isChecked());
            notifyItemChanged(pos);
        }

    }
}

