package app.warinator.goalcontrol.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.natasa.progressviews.CircleProgressBar;

import java.util.List;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.model.Task.ProgressTrackMode;
import app.warinator.goalcontrol.utils.ColorUtil;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;

import static app.warinator.goalcontrol.model.Task.ProgressTrackMode.LIST;
import static app.warinator.goalcontrol.model.Task.ProgressTrackMode.MARK;
import static app.warinator.goalcontrol.model.Task.ProgressTrackMode.PERCENT;
import static app.warinator.goalcontrol.model.Task.ProgressTrackMode.SEQUENCE;
import static app.warinator.goalcontrol.model.Task.ProgressTrackMode.UNITS;

/**
 * Адаптер списка задач
 */
public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder>
        implements RVHAdapter {
    private List<ConcreteTask> mTasks;
    private Context mContext;
    private OnItemMovedListener mItemMovedListener;

    public TasksAdapter(List<ConcreteTask> tasks, Context context, OnItemMovedListener listener) {
        mContext = context;
        mTasks = tasks;
        setHasStableIds(true);
        mItemMovedListener = listener;
    }


    @Override
    public long getItemId(int position) {
        return mTasks.get(position).getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_concrete_task, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        ConcreteTask ct = mTasks.get(pos);
        Task task = ct.getTask();

        //задача, проект, иконка
        holder.tvName.setText(task.getName());
        holder.iconTask.setIcon(GoogleMaterial.Icon.values()[task.getIcon()]);
        int projectCol;
        if (task.getProject() != null) {
            projectCol = ColorUtil.getProjectColor(task.getProject().getColor(), mContext);
            holder.tvProjectName.setText(task.getProject().getName());
        } else {
            projectCol = ColorUtil.getProjectColor(ColorUtil.COLOR_DEFAULT, mContext);
            holder.tvProjectName.setText(R.string.by_default);
        }
        holder.ivIconBgr.getBackground().setColorFilter(projectCol, PorterDuff.Mode.SRC_ATOP);
        holder.iconProject.setColor(projectCol);

        //время
        holder.tvTime.setVisibility(ct.getDateTime() != null && task.isWithTime() ?
                View.VISIBLE : View.INVISIBLE);

        //дата
        if (ct.getDateTime() != null) {
            holder.laDate.setVisibility(View.VISIBLE);
            holder.tvDate.setText(Util.getFormattedDate(ct.getDateTime(), mContext, false));
            holder.iconRepeat.setVisibility(task.isRepeatable() ? View.VISIBLE : View.INVISIBLE);
            if (task.isWithTime()) {
                holder.tvTime.setVisibility(View.VISIBLE);
                holder.tvTime.setText(Util.getFormattedTime(ct.getDateTime()));
            } else {
                holder.tvTime.setVisibility(View.INVISIBLE);
            }
            holder.tvDate.setTextColor(ContextCompat.getColor(mContext, Util.dayIsInThePast(
                    ct.getDateTime()) ? R.color.colorAccent : R.color.colorGrey));
        } else {
            holder.tvTime.setVisibility(View.INVISIBLE);
            holder.laDate.setVisibility(View.INVISIBLE);
        }

        //приоритет, категория
        int prioCol = mContext.getResources().getIntArray
                (R.array.palette_priorities)[task.getPriority().ordinal()];
        holder.ivPriority.getBackground().setColorFilter(prioCol, PorterDuff.Mode.SRC_ATOP);
        int categoryCol;
        if (task.getCategory() != null) {
            holder.tvCategory.setText(task.getCategory().getName());
            categoryCol = ColorUtil.getCategoryColor(task.getCategory().getColor(), mContext);
        } else {
            holder.tvCategory.setText(R.string.common);
            categoryCol = ColorUtil.getCategoryColor(ColorUtil.COLOR_DEFAULT, mContext);
        }
        holder.tvCategory.getBackground().setColorFilter(categoryCol, PorterDuff.Mode.SRC_ATOP);

        //прогресс
        ProgressTrackMode trackMode = task.getProgressTrackMode();
        holder.pbProgressExp.setVisibility(trackMode == UNITS || trackMode == PERCENT ||
                trackMode == MARK ? View.VISIBLE : View.INVISIBLE);
        holder.laDone.setVisibility(trackMode != MARK && trackMode != SEQUENCE ?
                View.VISIBLE : View.GONE);
        holder.laNeed.setVisibility((trackMode == UNITS || trackMode == PERCENT) &&
                task.getBeginDate() != null ? View.VISIBLE : View.GONE);
        holder.laCombo.setVisibility(trackMode == SEQUENCE ? View.VISIBLE : View.GONE);
        holder.ivDone.setVisibility(trackMode == MARK && ct.getAmountDone() > 0 ?
                View.VISIBLE : View.GONE);

        if (trackMode == LIST) {
            holder.iivAll.setIcon(CommunityMaterial.Icon.cmd_checkbox_multiple_marked_outline);
        } else {
            holder.iivAll.setIcon(CommunityMaterial.Icon.cmd_checkbox_multiple_marked);
        }

        if (trackMode == MARK) {
            holder.pbProgressExp.setProgress(ct.getProgressExp());
            holder.pbProgressReal.setProgress(ct.getProgressReal());
        } else if (trackMode == SEQUENCE) {
            holder.tvComboLength.setText(String.valueOf(ct.getAmtDoneTotal()));
            holder.tvComboLbl.setText(mContext.getResources().
                    getQuantityString(R.plurals.plurals_times, ct.getAmtDoneTotal()));
            holder.pbProgressReal.setProgress(ct.getProgressReal());
        } else if (trackMode == LIST) {
            holder.tvAllDone.setText(String.valueOf(ct.getAmtDoneTotal()));
            holder.allNeed.setText(String.valueOf(ct.getAmtNeedTotal()));
            holder.tvUnits.setText("");
            holder.pbProgressReal.setProgress(ct.getProgressReal());
        } else if (trackMode == PERCENT || trackMode == UNITS) {
            int allNeed = ct.getAmtNeedTotal();
            int amtToday = ct.getAmtToday();
            int realPercent = ct.getProgressReal();
            int expectedPercent = ct.getProgressExp();

            holder.tvAllDone.setText(String.valueOf(ct.getAmtDoneTotal()));
            holder.allNeed.setText(String.valueOf(allNeed));
            holder.tvTodayDone.setText(String.valueOf(ct.getAmountDone()));
            holder.tvTodayNeed.setText(String.valueOf(amtToday));

            holder.pbProgressReal.setProgress(realPercent);
            holder.pbProgressExp.setProgress(expectedPercent);

            if (trackMode == ProgressTrackMode.PERCENT) {
                holder.tvUnits.setText(R.string.percent_char);
            } else if (task.getUnits() != null) {
                holder.tvUnits.setText(task.getUnits().getShortName());
            } else {
                holder.tvUnits.setText("");
            }

        }
        holder.pbProgressReal.setStartPositionInDegrees(270);
        holder.pbProgressExp.setStartPositionInDegrees(270);

        //учет времени
        Task.ChronoTrackMode chronoTrackMode = task.getChronoTrackMode();
        if (chronoTrackMode == Task.ChronoTrackMode.NONE) {
            holder.laTimer.setVisibility(View.INVISIBLE);
            holder.laTargetTime.setVisibility(View.INVISIBLE);
            holder.btnTimer.setVisibility(View.INVISIBLE);
        } else {
            holder.laTimer.setVisibility(View.VISIBLE);
            holder.laTargetTime.setVisibility(View.VISIBLE);
            holder.btnTimer.setVisibility(View.VISIBLE);
            holder.tvTimer.setText(Util.getFormattedTime(ct.getTimeSpent()));
        }
        switch (chronoTrackMode) {
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

        holder.btnTimer.getBackground().setColorFilter(ContextCompat.getColor(mContext,
                R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        //примечание и напоминание
        int noteCol = task.getNote() != null ? R.color.colorGrey : R.color.colorGreyVeryLight;
        holder.iconNote.setColor(ContextCompat.getColor(mContext, noteCol));
        int remCol = task.getReminder() >= 0 ? R.color.colorGrey : R.color.colorGreyVeryLight;
        holder.iconReminder.setColor(ContextCompat.getColor(mContext, remCol));

        if (ct.getDateTime() == null && task.getProgressTrackMode() == MARK) {
            holder.separatorHor.setVisibility(View.GONE);
        } else {
            holder.separatorHor.setVisibility(View.VISIBLE);
        }

        if (ct.getState() != ConcreteTask.State.REMOVING &&
                (ct.getTimeSpent() >= TimeUnit.MINUTES.toMillis(1) || ct.getAmountDone() > 0) ){
            ct.setState(ConcreteTask.State.PROCESSED);
        }
        int tintColor;
        switch (ct.getState()){
            case REMOVING:
                tintColor = R.color.colorBlackSemiTransparent;
                break;
            case PROCESSED:
                tintColor = R.color.colorPrimaryVeryLight;
                break;
            default:
                tintColor = R.color.colorTransparent;
                break;
        }
        tintColor = ContextCompat.getColor(mContext, tintColor);
        if (ct.getState() == ConcreteTask.State.PROCESSED){
            holder.laRowFg.setBackgroundColor(tintColor);
            holder.tint.setBackgroundColor(ContextCompat
                    .getColor(mContext, R.color.colorTransparent));
        }
        else {
            holder.laRowFg.setBackgroundColor(ContextCompat
                    .getColor(mContext, R.color.colorWhite));
            holder.tint.setBackgroundColor(tintColor);
        }

    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        ConcreteTask t = mTasks.get(fromPosition);
        mTasks.remove(fromPosition);
        mTasks.add(toPosition, t);
        notifyItemMoved(fromPosition, toPosition);
        mItemMovedListener.onItemMoved(fromPosition, toPosition);
        return false;
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        mTasks.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements RVHViewHolder {
        //Задача
        @BindView(R.id.tv_task_name)
        TextView tvName;
        @BindView(R.id.iiv_task_icon)
        IconicsImageView iconTask;
        //Время
        @BindView(R.id.tv_due_time)
        TextView tvTime;

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
        @BindView(R.id.tv_count_today_done)
        TextView tvTodayDone;
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
        @BindView(R.id.la_row_fg)
        LinearLayout laRowFg;
        @BindView(R.id.iv_icon_bgr)
        ImageView ivIconBgr;
        @BindView(R.id.la_progress_circle)
        FrameLayout laProgressCircle;
        @BindView(R.id.la_timer_outer)
        LinearLayout laTimerOuter;
        @BindView(R.id.la_bottom)
        LinearLayout laBottom;
        @BindView(R.id.separator_hor)
        View separatorHor;
        @BindView(R.id.la_done)
        LinearLayout laDone;
        @BindView(R.id.la_need)
        LinearLayout laNeed;
        @BindView(R.id.iiv_all)
        IconicsImageView iivAll;
        @BindView(R.id.la_combo)
        LinearLayout laCombo;
        @BindView(R.id.tv_combo_length)
        TextView tvComboLength;
        @BindView(R.id.tv_combo_lbl)
        TextView tvComboLbl;
        @BindView(R.id.iv_done)
        ImageView ivDone;
        @BindView(R.id.v_tint)
        View tint;

        private View root;

        public ViewHolder(View v) {
            super(v);
            root = v;
            ButterKnife.bind(this, v);
        }

        @Override
        public void onItemSelected(int actionstate) {
        }

        @Override
        public void onItemClear() {

        }
    }

    public interface OnItemMovedListener {
        void onItemMoved(int from, int to);
    }

}