package app.warinator.goalcontrol.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eralp.circleprogressview.CircleProgressView;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.misc.DummyTask;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Адаптер списка задач
 */
public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {
    private DummyTask mTasks[];

    public TasksAdapter(DummyTask tasks[]) {
        mTasks = tasks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_3, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        holder.time.setText(mTasks[pos].dueTime);
        holder.name.setText(mTasks[pos].name);
        holder.projectName.setText(mTasks[pos].project);
        holder.date.setText(mTasks[pos].dueDate);
        if (!mTasks[pos].repeat) {
            holder.repeat.setVisibility(View.INVISIBLE);
        }
        holder.todayNeed.setText(String.valueOf(mTasks[pos].needToday));
        holder.allDone.setText(String.valueOf(mTasks[pos].doneAll));
        holder.allNeed.setText(String.valueOf(mTasks[pos].needAll));
        holder.progressReal.setProgress(mTasks[pos].donePercent);
        holder.progressExp.setProgress(mTasks[pos].needPercent);
        holder.icon.setIcon(CommunityMaterial.Icon.valueOf(mTasks[pos].icon));
    }

    @Override
    public int getItemCount() {
        return mTasks.length;
    }

    public static class ViewHolder extends AbstractSwipeableItemViewHolder {
        @BindView(R.id.tv_due_time)
        TextView time;
        @BindView(R.id.tv_task_name)
        TextView name;
        @BindView(R.id.tv_project_name)
        TextView projectName;
        @BindView(R.id.tv_due_date)
        TextView date;
        @BindView(R.id.iiv_repeatable)
        IconicsImageView repeat;
        @BindView(R.id.tv_count_today_need)
        TextView todayNeed;
        @BindView(R.id.tv_count_all_done)
        TextView allDone;
        @BindView(R.id.tv_count_all_need)
        TextView allNeed;

        @BindView(R.id.pv_real)
        CircleProgressView progressReal;
        @BindView(R.id.pv_expected)
        CircleProgressView progressExp;
        @BindView(R.id.iiv_task_icon)
        IconicsImageView icon;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @Override
        public View getSwipeableContainerView() {
            return null;
        }
    }
}
