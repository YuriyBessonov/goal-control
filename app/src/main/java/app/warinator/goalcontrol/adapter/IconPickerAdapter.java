package app.warinator.goalcontrol.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.iconics.view.IconicsImageView;

import app.warinator.goalcontrol.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Адаптер списка иконок
 */
public class IconPickerAdapter extends RecyclerView.Adapter<IconPickerAdapter.ViewHolder> {

    private String[] mIcons;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    public IconPickerAdapter(Context context, String[] icons) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mIcons = icons;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_icon, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String icon = mIcons[position];
        holder.icon.setIcon(icon);
    }

    @Override
    public int getItemCount() {
        return mIcons.length;
    }

    public String getItem(int id) {
        return mIcons[id];
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iiv_icon)
        IconicsImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}