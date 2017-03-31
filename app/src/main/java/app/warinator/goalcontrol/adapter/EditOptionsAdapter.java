package app.warinator.goalcontrol.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import app.warinator.goalcontrol.EditOptionsCallback;
import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.misc.EditOption;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Warinator on 16.03.2017.
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
        holder.optionName.setText(mOptions[pos].getName());
        //holder.optionInfo.setText(mOptions[pos].getInfo());
        holder.optionIcon.setIcon(mOptions[pos].getIcon());
        if (!mOptions[pos].isSwitcheable()){
            holder.optionSwitch.setChecked(true);
            holder.optionSwitch.setVisibility(View.GONE);
        }
        else {
            if (!holder.optionSwitch.isChecked()){
                holder.itemView.setBackgroundResource(R.color.colorGreyVeryLight);
            }
        }
        final int position = pos;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditOptionsCallback.handleEditOptionClick(position, mOptions[position].getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mOptions.length;
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

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }
}