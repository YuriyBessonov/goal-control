package app.warinator.goalcontrol;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.mikepenz.iconics.view.IconicsTextView;

import app.warinator.goalcontrol.model.DummyTask;
import butterknife.BindView;
import butterknife.ButterKnife;
import github.nisrulz.stackedhorizontalprogressbar.StackedHorizontalProgressBar;

/**
 * Created by Warinator on 26.01.2017.
 */

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {
    private DummyTask mTasks[];

    public TasksAdapter(DummyTask tasks[]) {
        mTasks = tasks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
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
        holder.todayDone.setText(String.valueOf(mTasks[pos].doneToday));
        holder.todayNeed.setText(String.valueOf(mTasks[pos].needToday));
        holder.allDone.setText(String.valueOf(mTasks[pos].doneAll));
        holder.allNeed.setText(String.valueOf(mTasks[pos].needAll));
        holder.progressReal.setText(String.valueOf(mTasks[pos].donePercent) + "%");
        holder.progressExp.setText(String.valueOf(mTasks[pos].needPercent));
        holder.icon.setIcon(CommunityMaterial.Icon.valueOf(mTasks[pos].icon));
        holder.icon.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        holder.progressBar.setMax(100);
        holder.progressBar.setProgress(mTasks[pos].donePercent);
        int lack = mTasks[pos].needPercent - mTasks[pos].donePercent;
        if (lack < 0) {
            lack = 0;
        }
        holder.progressBar.setSecondaryProgress(lack);
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
        @BindView(R.id.itv_repeatable)
        IconicsTextView repeat;
        @BindView(R.id.tv_count_today_done)
        TextView todayDone;
        @BindView(R.id.tv_count_today_need)
        TextView todayNeed;
        @BindView(R.id.tv_count_all_done)
        TextView allDone;
        @BindView(R.id.tv_count_all_need)
        TextView allNeed;
        @BindView(R.id.tv_progress_real)
        TextView progressReal;
        @BindView(R.id.tv_progress_exp)
        TextView progressExp;
        @BindView(R.id.iiv_task_icon)
        IconicsImageView icon;
        @BindView(R.id.pb_progress_hor)
        StackedHorizontalProgressBar progressBar;

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