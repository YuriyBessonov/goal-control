package app.warinator.goalcontrol.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionButton;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.EditOptionsAdapter;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.database.DAO.TrackUnitDAO;
import app.warinator.goalcontrol.fragment.CategoriesDialogFragment;
import app.warinator.goalcontrol.fragment.ChecklistDialogFragment;
import app.warinator.goalcontrol.fragment.IconPickerDialogFragment;
import app.warinator.goalcontrol.fragment.PriorityPickerDialogFragment;
import app.warinator.goalcontrol.fragment.ProjectsDialogFragment;
import app.warinator.goalcontrol.fragment.TaskAppointDialogFragment;
import app.warinator.goalcontrol.fragment.TaskChronoDialogFragment;
import app.warinator.goalcontrol.fragment.TaskNotesEditDialogFragment;
import app.warinator.goalcontrol.fragment.TaskProgressConfDialogFragment;
import app.warinator.goalcontrol.fragment.TaskReminderDialogFragment;
import app.warinator.goalcontrol.model.Category;
import app.warinator.goalcontrol.model.CheckListItem;
import app.warinator.goalcontrol.model.EditOption;
import app.warinator.goalcontrol.model.Project;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.model.TrackUnit;
import app.warinator.goalcontrol.model.Weekdays;
import app.warinator.goalcontrol.tasks.TaskScheduler;
import app.warinator.goalcontrol.utils.ColorUtil;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.list.SimpleListDialog;
import es.dmoral.toasty.Toasty;
import rx.Observable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import static eltos.simpledialogfragment.list.CustomListDialog.SELECTED_SINGLE_POSITION;

/**
 * Активность редактирования задачи
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
        TaskProgressConfDialogFragment.OnTaskProgressConfiguredListener,
        ChecklistDialogFragment.OnChecklistChangedListener,
        SimpleDialog.OnDialogResultListener {
    public static final String ARG_TASK_ID = "task_id";
    public static final String TAG_DIALOG = "dialog";
    private static final int[] mOptionLabels = {R.string.task_option_priority, R.string.task_option_project, R.string.task_option_appoint, R.string.task_option_category,
            R.string.task_option_progress, R.string.task_option_chrono, R.string.task_option_reminder, R.string.task_option_comment};
    private static final String DIALOG_PROJECT = "dialog_project";
    private static final String DIALOG_APPOINT = "dialog_appoint";
    private static final String DIALOG_PROGRESS_CONF = "dialog_progress_conf";
    private static final String DIALOG_PRIORITY = "dialog_priority";
    private static final String DIALOG_CHRONO = "dialog_chrono";
    private static final String DIALOG_EDIT_CATEGORY = "dialog_edit_category";
    private static final String DIALOG_EDIT_REMINDER = "dialog_edit_reminder";
    private static final String DIALOG_NOTE = "dialog_note";
    private static final String DIALOG_ICON_PICKER = "dialog_icon_picker";

    @BindView(R.id.rv_task_edit_options)
    RecyclerView rvTaskEditOptions;
    @BindView(R.id.fab_save)
    FloatingActionButton fabSave;
    @BindView(R.id.inc_toolbar)
    View incToolbar;

    private TbEdit mTbEdit = new TbEdit();
    private EditOption[] mOptions;
    private EditOptionsAdapter mAdapter;
    private Task mTask;
    private ArrayList<CheckListItem> mTodoList;
    private CompositeSubscription mSub;
    private boolean mTaskAppointChanged = false;

    //Выбор пункта настроек
    private EditOptionsAdapter.EditOptionsCallback mEditOptionCallback =
            new EditOptionsAdapter.EditOptionsCallback() {
        @Override
        public void handleEditOptionClick(int pos, int optResId) {
            FragmentTransaction ft;
            DialogFragment fragment;
            switch (optResId) {
                case R.string.task_option_project:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = ProjectsDialogFragment.newInstance();
                    fragment.show(ft, DIALOG_PROJECT);
                    break;
                case R.string.task_option_appoint:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskAppointDialogFragment.newInstance(mTask.getBeginDate(),
                            mTask.isWithTime(), mTask.getWeekdays(), mTask.getIntervalValue(),
                            mTask.getRepeatCount());
                    fragment.show(ft, DIALOG_APPOINT);
                    break;
                case R.string.task_option_progress:
                    ft = getSupportFragmentManager().beginTransaction();
                    long unitsId = (mTask.getUnits() == null) ? 0 : mTask.getUnits().getId();
                    int repeatCount = 1;
                    if (mTask.isRepeatable()) {
                        if (mTask.isInterval()) {
                            repeatCount = mTask.getRepeatCount();
                        } else {
                            repeatCount = mTask.getRepeatCount() *
                                    mTask.getWeekdays().getCheckedDays().size();
                        }
                    }
                    fragment = TaskProgressConfDialogFragment.newInstance(mTask.getId(),
                            mTask.getProgressTrackMode(), unitsId, mTask.getAmountTotal(),
                            mTask.getAmountOnce(), repeatCount, mTodoList);
                    fragment.show(ft, DIALOG_PROGRESS_CONF);
                    break;
                case R.string.task_option_priority:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = PriorityPickerDialogFragment.newInstance();
                    fragment.show(ft, DIALOG_PRIORITY);
                    break;
                case R.string.task_option_chrono:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskChronoDialogFragment.newInstance(
                            mTask.getChronoTrackMode(), mTask.getWorkTime(),
                            mTask.getSmallBreakTime(), mTask.getBigBreakTime(),
                            mTask.getIntervalsCount(), mTask.getBigBreakEvery());
                    fragment.show(ft, DIALOG_CHRONO);
                    break;
                case R.string.task_option_category:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = CategoriesDialogFragment.newInstance();
                    fragment.show(ft, DIALOG_EDIT_CATEGORY);
                    break;
                case R.string.task_option_reminder:
                    if (!mTask.isWithTime()) {
                        Toasty.warning(TaskEditActivity.this,
                                getString(R.string.specify_task_time_to_set_reminder)).show();
                        break;
                    }
                    long timeBefore = Math.max(mTask.getReminder(), 0);
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskReminderDialogFragment.newInstance(
                            mTask.getBeginDate().getTimeInMillis(), timeBefore);
                    fragment.show(ft, DIALOG_EDIT_REMINDER);
                    break;
                case R.string.task_option_comment:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskNotesEditDialogFragment.newInstance(mTask.getNote());
                    fragment.show(ft, DIALOG_NOTE);
                    break;
            }
        }

        //Обработка переключения активности опции
        @Override
        public void handleEditOptionSwitch(EditOption option, boolean active) {
            switch (option.getId()) {
                case R.string.task_option_project:
                    if (active) {
                        option.setActive(false);
                        mEditOptionCallback.handleEditOptionClick(0, option.getId());
                    } else {
                        mTask.setProject(null);
                    }
                    updateOptionDetails(option.getId());
                    break;
                case R.string.task_option_category:
                    if (active) {
                        option.setActive(false);
                        mEditOptionCallback.handleEditOptionClick(0, option.getId());
                    } else {
                        mTask.setCategory(null);
                    }
                    updateOptionDetails(option.getId());
                    break;
                case R.string.task_option_reminder:
                    if (active) {
                        if (!mTask.isWithTime()) {
                            setOptionActive(R.string.task_option_reminder, false);
                            Toasty.warning(TaskEditActivity.this,
                                    getString(R.string.specify_task_time_to_set_reminder)).show();
                        } else if (mTask.getReminder() < 0) {
                            mTask.setReminder(0);
                            updateOptionDetails(R.string.task_option_reminder);
                        }
                    }
                    break;
            }

        }
    };

    //Выбор иконки
    private View.OnClickListener onTaskIconClick = v -> {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        IconPickerDialogFragment fragment = IconPickerDialogFragment.newInstance();
        fragment.show(ft, DIALOG_ICON_PICKER);
    };

    //Получение намерения запуска активности
    public static Intent getIntent(long taskId, Context context) {
        Intent intent = new Intent(context, TaskEditActivity.class);
        intent.putExtra(ARG_TASK_ID, taskId);
        return intent;
    }

    //Обработка результата диалога обновления параметров назначения задачи
    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (which == BUTTON_POSITIVE) {
            int pos = extras.getInt(SELECTED_SINGLE_POSITION);
            TaskScheduler.UpdateMethod updMethod =
                    TaskScheduler.UpdateMethod.values()[pos];
            updateTask(updMethod);
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);
        ButterKnife.bind(this);
        ButterKnife.bind(mTbEdit, incToolbar);

        mSub = new CompositeSubscription();

        setSupportActionBar((Toolbar) incToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.rv_task_edit_options);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        initOptions();
        mAdapter = new EditOptionsAdapter(mOptions, mEditOptionCallback);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mTbEdit.laTaskIcon.setOnClickListener(onTaskIconClick);
        fabSave.setOnClickListener(v -> saveIfNameIsUnique());
        mSub.add(RxTextView.textChanges(mTbEdit.etTaskName)
                .subscribe(charSequence -> validateNameIsNotEmpty()));

        mTask = new Task();
        if (savedInstanceState == null) {
            Bundle b = getIntent().getExtras();
            long taskId = b.getLong(ARG_TASK_ID, 0);
            if (taskId != 0) {
                mSub.add(TaskDAO.getDAO().get(taskId).subscribe(task -> {
                    mTask = task;
                    setupTask();
                }));
            } else {
                initTaskDefault();
                setupTask();
            }
        }
    }

    //Сохранить задачу
    private void saveTask() {
        mTask.setName(mTbEdit.etTaskName.getText().toString());
        if (!isOptionActive(R.string.task_option_project)) {
            mTask.setProject(null);
        }
        if (!isOptionActive(R.string.task_option_appoint)) {
            mTask.setBeginDate(null);
        }
        if (!isOptionActive(R.string.task_option_category)) {
            mTask.setCategory(null);
        }
        if (!isOptionActive(R.string.task_option_reminder)) {
            mTask.setReminder(-1);
        }
        if (!isOptionActive(R.string.task_option_comment)) {
            mTask.setNote(null);
        }
        if (mTodoList == null) {
            mTodoList = new ArrayList<>();
        }

        if (mTask.getId() == 0) {
            mSub.add(TaskDAO.getDAO().add(mTask).concatMap(new Func1<Long, Observable<List<Long>>>() {
                @Override
                public Observable<List<Long>> call(Long taskId) {
                    mTask.setId(taskId);
                    return CheckListItemDAO.getDAO().replaceForTask(mTask.getId(), mTodoList);
                }
            }).subscribe(longs -> {
                Toasty.info(TaskEditActivity.this, getString(R.string.task_added)).show();
                setResult(RESULT_OK);
                TaskScheduler.createConcreteTasks(mTask);
                finish();
            }));
        } else {
            if (mTaskAppointChanged) {
                int[] data = new int[]{R.string.left_all, R.string.delete_all,
                        R.string.delete_only_when_days_are_the_same};
                SimpleListDialog.build()
                        .title(R.string.what_to_do_with_alredy_appointed)
                        .choiceMode(ListView.CHOICE_MODE_SINGLE)
                        .items(getBaseContext(), data)
                        .choicePreset(0)
                        .show(this, TAG_DIALOG);
            } else {
                updateTask(TaskScheduler.UpdateMethod.LEFT_ALL);
            }
        }
    }

    //Обновить задачу
    private void updateTask(TaskScheduler.UpdateMethod updMethod) {
        mSub.add(TaskDAO.getDAO().update(mTask).concatMap(new Func1<Integer, Observable<List<Long>>>() {
            @Override
            public Observable<List<Long>> call(Integer aInt) {
                return CheckListItemDAO.getDAO().replaceForTask(mTask.getId(), mTodoList);
            }
        }).subscribe(longs -> {
            if (mTaskAppointChanged) {
                TaskScheduler.createConcreteTasks(mTask, updMethod);
            } else {
                ConcreteTaskDAO.getDAO().trigger();
            }
            Toasty.info(TaskEditActivity.this,  getString(R.string.task_updated)).show();
            setResult(RESULT_OK);
            finish();
        }));
    }

    //Проверить, чтобы имя было непустым
    private void validateNameIsNotEmpty() {
        if (Util.editTextIsEmpty(mTbEdit.etTaskName)) {
            mTbEdit.tilTaskName.setError(getString(R.string.err_name_not_specified));
            fabSave.setEnabled(false);
        } else {
            mTbEdit.tilTaskName.setErrorEnabled(false);
            fabSave.setEnabled(true);
        }
    }

    //Проверить уникальность имени и сохранить, если уникально
    private void saveIfNameIsUnique() {
        mSub.add(TaskDAO.getDAO().exists(mTbEdit.etTaskName.getText().toString()).subscribe(exists -> {
            if (!exists || mTbEdit.etTaskName.getText().toString().equals(mTask.getName())) {
                saveTask();
            } else {
                mTbEdit.tilTaskName.setError(getString(R.string.name_should_be_unique));
                fabSave.setEnabled(false);
            }
        }));
    }

    //Отобразить параметры задачи
    private void setupTask() {
        for (int mOptionLabel : mOptionLabels) {
            updateOptionDetails(mOptionLabel);
        }
        mTbEdit.etTaskName.setText(mTask.getName());
        mTbEdit.iivTaskIcon.setIcon(GoogleMaterial.Icon.values()[mTask.getIcon()]);
        int colInd = (mTask.getProject() != null) ?
                mTask.getProject().getColor() : ColorUtil.COLOR_DEFAULT;
        mTbEdit.iivTaskIcon.getBackground().setColorFilter(ColorUtil.getProjectColor(colInd, this),
                PorterDuff.Mode.SRC_ATOP);
    }

    //Инициализировать список параметров задачи
    private void initOptions() {
        String[] icons = getResources().getStringArray(R.array.task_option_items);
        mOptions = new EditOption[icons.length];
        for (int i = 0; i < icons.length; i++) {
            String name = getString(mOptionLabels[i]);
            mOptions[i] = new EditOption(mOptionLabels[i], name, icons[i]);
            if (mOptionLabels[i] == R.string.task_option_progress ||
                    mOptionLabels[i] == R.string.task_option_priority ||
                    mOptionLabels[i] == R.string.task_option_chrono) {
                mOptions[i].setSwitcheable(false);
            } else {
                mOptions[i].setSwitcheable(true);
            }
        }
    }

    //Обновить подробности пункта настроек
    private void updateOptionDetails(int labelId) {
        switch (labelId) {
            case R.string.task_option_project:
                if (mTask.getProject() != null) {
                    setOptionInfo(R.string.task_option_project, mTask.getProject().getName(), true);
                    mTbEdit.iivTaskIcon.getBackground().setColorFilter(ColorUtil
                            .getProjectColor(mTask.getProject().getColor(), this), PorterDuff.Mode.SRC_ATOP);
                } else {
                    setOptionInfo(R.string.task_option_project, getString(R.string.by_default), false);
                    mTbEdit.iivTaskIcon.getBackground().setColorFilter(ColorUtil
                            .getProjectColor(ColorUtil.COLOR_DEFAULT, this), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case R.string.task_option_appoint:
                if (mTask.getBeginDate() == null || mTask.getBeginDate().getTimeInMillis() == 0) {
                    setOptionInfo(R.string.task_option_appoint, getString(R.string.not_defined), false);
                } else {
                    StringBuilder sb = new StringBuilder();
                    Calendar date = mTask.getBeginDate();
                    if (mTask.isWithTime()) {
                        sb.append(getString(R.string.at));
                        sb.append(" ");
                        sb.append(Util.getFormattedTime(date));
                        sb.append(", ");
                    } else {
                        setOptionActive(R.string.task_option_reminder, false);
                    }
                    if (mTask.isRepeatable()) {
                        sb.append(getString(R.string.repeat_lowercase));
                        sb.append(" ");
                        if (mTask.isInterval()) {
                            int count = mTask.getIntervalValue();
                            if (count > 1) {
                                sb.append(getResources().getQuantityString
                                        (R.plurals.plurals_days, count, count));
                            } else {
                                sb.append(getString(R.string.every_day));
                            }
                        } else {
                            sb.append(getString(R.string.on));
                            sb.append(" ");
                            sb.append(Util.weekdaysStr(mTask.getWeekdays(), this));
                        }
                        sb.append(" ");
                        sb.append(getString(R.string.starting));
                        sb.append(" ");
                    }
                    sb.append(Util.getFormattedDate(mTask.getBeginDate(), this, true));
                    setOptionInfo(R.string.task_option_appoint, sb.toString(), true);
                }
                break;
            case R.string.task_option_priority:
                setOptionInfo(R.string.task_option_priority, getResources().
                        getStringArray(R.array.priorities)[mTask.getPriority().ordinal()]);
                break;
            case R.string.task_option_category:
                if (mTask.getCategory() != null) {
                    setOptionInfo(R.string.task_option_category, mTask.getCategory().getName(), true);
                } else {
                    setOptionInfo(R.string.task_option_category, getString(R.string.common), false);
                }
                break;
            case R.string.task_option_progress:
                setOptionInfo(R.string.task_option_progress, getResources()
                        .getStringArray(R.array.progress_track_mode)[mTask
                        .getProgressTrackMode().ordinal()]);
                break;
            case R.string.task_option_chrono:
                setOptionInfo(R.string.task_option_chrono, getResources()
                                .getStringArray(R.array.chrono_track_mode)[mTask
                                .getChronoTrackMode().ordinal()],
                        mTask.getChronoTrackMode() != Task.ChronoTrackMode.NONE);
                break;
            case R.string.task_option_reminder:
                if (mTask.getReminder() >= 0) {
                    long timeBefore = mTask.getReminder();
                    String reminderStr;
                    if (timeBefore > 0) {
                        reminderStr = String.format(getString(R.string.before_x),
                                Util.getFormattedTimeWithUnits(timeBefore, this));
                    } else {
                        reminderStr = getString(R.string.in_specified_time);
                    }
                    setOptionInfo(R.string.task_option_reminder, reminderStr, true);
                } else {
                    setOptionInfo(R.string.task_option_reminder, getString(R.string.not_defined), false);
                }
                break;
            case R.string.task_option_comment:
                if (mTask.getNote() != null) {
                    setOptionInfo(R.string.task_option_comment, mTask.getNote(), true);
                } else {
                    setOptionInfo(R.string.task_option_comment, getString(R.string.not_defined), false);
                }
                break;
        }
        int i = getOptionIndById(labelId);
        mAdapter.notifyItemChanged(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //Обработка выбора иконки
    @Override
    public void onIconPicked(int position, String icon) {
        mTbEdit.iivTaskIcon.setIcon(icon);
        mTask.setIcon(position);
    }

    //Инициализировать задачу значениями по умолчанию
    private void initTaskDefault() {
        mTask.setWithTime(false);
        mTask.setPriority(Task.Priority.MEDIUM);
        mTask.setProgressTrackMode(Task.ProgressTrackMode.MARK);
        mTask.setChronoTrackMode(Task.ChronoTrackMode.DIRECT);
        Calendar cal = Calendar.getInstance();
        mTask.setBeginDate(cal);
        mTask.setReminder(-1);
        Weekdays wd = new Weekdays(0);
        wd.setDay(cal.get(Calendar.DAY_OF_WEEK), true);
        mTask.setWeekdays(wd);
    }

    //Обработка выбора категории
    @Override
    public void onCategorySelected(Category category) {
        mTask.setCategory(category);
        updateOptionDetails(R.string.task_option_category);
    }

    //Обработка редактирования примеачния
    @Override
    public void onNoteEdited(String note) {
        if (note.trim().length() > 0) {
            mTask.setNote(note);
        } else {
            mTask.setNote(null);
        }
        updateOptionDetails(R.string.task_option_comment);
    }

    //Получить индекс параметра по id
    private int getOptionIndById(int labelId) {
        for (int i = 0; i < mOptionLabels.length; i++) {
            if (mOptionLabels[i] == labelId) {
                return i;
            }
        }
        return -1;
    }

    //Задать сведения о параметре
    private int setOptionInfo(int labelId, String info) {
        int i = getOptionIndById(labelId);
        mOptions[i].setInfo(info);
        mAdapter.notifyItemChanged(i);
        return i;
    }

    //Задать сведения о параметре
    private void setOptionInfo(int labelId, String info, boolean active) {
        int i = setOptionInfo(labelId, info);
        mOptions[i].setActive(active);
        mAdapter.notifyItemChanged(i);
    }

    //Задать активность параметра
    private void setOptionActive(int labelId, boolean active) {
        int i = getOptionIndById(labelId);
        mOptions[i].setActive(active);
        mAdapter.notifyItemChanged(i);
    }

    //Проверить активность параметра
    private boolean isOptionActive(int labelId) {
        int i = getOptionIndById(labelId);
        return mOptions[i].isActive();
    }

    //Обработка выбора приоритета
    @Override
    public void onPrioritySelected(int pos) {
        mTask.setPriority(Task.Priority.values()[pos]);
        updateOptionDetails(R.string.task_option_priority);
    }

    //Обработка задания напоминания
    @Override
    public void onReminderSet(long timeBefore) {
        mTask.setReminder(timeBefore);
        updateOptionDetails(R.string.task_option_reminder);
    }

    //Обработка выбора проекта
    @Override
    public void onProjectPicked(Project project) {
        mTask.setProject(project);
        updateOptionDetails(R.string.task_option_project);
    }

    //Обработка задания настроек учета времени
    @Override
    public void onChronoTrackSet(Task.ChronoTrackMode mode, long workTime, long breakTime,
                                 long bigBreakTime, int intervals, int bigBreakEvery) {
        mTask.setChronoTrackMode(mode);
        mTask.setWorkTime(workTime);
        mTask.setSmallBreakTime(breakTime);
        mTask.setBigBreakTime(bigBreakTime);
        mTask.setIntervalsCount(intervals);
        mTask.setBigBreakEvery(bigBreakEvery);
        updateOptionDetails(R.string.task_option_chrono);
    }

    //Обработка задания настроек назначения задачи
    @Override
    public void onTaskAppointSet(Calendar date, boolean isWithTime, Weekdays weekdays,
                                 int repInterval, int repCount) {
        mTaskAppointChanged = true;
        mTask.setBeginDate(date);
        mTask.setWithTime(isWithTime);
        mTask.setWeekdays(weekdays);
        mTask.setInterval(repInterval > 0);
        mTask.setIntervalValue(repInterval);
        mTask.setRepeatable(repCount > 0);
        mTask.setRepeatCount(repCount);
        updateOptionDetails(R.string.task_option_appoint);
    }

    //Обработка задания настроек учета прогресса
    @Override
    public void onTaskProgressConfigured(Task.ProgressTrackMode mode, final TrackUnit units,
                                         int amountTotal, int amountOnce) {
        mTask.setProgressTrackMode(mode);
        mTask.setUnits(units);
        if (units != null) {
            if (units.getId() != 0) {
                mSub.add(TrackUnitDAO.getDAO().update(units).subscribe());
            } else {
                mSub.add(TrackUnitDAO.getDAO().exists(units.getName()).concatMap(exists -> {
                    if (exists) {
                        return TrackUnitDAO.getDAO().getByName(units.getName());
                    } else {
                        return TrackUnitDAO.getDAO().add(units);
                    }
                }).concatMap(new Func1<Object, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Object o) {
                        if (o instanceof TrackUnit) {
                            //получен существующий объект
                            units.setId(((TrackUnit) o).getId());
                            return TrackUnitDAO.getDAO().update(units);//число обновл. строк
                        } else {
                            //получен id добавленного
                            long id = (long) o;
                            units.setId(id);
                            return Observable.just(-1);
                        }
                    }
                }).subscribe());
            }
        }
        mTask.setAmountTotal(amountTotal);
        mTask.setAmountOnce(amountOnce);
        updateOptionDetails(R.string.task_option_progress);
    }

    //Обработка изменения списка дел
    @Override
    public void onCheckListChanged(ArrayList<CheckListItem> list) {
        mTodoList = list;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DIALOG_PROGRESS_CONF);
        if (fragment != null) {
            ((TaskProgressConfDialogFragment) fragment).updateTodoListItemsCount(list.size());
        }
    }

    @Override
    public void onCheckListEditDone(ArrayList<CheckListItem> list, boolean cancelled, int checkedDiff) {
    }

    @Override
    protected void onDestroy() {
        if (!mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
        super.onDestroy();
    }

    static class TbEdit {
        @BindView(R.id.til_task_name)
        TextInputLayout tilTaskName;
        @BindView(R.id.et_name)
        EditText etTaskName;
        @BindView(R.id.la_task_icon)
        FrameLayout laTaskIcon;
        @BindView(R.id.iiv_task_icon)
        IconicsImageView iivTaskIcon;
    }
}
