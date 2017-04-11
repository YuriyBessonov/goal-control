package app.warinator.goalcontrol.activity;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mikepenz.iconics.view.IconicsImageView;

import java.util.Calendar;

import app.warinator.goalcontrol.EditOptionsCallback;
import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.EditOptionsAdapter;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.database.DAO.TrackUnitDAO;
import app.warinator.goalcontrol.fragment.CategoriesDialogFragment;
import app.warinator.goalcontrol.fragment.IconPickerDialogFragment;
import app.warinator.goalcontrol.fragment.TaskNotesEditDialogFragment;
import app.warinator.goalcontrol.fragment.PriorityPickerDialogFragment;
import app.warinator.goalcontrol.fragment.ProjectsDialogFragment;
import app.warinator.goalcontrol.fragment.TaskReminderDialogFragment;
import app.warinator.goalcontrol.fragment.TaskAppointDialogFragment;
import app.warinator.goalcontrol.fragment.TaskChronoDialogFragment;
import app.warinator.goalcontrol.fragment.TaskProgressConfDialogFragment;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.Project;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.model.main.TrackUnit;
import app.warinator.goalcontrol.model.main.Weekdays;
import app.warinator.goalcontrol.model.misc.EditOption;
import app.warinator.goalcontrol.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Редактирование задачи
 */
public class TaskEditActivity extends AppCompatActivity implements
        IconPickerDialogFragment.OnIconPickedListener,
        CategoriesDialogFragment.OnCategorySelectedListener,
        TaskNotesEditDialogFragment.OnNoteEditedListener,
        PriorityPickerDialogFragment.OnPrioritySelectedListener,
        TaskReminderDialogFragment.OnReminderSetListener,
        ProjectsDialogFragment.OnProjectPickedListener,
        TaskChronoDialogFragment.OnChronoTrackSetListener,
        TaskAppointDialogFragment.OnTaskAppointSetListener,
        TaskProgressConfDialogFragment.OnTaskProgressConfiguredListener
{
    public static final String ARG_TASK_ID = "task_id";
    private static final int[] mOptionLabels = {R.string.task_option_project, R.string.task_option_appoint, R.string.task_option_priority, R.string.task_option_category,
            R.string.task_option_progress, R.string.task_option_chrono, R.string.task_option_reminder, R.string.task_option_comment};
    @BindView(R.id.et_name)
    EditText etTaskName;
    @BindView(R.id.la_task_icon)
    FrameLayout laTaskIcon;
    @BindView(R.id.iiv_task_icon)
    IconicsImageView iivTaskIcon;
    @BindView(R.id.rv_task_edit_options)
    RecyclerView rvTaskEditOptions;

    private EditOption[] mOptions;
    private EditOptionsAdapter mAdapter;
    private Task mTask;

    //Выбор пункта настроек
    private EditOptionsCallback mEditOptionCallback = new EditOptionsCallback() {
        @Override
        public void handleEditOptionClick(int pos, int optResId) {
            FragmentTransaction ft;
            DialogFragment fragment;
            switch (optResId) {
                case R.string.task_option_project:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = ProjectsDialogFragment.newInstance();
                    fragment.show(ft, "dialog_projects");
                    break;
                case R.string.task_option_appoint:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskAppointDialogFragment.newInstance(mTask.getBeginDate(),mTask.isWithTime(),
                            mTask.getWeekdays(), mTask.getIntervalValue(), mTask.getRepeatCount());
                    fragment.show(ft, "dialog_appoint");
                    break;
                case R.string.task_option_progress:
                    //TODO
                    ft = getSupportFragmentManager().beginTransaction();
                    long unitsId = (mTask.getUnits() == null) ? 0 : mTask.getUnits().getId();
                    int repeatCount = 1;
                    if (mTask.isRepeatable()){
                        if (mTask.isInterval()){
                            repeatCount = mTask.getRepeatCount();
                        }
                        else {
                            repeatCount = mTask.getRepeatCount() *
                                    mTask.getWeekdays().getCheckedDays().size();
                        }
                    }
                    fragment = TaskProgressConfDialogFragment.newInstance(mTask.getId(),mTask.getProgressTrackMode(),
                            unitsId, mTask.getAmountTotal(),mTask.getAmountOnce(),repeatCount);
                    fragment.show(ft, "dialog_progress_conf");
                    break;
                case R.string.task_option_priority:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = PriorityPickerDialogFragment.newInstance();
                    fragment.show(ft, "dialog_priority");
                    break;
                case R.string.task_option_chrono:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskChronoDialogFragment.newInstance(
                            mTask.getChronoTrackMode(), mTask.getWorkTime(), mTask.getSmallBreakTime(),
                            mTask.getBigBreakTime(), mTask.getIntervalsCount(), mTask.getBigBreakEvery());
                    fragment.show(ft, "dialog_chrono");
                    break;
                case R.string.task_option_category:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = CategoriesDialogFragment.newInstance();
                    fragment.show(ft, "dialog_edit_category");
                    break;
                case R.string.task_option_reminder:
                    if (!mTask.isWithTime()){
                        Toast.makeText(TaskEditActivity.this,
                                getString(R.string.specify_task_time_to_set_reminder), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    long timeBefore = 0;
                    if (mTask.getReminder() != null){
                        timeBefore = mTask.getBeginDate().getTimeInMillis() -
                                mTask.getReminder().getTimeInMillis();
                    }
                    else {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(mTask.getBeginDate().getTimeInMillis());
                        mTask.setReminder(cal);
                    }
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskReminderDialogFragment.newInstance(mTask.getBeginDate().getTimeInMillis(), timeBefore);
                    fragment.show(ft, "dialog_edit_reminder");
                    break;
                case R.string.task_option_comment:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskNotesEditDialogFragment.newInstance(mTask.getNote());
                    fragment.show(ft, "dialog_note");
                    break;
            }
        }
    };
    //Выбор иконки
    private View.OnClickListener onTaskIconClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            IconPickerDialogFragment fragment = IconPickerDialogFragment.newInstance();
            fragment.show(ft, "dialog_icon_picker");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_edit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.rv_task_edit_options);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        initOptions();
        mAdapter = new EditOptionsAdapter(mOptions, mEditOptionCallback);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        laTaskIcon.setOnClickListener(onTaskIconClick);

        mTask = new Task();
        if (savedInstanceState == null){
            Bundle b = getIntent().getExtras();
            long taskId = b.getLong(ARG_TASK_ID, 0);
            if (taskId != 0){
                TaskDAO.getDAO().get(taskId).subscribe(new Action1<Task>() {
                    @Override
                    public void call(Task task) {
                        mTask = task;
                        updateOptionsDetails();
                    }
                });
            }
            else {
                updateOptionsDetails();
            }
        }
    }

    private void initOptions(){
        String[] icons = getResources().getStringArray(R.array.task_option_items);
        mOptions = new EditOption[icons.length];
        for (int i = 0; i < icons.length; i++) {
            String name = getString(mOptionLabels[i]);
            mOptions[i] = new EditOption(mOptionLabels[i], name, icons[i]);
            if (mOptionLabels[i] == R.string.task_option_progress ||
                    mOptionLabels[i] == R.string.task_option_category ||
                    mOptionLabels[i] == R.string.task_option_priority ||
                    mOptionLabels[i] == R.string.task_option_project) {
                mOptions[i].setSwitcheable(false);
            } else {
                mOptions[i].setSwitcheable(true);
            }
        }
    }

    private void updateOptionsDetails(){
        //Проект
        if (mTask.getProject() != null){
            setOptionInfo(R.string.task_option_project, mTask.getProject().getName(), true);
        }
        else {
            setOptionInfo(R.string.task_option_project, getString(R.string.by_default), false);
        }
        //Назначение задачи
        if (mTask.getBeginDate() == null || mTask.getBeginDate().getTimeInMillis() == 0){
            setOptionInfo(R.string.task_option_appoint, getString(R.string.not_defined), false);
        }
        else {
            StringBuilder sb = new StringBuilder();
            Calendar date = mTask.getBeginDate();
            if (mTask.isWithTime()){
                sb.append(getString(R.string.at));
                sb.append(" ");
                sb.append(Util.getFormattedTime(date));
                sb.append(", ");
            }
            if (mTask.isRepeatable()){
                sb.append(getString(R.string.repeat_lowercase));
                sb.append(" ");
                if (mTask.isInterval()){
                    int count = mTask.getIntervalValue();
                    if (count > 1){
                        sb.append(getResources().getQuantityString
                                (R.plurals.plurals_days, count, count));
                    }
                    else {
                        sb.append(getString(R.string.every_day));
                    }
                }
                else {
                    sb.append(getString(R.string.on));
                    sb.append(" ");
                    sb.append(Util.weekdaysStr(mTask.getWeekdays(),this));
                }
                sb.append(" ");
                sb.append(getString(R.string.starting));
                sb.append(" ");
            }
            sb.append(Util.getFormattedDate(mTask.getBeginDate(),this));
            setOptionInfo(R.string.task_option_appoint, sb.toString(), true);
        }
        //Приоритет
        setOptionInfo(R.string.task_option_priority,
                getResources().getStringArray(R.array.priorities)[mTask.getPriority().ordinal()]);
        //Категория
        if (mTask.getCategory() != null){
            setOptionInfo(R.string.task_option_category, mTask.getCategory().getName());
        }
        else {
            setOptionInfo(R.string.task_option_category, getString(R.string.common));
        }
        //Учет прогресса
        setOptionInfo(R.string.task_option_progress, getResources()
                .getStringArray(R.array.progress_track_mode)[mTask.getProgressTrackMode().ordinal()]);
        //Учет времени
        setOptionInfo(R.string.task_option_chrono, getResources()
                .getStringArray(R.array.chrono_track_mode)[mTask.getChronoTrackMode().ordinal()],
                mTask.getChronoTrackMode() != Task.ChronoTrackMode.NONE);
        //Напоминание
        if (mTask.getReminder() != null){
            long timeBefore = mTask.getBeginDate().getTimeInMillis() -
                    mTask.getReminder().getTimeInMillis();
            String reminderStr;
            if (timeBefore > 0){
                reminderStr = String.format(getString(R.string.before_x),
                        Util.getFormattedTimeWithUnits(timeBefore,this));
            }
            else {
                reminderStr = getString(R.string.in_specified_time);
            }
            setOptionInfo(R.string.task_option_reminder, reminderStr , true);
        }
        else {
            setOptionInfo(R.string.task_option_reminder, getString(R.string.not_defined), false);
        }
        //Примечание
        if (mTask.getNote() != null){
            setOptionInfo(R.string.task_option_comment, mTask.getNote(), true);
        }
        else {
            setOptionInfo(R.string.task_option_comment, getString(R.string.not_defined), false);
        }
        mAdapter.notifyDataSetChanged();
    }



    //Callbacks
    @Override
    public void onIconPicked(String icon) {
        iivTaskIcon.setIcon(icon);
    }

    @Override
    public void onCategorySelected(Category category) {
        mTask.setCategory(category);
        updateOptionsDetails();
    }

    @Override
    public void onNoteEdited(String note) {
        if (note.trim().length() > 0){
            mTask.setNote(note);
        }
        else {
            mTask.setNote(null);
        }
        updateOptionsDetails();
    }

    private int setOptionInfo(int labelId, String info){
        for (int i=0; i < mOptionLabels.length; i++){
            if (mOptionLabels[i] == labelId){
                mOptions[i].setInfo(info);
                mAdapter.notifyItemChanged(i);
                return i;
            }
        }
        return -1;
    }

    private void setOptionInfo(int labelId, String info,  boolean active){
        mOptions[setOptionInfo(labelId,info)].setActive(active);
    }

    @Override
    public void onPrioritySelected(int pos) {
        mTask.setPriority(Task.Priority.values()[pos]);
        updateOptionsDetails();
    }

    @Override
    public void onReminderSet(long timeBefore) {
        long reminderTime = mTask.getBeginDate().getTimeInMillis() - timeBefore;
        mTask.getReminder().setTimeInMillis(reminderTime);
        updateOptionsDetails();
    }

    @Override
    public void onProjectPicked(Project project) {
        mTask.setProject(project);
        updateOptionsDetails();
    }

    @Override
    public void onChronoTrackSet(Task.ChronoTrackMode mode, long workTime, long breakTime,
                                 long bigBreakTime, int intervals, int bigBreakEvery) {
        mTask.setChronoTrackMode(mode);
        mTask.setWorkTime(workTime);
        mTask.setSmallBreakTime(breakTime);
        mTask.setBigBreakTime(bigBreakTime);
        mTask.setIntervalsCount(intervals);
        mTask.setBigBreakEvery(bigBreakEvery);
        updateOptionsDetails();
    }

    @Override
    public void onTaskAppointSet(Calendar date, boolean isWithTime, Weekdays weekdays, int repInterval, int repCount) {
        mTask.setBeginDate(date);
        mTask.setWithTime(isWithTime);
        mTask.setWeekdays(weekdays);
        mTask.setInterval(repInterval > 0);
        mTask.setIntervalValue(repInterval);
        mTask.setRepeatable(repCount > 0);
        mTask.setRepeatCount(repCount);
        updateOptionsDetails();
    }

    @Override
    public void onTaskProgressConfigured(Task.ProgressTrackMode mode, final TrackUnit units, int amountTotal, int amountOnce) {
        mTask.setProgressTrackMode(mode);

        if (units != null && units.getId() == 0){
            TrackUnitDAO.getDAO().exists(units.getName()).concatMap(new Func1<Boolean, Observable<?>>() {
                @Override
                public Observable<?> call(Boolean exists) {
                    if (exists){
                        return TrackUnitDAO.getDAO().getByName(units.getName());
                    }
                    else {
                        return TrackUnitDAO.getDAO().add(units);
                    }
                }
            }).concatMap(new Func1<Object, Observable<Integer>>() {
                @Override
                public Observable<Integer> call(Object o) {
                    if (o instanceof TrackUnit){
                        //получен существующий объект
                        units.setId(((TrackUnit) o).getId());
                        return TrackUnitDAO.getDAO().update(units);//число обновл. строк
                    }
                    else {
                        //получен id добавленного
                        long id = (long)o;
                        units.setId(id);
                        return Observable.just(-1);
                    }
                }
            }).subscribe();
        }
        mTask.setUnits(units);

        mTask.setAmountTotal(amountTotal);
        mTask.setAmountOnce(amountOnce);
        updateOptionsDetails();
    }
}
