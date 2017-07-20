package app.warinator.goalcontrol.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.Category;
import app.warinator.goalcontrol.utils.ColorUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Адаптер списка категорий
 */
public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private final List<Category> mValues;
    private final OnListItemClickListener mListener;
    private Context mContext;

    public CategoriesAdapter(Context context, OnListItemClickListener listener, List<Category> values) {
        mListener = listener;
        mContext = context;
        mValues = values;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.name.setText(mValues.get(position).getName());
        int color = ColorUtil.getCategoryColor(mValues.get(position).getColor(), mContext);
        holder.mView.setBackgroundColor(color);

        holder.laFront.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onListItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public interface OnListItemClickListener {
        void onListItemClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        @BindView(R.id.tv_name)
        TextView name;
        @BindView(R.id.la_front)
        LinearLayout laFront;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }
    }
}
