package app.warinator.goalcontrol;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import app.warinator.goalcontrol.model.EditOption;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Warinator on 16.03.2017.
 */

public class EditOptionsAdapter extends RecyclerView.Adapter<EditOptionsAdapter.ViewHolder> {
    private EditOption mOptions[];

    public EditOptionsAdapter(EditOption options[]) {
        mOptions = options;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_option, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        holder.optionName.setText(mOptions[pos].getName());
        holder.optionInfo.setText(mOptions[pos].getInfo());
        holder.optionIcon.setIcon(mOptions[pos].getIcon());
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

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}