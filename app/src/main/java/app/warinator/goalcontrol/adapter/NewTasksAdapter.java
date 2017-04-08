package app.warinator.goalcontrol.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.natasa.progressviews.CircleProgressBar;

import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Адаптер списка задач
 */
public class NewTasksAdapter extends RecyclerView.Adapter<NewTasksAdapter.ViewHolder> {
    private List<ConcreteTask> mTasks;
    private Context mContext;

    public NewTasksAdapter(List<ConcreteTask> tasks, Context context) {
        mContext = context;
        mTasks = tasks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_4, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        ConcreteTask ct = mTasks.get(pos);
        Task task = ct.getTask();
        holder.tvName.setText(task.getName());

        holder.iconTask.setIcon(GoogleMaterial.Icon.values()[task.getIcon()]);
        if (task.getProject() != null){
            int projectCol = mContext.getResources()
                    .getIntArray(R.array.palette_material)[task.getProject().getColor()];
            holder.tvProjectName.setText(task.getProject().getName());
            holder.iconTask.setColor(projectCol);
            holder.iconProject.setColor(projectCol);
        }
        else {
            holder.laProject.setVisibility(View.INVISIBLE);
        }

        if (!task.isWithTime()){
            holder.laTime.setVisibility(View.INVISIBLE);
        }
        if (ct.getDateTime() != null){
            holder.tvDate.setText(Util.getFormattedDate(ct.getDateTime(),mContext));
            if (!task.isRepeatable()) {
                holder.iconRepeat.setVisibility(View.INVISIBLE);
            }
            if (task.isWithTime()){
                holder.tvTime.setText(Util.getFormattedTime(ct.getDateTime()));
            }
        }
        else {
            holder.laDate.setVisibility(View.INVISIBLE);
        }

        int prioCol = mContext.getResources().getIntArray
                (R.array.palette_priorities)[task.getPriority().ordinal()];
        holder.ivPriority.getBackground().setColorFilter(prioCol, PorterDuff.Mode.SRC_ATOP);

        if (task.getCategory() != null){
            holder.tvCategory.setText(task.getCategory().getName());
            int categoryCol = mContext.getResources().getIntArray
                    (R.array.palette_material_light)[task.getCategory().getColor()];
            holder.tvCategory.getBackground().setColorFilter(categoryCol, PorterDuff.Mode.SRC_ATOP);
        }
        else {
            holder.tvCategory.setVisibility(View.INVISIBLE);
        }

        Task.ProgressTrackMode trackMode = task.getProgressTrackMode();
        if (trackMode == Task.ProgressTrackMode.MARK ||
                trackMode == Task.ProgressTrackMode.SEQUENCE){
            holder.laProgressUnits.setVisibility(View.GONE);
            holder.pbProgressReal.setVisibility(View.INVISIBLE);
            holder.pbProgressExp.setProgress(0);
        }
        if (trackMode == Task.ProgressTrackMode.SEQUENCE){
            //отобразить layout результата
        }
        else if (trackMode != Task.ProgressTrackMode.MARK){
            holder.tvTodayNeed.setText(String.valueOf(task.getAmountOnce()));
            int allDone = 123;
            int allNeed = task.getAmountTotal();
            int realPercent = (int)(((double)allDone/(double)allNeed)*100.0);
            int expPercent = (realPercent + 15) % 100;
            holder.tvAllDone.setText(String.valueOf(allDone));
            holder.allNeed.setText(String.valueOf(allDone));
            holder.pbProgressReal.setStartPositionInDegrees(270);
            holder.pbProgressExp.setStartPositionInDegrees(270);
            holder.pbProgressReal.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            holder.pbProgressExp.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            holder.pbProgressReal.setProgress(realPercent);
            holder.pbProgressExp.setProgress(expPercent);
            if (trackMode == Task.ProgressTrackMode.PERCENT){
                holder.tvUnits.setText(R.string.percent_char);
            }
            else if (trackMode == Task.ProgressTrackMode.UNITS &&
                    task.getUnits() != null){
                holder.tvUnits.setText(task.getUnits().getShortName());
            }
        }

        Task.ChronoTrackMode chronoTrackMode = task.getChronoTrackMode();
        if (chronoTrackMode == Task.ChronoTrackMode.NONE){
            holder.laTimer.setVisibility(View.INVISIBLE);
            holder.laTargetTime.setVisibility(View.INVISIBLE);
            holder.btnTimer.setVisibility(View.INVISIBLE);
        }
        else {
            holder.tvTimer.setText(Util.getFormattedTime(ct.getTimeSpent()));
        }
        switch (chronoTrackMode){
            case DIRECT:
                holder.laTargetTime.setVisibility(View.INVISIBLE);
                break;
            case COUNTDOWN:
                holder.tvTargetTime.setText(Util.getFormattedTime(task.getWorkTime()));
                break;
            case INTERVAL:
                long workTime = task.getWorkTime() * task.getIntervalsCount();
                holder.tvTargetTime.setText(Util.getFormattedTime(workTime));
                break;
        }

        if (task.getNote() != null){
            holder.iconNote.setColor(ContextCompat.getColor(mContext, R.color.colorGrey));
        }
        if (task.getReminder() != null){
            holder.iconReminder.setColor(ContextCompat.getColor(mContext, R.color.colorGrey));
        }
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public static class ViewHolder extends AbstractSwipeableItemViewHolder {
        //Задача
        @BindView(R.id.tv_task_name)
        TextView tvName;
        @BindView(R.id.iiv_task_icon)
        IconicsImageView iconTask;
        //Время
        @BindView(R.id.tv_due_time)
        TextView tvTime;
        @BindView(R.id.la_time)
        LinearLayout laTime;

        //Дата
        @BindView(R.id.tv_due_date)
        TextView tvDate;
        @BindView(R.id.la_date)
        LinearLayout laDate;
        @BindView(R.id.iiv_repeatable)
        IconicsImageView iconRepeat;
        //Проект
        @BindView(R.id.tv_project_name)
        TextView tvProjectName;
        @BindView(R.id.iiv_project_icon)
        IconicsImageView iconProject;
        @BindView(R.id.la_project)
        LinearLayout laProject;
        //Категория
        @BindView(R.id.tv_category)
        TextView tvCategory;
        //Прогресс
        @BindView(R.id.la_progress_units)
        LinearLayout laProgressUnits;
        @BindView(R.id.tv_count_today_need)
        TextView tvTodayNeed;
        @BindView(R.id.tv_count_all_done)
        TextView tvAllDone;
        @BindView(R.id.tv_count_all_need)
        TextView allNeed;
        @BindView(R.id.tv_units)
        TextView tvUnits;
        //Индикатор прогресса
        @BindView(R.id.pb_progress_real)
        CircleProgressBar pbProgressReal;
        @BindView(R.id.pb_progress_exp)
        CircleProgressBar pbProgressExp;
        //Таймер
        @BindView(R.id.tv_timer)
        TextView tvTimer;
        @BindView(R.id.la_timer)
        LinearLayout laTimer;
        @BindView(R.id.tv_target_time)
        TextView tvTargetTime;
        @BindView(R.id.la_target_time)
        LinearLayout laTargetTime;
        @BindView(R.id.btn_timer)
        ImageButton btnTimer;
        //Приоритет
        @BindView(R.id.iv_priority)
        ImageView ivPriority;
        //Прочее
        @BindView(R.id.iiv_note)
        IconicsImageView iconNote;
        @BindView(R.id.iiv_reminder)
        IconicsImageView iconReminder;

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