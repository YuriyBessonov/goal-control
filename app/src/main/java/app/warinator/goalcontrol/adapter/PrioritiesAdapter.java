package app.warinator.goalcontrol.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.fragment.PriorityPickerDialogFragment.Priority;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Адаптер списка категорий
 */
public class PrioritiesAdapter extends RecyclerView.Adapter<PrioritiesAdapter.ViewHolder> {

    private final List<Priority> mValues;
    private final ItemClickCallback mListener;

    public PrioritiesAdapter(ItemClickCallback listener, List<Priority> values) {
        mListener = listener;
        mValues = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_priority, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.tvPriority.setText(mValues.get(position).name);
        holder.ivPriorityL.setBackgroundColor(mValues.get(position).color);
        holder.ivPriorityR.setBackgroundColor(mValues.get(position).color);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onItemClicked(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public interface ItemClickCallback {
        void onItemClicked(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        @BindView(R.id.tv_priority)
        TextView tvPriority;
        @BindView(R.id.iv_priority_l)
        ImageView ivPriorityL;
        @BindView(R.id.iv_priority_r)
        ImageView ivPriorityR;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }
    }
}
