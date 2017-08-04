package app.warinator.goalcontrol.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.activity.TaskEditActivity;
import app.warinator.goalcontrol.activity.TaskInfoActivity;
import app.warinator.goalcontrol.adapter.TasksAdapter;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.job.RemindersManager;
import app.warinator.goalcontrol.model.CheckListItem;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.tasks.TasksComparator;
import app.warinator.goalcontrol.tasks.TasksFilter;
import app.warinator.goalcontrol.tasks.TasksProvider;
import app.warinator.goalcontrol.timer.TimerManager;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Фрагмент списка задач
 */
public class TasksFragment extends Fragment {
    private static final String ARG_MODE = "mode";
    private static final String ARG_DATE = "date";
    private static final String DIALOG_RESCHEDULE = "dialog_reschedule";
    private static final String DIALOG_CHECKLIST = "dialog_checklist";
    private static final String DIALOG_PROGRESS = "dialog_progress";
    private static final int DELAY_REMOVING = 1500;//мс
    private static final int DELAY_UPDATE_ORDER = 1500;//мс

    @BindView(R.id.cpv_tasks)
    CircularProgressView progressView;
    @BindView(R.id.iv_logo_empty)
    ImageView ivLogoEmpty;
    @BindView(R.id.v_tint)
    View progressTint;

    private RecyclerView mRecyclerView;
    private TasksAdapter mAdapter;
    private ArrayList<ConcreteTask> mTasks;
    private ConcreteTask mTargetTask;
    private Subscription mOrderSub;
    private RecyclerTouchListener mRecyclerTouchListener;
    private Calendar mDate;
    private DisplayMode mMode;
    private TasksProvider mTasksProvider;

    //выбор опции задачи из bottom меню
    private BottomSheetListener mMenuOptionSelected = new BottomSheetListener() {
        @Override
        public void onSheetShown(@NonNull BottomSheet bottomSheet) {
        }

        @Override
        public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_task_edit:
                    editTask(mTargetTask.getTask().getId());
                    break;
                case R.id.action_task_info:
                    startActivity(TaskInfoActivity.getIntent(getActivity(),
                            mTargetTask.getTask().getId()));
                    break;
                case R.id.action_task_register_progress:
                case R.id.action_task_cancel_progress:
                    registerProgress();
                    break;
                case R.id.action_task_add_to_queue:
                    addToQueued(mTargetTask.getId());
                    break;
                case R.id.action_task_remove_from_queue:
                    removeFromQueued(mTargetTask.getId());
                    break;
            }
        }

        @Override
        public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @DismissEvent int i) {
        }
    };

    public TasksFragment() {
    }

    public static TasksFragment getInstance(DisplayMode mode) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode.ordinal());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tasks, container, false);
        ButterKnife.bind(this, rootView);

        mTasksProvider = new TasksProvider();
        mDate = Calendar.getInstance();
        if (savedInstanceState != null) {
            mMode = DisplayMode.values()[savedInstanceState.getInt(ARG_MODE)];
            long date = savedInstanceState.getLong(ARG_DATE, 0);
            if (date > 0) {
                mDate = Calendar.getInstance();
                mDate.setTimeInMillis(date);
            }
        } else {
            mMode = DisplayMode.values()[getArguments().getInt(ARG_MODE)];
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_tasks);

        mRecyclerTouchListener = new RecyclerTouchListener(getActivity(), mRecyclerView);
        configureTouchListener();

        mTasks = new ArrayList<>();
        mAdapter = new TasksAdapter(mTasks, getContext(), onItemMovedListener);

        setMode(mMode);
        subscribeOnProvider();

        mRecyclerView.hasFixedSize();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback mItemTouchCallback = new RVHItemTouchHelperCallback(mAdapter,
                true, false, false);
        ItemTouchHelper helper = new ItemTouchHelper(mItemTouchCallback);
        helper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        return rootView;
    }

    private TasksAdapter.OnItemMovedListener onItemMovedListener = (from, to) -> {
        if (from != to && mMode == DisplayMode.QUEUED){
            saveTasksOrder();
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_MODE, mMode.ordinal());
        if (mTasksProvider.getDate() != null) {
            outState.putLong(ARG_DATE, mTasksProvider.getDate().getTimeInMillis());
        }
        super.onSaveInstanceState(outState);
    }

    //задать режим отображаемых задач
    public void setMode(DisplayMode mode) {
        if (mMode == DisplayMode.QUEUED) {
            saveTasksOrder();
        }
        mMode = mode;
        switch (mMode) {
            case QUEUED:
                mTasksProvider.tasksQueued();
                break;
            case TODAY:
                mTasksProvider.tasksToday();
                break;
            case WEEK:
                mTasksProvider.tasksForWeek();
                break;
            case DATE:
                mTasksProvider.tasksForDate(mDate);
                break;
            case WITHOUT_DATE:
                mTasksProvider.tasksWithNoDate();
                break;
        }
        subscribeOnProvider();
    }

    //подписаться на обновления списка задач
    private void subscribeOnProvider() {
        showProgress(true);
        mTasksProvider.subscribe(new TasksProvider.OnTasksUpdatedListener() {
            @Override
            public void onTasksUpdated(List<ConcreteTask> cTasks) {
                mTasks.clear();
                mTasks.addAll(cTasks);
                showProgress(false);
                if (mTasks.size() > 0) {
                    ivLogoEmpty.setVisibility(View.INVISIBLE);
                } else {
                    ivLogoEmpty.setVisibility(View.VISIBLE);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTasksUpdateError(Throwable e) {
                handleTaskOperationError(e);
            }
        });
    }

    //получить критерии сортировка
    public ArrayList<TasksComparator.SortCriterion> getSortCriteria() {
        return mTasksProvider.getSortCriteria();
    }

    //задать критерии сортировка
    public void setSortCriteria(ArrayList<TasksComparator.SortCriterion> criteria) {
        mTasksProvider.setSortCriteria(criteria);
        subscribeOnProvider();
    }

    //получить фильтр задач
    public TasksFilter getFilter() {
        return mTasksProvider.getFilter();
    }

    //установить фильтр задач
    public void setFilter(TasksFilter filter) {
        mTasksProvider.setFilter(filter);
        subscribeOnProvider();
    }

    //задать дату отображаемых задач
    public void setDisplayedDate(Calendar date) {
        if (mMode == DisplayMode.DATE) {
            mTasksProvider.tasksForDate(date);
            subscribeOnProvider();
        }
    }

    //диалог опций задачи
    private void showTaskBottomDialog(ConcreteTask task) {
        mTargetTask = task;
        Menu menu = new MenuBuilder(getContext());
        new MenuInflater(getContext()).inflate(R.menu.menu_concrete_task_options, menu);
        if (task.isQueued()){
            menu.removeItem(R.id.action_task_add_to_queue);
        }
        else {
            menu.removeItem(R.id.action_task_remove_from_queue);
        }
        Task.ProgressTrackMode taskTrackMode = task.getTask().getProgressTrackMode();
        if ((taskTrackMode == Task.ProgressTrackMode.MARK ||
                taskTrackMode == Task.ProgressTrackMode.SEQUENCE) && task.getAmountDone() > 0){
            menu.removeItem(R.id.action_task_register_progress);
        }
        else {
            menu.removeItem(R.id.action_task_cancel_progress);
        }

        BottomSheet bottomSheet = new BottomSheet.Builder(getActivity(), R.style.MyBottomSheetStyle)
                .setMenu(menu)
                .setListener(mMenuOptionSelected)
                .setTitle(task.getTask().getName())
                .create();
        bottomSheet.show();
    }

    //отметить прогресс
    private void registerProgress() {
        FragmentTransaction ft;
        DialogFragment fragment;
        Task.ProgressTrackMode mode = mTargetTask.getTask().getProgressTrackMode();
        switch (mode) {
            case MARK:
            case SEQUENCE:
                switchTargetTaskCompleted();
                break;
            case LIST:
                ft = getActivity().getSupportFragmentManager().beginTransaction();
                fragment = ChecklistDialogFragment.getInstance(mTargetTask.getTask().getId(),
                        null, false);
                fragment.show(ft, DIALOG_CHECKLIST);
                break;
            default:
                ft = getActivity().getSupportFragmentManager().beginTransaction();
                fragment = ProgressRegisterDialogFragment.newInstance(mTargetTask.getId());
                fragment.show(ft, DIALOG_PROGRESS);
                break;
        }
    }

    //отображать ли индикатор прогресса
    public void showProgress(boolean show){
        if (show){
            progressTint.setVisibility(View.VISIBLE);
            progressView.setVisibility(View.VISIBLE);
        }
        else {
            progressTint.setVisibility(View.INVISIBLE);
            progressView.setVisibility(View.INVISIBLE);
        }
    }

    //отобразить меню задачи
    public void showTaskOptions(long taskId) {
        ConcreteTaskDAO.getDAO().get(taskId).subscribe(this::showTaskBottomDialog);
    }

    //список дел изменен
    public void onChecklistChanged(ArrayList<CheckListItem> list, int checkedDiff) {
        long taskId = mTargetTask.getTask().getId();
        CheckListItemDAO.getDAO().replaceForTask(taskId, list)
                .concatMap(longs -> {
                    mTargetTask.setAmountDone(mTargetTask.getAmountDone() + checkedDiff);
                    return ConcreteTaskDAO.getDAO().update(mTargetTask);
                })
                .subscribe(aInt -> {
                   handleProgress(checkedDiff);
                });
    }

    //задан прогресс в единицах/процентах
    public void onCustomProgressSet(long amtDone){
        handleProgress(amtDone);
    }

    //отобразить уведомление об изменении прогресса и удалить задачу
    //из списка текущих, если прогресс ненулевой
    private void handleProgress(long amountDiff){
        String response = getString(R.string.progress_is_not_changed);
        if (amountDiff > 0){
            response = getString(R.string.progress_registered);
        }
        else if (amountDiff < 0){
            response = getString(R.string.regress_registered);
        }
        if (amountDiff != 0){
            Toasty.success(getContext(), response).show();
            mAdapter.notifyDataSetChanged();
            Util.timer(DELAY_REMOVING).subscribe(aLong -> {
                if (mTargetTask.isQueued()){
                    removeFromQueued(mTargetTask.getId());
                }
            });
        }
        else {
            Toasty.info(getContext(), response).show();
        }
    }

    //пометить задачу как выполненную, если она не была выполнена ранее;
    //иначе - сбросить отметку о выполнении
    private void switchTargetTaskCompleted() {
        if (mTargetTask.getAmountDone() > 0) {
            mTargetTask.setAmountDone(0);
        } else {
            mTargetTask.setAmountDone(1);
        }
        ConcreteTaskDAO.getDAO().update(mTargetTask).subscribe(integer -> {
            if (mTargetTask.getAmountDone() > 0) {
                Toasty.success(getContext(), getString(R.string.task_completion_registered)).show();
                if (mMode == DisplayMode.QUEUED){
                    Util.timer(DELAY_REMOVING).subscribe(aLong -> {
                        if (mTargetTask.isQueued()){
                            removeFromQueued(mTargetTask.getId());
                        }
                    });
                }
            } else {
                Toasty.warning(getContext(), getString(R.string.task_completion_cancelled)).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.addOnItemTouchListener(mRecyclerTouchListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRecyclerView.removeOnItemTouchListener(mRecyclerTouchListener);
    }

    //настройка обработки нажатий на задачу
    private void configureTouchListener() {
        mRecyclerTouchListener
                .setSwipeOptionViews(R.id.sw_action_delete, R.id.sw_action_reschedule)
                .setIndependentViews(R.id.btn_timer, R.id.la_progress_circle)
                .setSwipeable(R.id.la_row_fg, R.id.la_row_bg, (viewID, position) -> {
                    switch (viewID) {
                        case R.id.sw_action_delete:
                            mTargetTask = mTasks.get(position);
                            deleteTask(mTasks.get(position));
                            break;
                        case R.id.sw_action_reschedule:
                            rescheduleTask(mTasks.get(position));
                            break;
                    }
                }).setClickable(new RecyclerTouchListener.OnRowClickListener() {
            @Override
            public void onRowClicked(int position) {
                showTaskBottomDialog(mTasks.get(position));
            }

            @Override
            public void onIndependentViewClicked(int independentViewID, int position) {
                switch (independentViewID) {
                    case R.id.btn_timer:
                        TimerManager.getInstance(getContext()).startTask(mTasks.get(position));
                        break;
                    case R.id.la_progress_circle:
                        int progress = mTasks.get(position).getProgressReal();
                        Toasty.info(getContext(), getString(R.string.format_task_progress,
                                progress)).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onStop() {
        if (mMode == DisplayMode.QUEUED) {
            saveTasksOrder();
        }
        super.onStop();
    }

    //сохранить порядок задач, если отображается список текущих
    private void saveTasksOrder() {
        if (mOrderSub != null && !mOrderSub.isUnsubscribed()) {
            mOrderSub.unsubscribe();
        }
        mOrderSub = Util.timer(DELAY_UPDATE_ORDER).concatMap(aLong -> {
            if (mMode == DisplayMode.QUEUED){
                return ConcreteTaskDAO.getDAO().updateQueuePositions(mTasks);
            }
            else {
                return Observable.just(null);
            }
        }).subscribe(integers -> {});
    }

    //создать новую задачу
    public void createTask() {
        editTask(0L);
    }

    //редактировать задачу
    private void editTask(long taskId) {
        Intent intent = TaskEditActivity.getIntent(taskId, getActivity());
        startActivity(intent);
    }

    //перенести задачу
    private void rescheduleTask(ConcreteTask ct) {
        Calendar date;
        if (ct.getDateTime() != null) {
            date = Calendar.getInstance();
            date.setTimeInMillis(ct.getDateTime().getTimeInMillis());
        } else {
            date = Util.justDate(Calendar.getInstance());
        }
        date.add(Calendar.DATE, 1);
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar newDate = Util.justDate(Calendar.getInstance());
                    if (ct.getDateTime() != null) {
                        newDate.setTimeInMillis(ct.getDateTime().getTimeInMillis());
                    }
                    newDate.set(year, monthOfYear, dayOfMonth);
                    if (Util.dayIsInThePast(newDate)) {
                        Toasty.error(getContext(),
                                getString(R.string.cannot_move_task_to_the_past)).show();
                    } else if (newDate.compareTo(ct.getDateTime()) != 0) {
                        ConcreteTaskDAO.getDAO().updateDateTime(ct.getId(), newDate, ct.getQueuePos())
                                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    Toasty.success(getContext(), String.format(getString(R.string.task_rescheduled_to),
                                            Util.getFormattedDate(newDate, getContext()))).show();
                                    if (DateUtils.isToday(newDate.getTimeInMillis())) {
                                        ct.setDateTime(newDate);
                                        RemindersManager.scheduleReminder(ct);
                                    }
                                });
                    }
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getActivity().getFragmentManager(), DIALOG_RESCHEDULE);
    }

    //инициировать удаление задачи
    private void deleteTask(ConcreteTask ct) {
        long id = ct.getId();
        Util.showConfirmationDialog(getString(R.string.remove_task), getContext(),
                (dialog, which) -> removeTask(id));
    }

    //удалить задачу
    private void removeTask(long id) {
        setTaskRemoving(mTargetTask);
        ConcreteTaskDAO.getDAO().markAsRemoved(id)
                .subscribe(integer -> Toasty.success(getContext(), getString(R.string.task_removed)).show(),
                        this::handleTaskOperationError);
    }

    //удалить задачу только из списка текущих
    private void removeFromQueued(long id){
        ConcreteTaskDAO.getDAO().removeFromQueue(id).subscribe(
                integer -> Toasty.info(getContext(),
                        getString(R.string.task_removed_from_the_queued)).show(),
                this::handleTaskOperationError);
    }

    //добавить задачу в список текущих
    private void addToQueued(long id){
        ConcreteTaskDAO.getDAO().addTaskToQueue(id).subscribe(
                integer -> Toasty.info(getContext(),
                getString(R.string.task_added_to_queued)).show(), this::handleTaskOperationError);
    }

    //индикация удаляемой задачи
    private void setTaskRemoving(ConcreteTask ct){
        ct.setState(ConcreteTask.State.REMOVING);
        mAdapter.notifyDataSetChanged();
    }

    //обработчик ошибок операций с задачами
    public void handleTaskOperationError(Throwable e){
        Toasty.error(getContext(), "Произошла непредвиденная ошибка!").show();
    }

    //режим отображения задач
    public enum DisplayMode {QUEUED, TODAY, WEEK, DATE, WITHOUT_DATE}


}
