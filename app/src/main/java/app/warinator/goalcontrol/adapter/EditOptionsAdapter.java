package app.warinator.goalcontrol.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.EditOption;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Адаптер списка настроект задачи
 */
public class EditOptionsAdapter extends RecyclerView.Adapter<EditOptionsAdapter.ViewHolder> {
    private EditOption mOptions[];
    private EditOptionsCallback mEditOptionsCallback;

    public EditOptionsAdapter(EditOption options[], EditOptionsCallback callback) {
        mOptions = options;
        mEditOptionsCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_option, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        holder.mCallback = mEditOptionsCallback;
        holder.mOption = mOptions[pos];
        holder.optionName.setText(mOptions[pos].getName());
        holder.optionInfo.setText(mOptions[pos].getInfo());
        holder.optionIcon.setIcon(mOptions[pos].getIcon());
        holder.itemView.setBackgroundResource(R.color.colorWhite);

        if (!mOptions[pos].isSwitcheable()) {
            holder.optionSwitch.setVisibility(View.GONE);
        } else {
            holder.optionSwitch.setVisibility(View.VISIBLE);
            holder.optionSwitch.setChecked(mOptions[pos].isActive());
            if (!holder.optionSwitch.isChecked()) {
                holder.itemView.setBackgroundResource(R.color.colorGreyVeryLight);
            }
        }
        final int position = pos;
        holder.itemView.setOnClickListener(v -> mEditOptionsCallback.handleEditOptionClick(
                position, mOptions[position].getId()));
    }

    @Override
    public int getItemCount() {
        return mOptions.length;
    }

    public interface EditOptionsCallback {
        void handleEditOptionClick(int pos, int optResId);

        void handleEditOptionSwitch(EditOption option, boolean active);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_option_name)
        TextView optionName;
        @BindView(R.id.tv_option_info)
        TextView optionInfo;
        @BindView(R.id.iiv_option_icon)
        IconicsImageView optionIcon;
        @BindView(R.id.sw_option_state)
        SwitchCompat optionSwitch;
        private EditOption mOption;
        private EditOptionsCallback mCallback;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            optionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (mOption.isSwitcheable()) {
                    mOption.setActive(isChecked);
                    if (!isChecked) {
                        itemView.setBackgroundResource(R.color.colorGreyVeryLight);
                    } else {
                        itemView.setBackgroundResource(R.color.colorWhite);
                    }
                }
            });
            optionSwitch.setOnClickListener(v1 -> mCallback.handleEditOptionSwitch(
                    mOption, optionSwitch.isChecked()));
        }

    }

}