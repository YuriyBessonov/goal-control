package app.warinator.goalcontrol.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.TasksComparator;
import app.warinator.goalcontrol.TasksFilter;
import app.warinator.goalcontrol.TasksProvider;
import app.warinator.goalcontrol.activity.TaskEditActivity;
import app.warinator.goalcontrol.adapter.TasksAdapter;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.model.main.CheckListItem;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback;
import rx.Subscription;

/**
 * Просмотр задач
 */
public class TasksFragment extends Fragment {
    private static final String ARG_MODE = "mode";
    private static final String DIALOG_RESCHEDULE = "dialog_reschedule";

    @BindView(R.id.cpv_tasks)
    CircularProgressView progressView;

    private RecyclerView mRecyclerView;
    private TasksAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;
    private ArrayList<ConcreteTask> mTasks;
    private ConcreteTask mTargetTask;
    private Subscription mTasksSub;
    private RecyclerTouchListener mRecyclerTouchListener;
    private ItemTouchHelper.Callback mitemTouchCallback;

    private DisplayMode mMode;
    private TasksProvider mTasksProvider;


    //отображение/скрытие управления таймером при прокрутке
    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (!(getActivity() instanceof ControlsVisibility)) {
                return;
            }
            ControlsVisibility a = (ControlsVisibility) getActivity();
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (((LinearLayoutManager) mLayoutManager).findLastCompletelyVisibleItemPosition() !=
                        mAdapter.getItemCount() - 1 && (!a.controlsAreShown() ||
                        ((LinearLayoutManager) mLayoutManager).findFirstCompletelyVisibleItemPosition() == 0)) {
                    a.showControls();
                }
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING && a.controlsAreShown()) {
                a.hideControls();
            }
            super.onScrollStateChanged(recyclerView, newState);
        }
    };

    //выбор опции задачи из bottom меню
    private BottomSheetListener mMenuOptionSelected = new BottomSheetListener() {
        @Override
        public void onSheetShown(@NonNull BottomSheet bottomSheet) {
        }

        @Override
        public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_task_edit:
                    editTask(mTargetTask.getId());
                    break;
                case R.id.action_task_info:
                    break;
                case R.id.action_task_register_progress:
                    registerProgress();
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

        if (savedInstanceState != null) {
            mMode = DisplayMode.values()[savedInstanceState.getInt(ARG_MODE)];
        } else {
            mMode = DisplayMode.values()[getArguments().getInt(ARG_MODE)];
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_tasks);

        mRecyclerTouchListener = new RecyclerTouchListener(getActivity(), mRecyclerView);
        configureTouchListener();

        mTasks = new ArrayList<>();
        mAdapter = new TasksAdapter(mTasks, getContext(), new TasksAdapter.ItemsInteractionsListener() {
            @Override
            public void cancelDrag() {
            }

        });


        mTasksProvider = new TasksProvider();
        setMode(mMode);
        subscribeOnProvider();

        mRecyclerView.hasFixedSize();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mitemTouchCallback = new RVHItemTouchHelperCallback(mAdapter, true, false, false);
        ItemTouchHelper helper = new ItemTouchHelper(mitemTouchCallback);
        helper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider_dark));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        return rootView;
    }

    //задать режим отображаемых задач
    public void setMode(DisplayMode mode) {
        mMode = mode;
        if (mode == DisplayMode.QUEUED) {
            mRecyclerView.addOnScrollListener(onScrollListener);
        } else {
            mRecyclerView.removeOnScrollListener(onScrollListener);
        }
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
                mTasksProvider.tasksForDate(Calendar.getInstance());
                break;
            case WITHOUT_DATE:
                mTasksProvider.tasksWithNoDate();
                break;
        }
        subscribeOnProvider();
    }

    //подписаться на обновления списка задач
    private void subscribeOnProvider() {
        progressView.setVisibility(View.VISIBLE);
        mTasksProvider.subscribe(cTasks -> {
            mTasks.clear();
            mTasks.addAll(cTasks);
            progressView.setVisibility(View.INVISIBLE);
            mAdapter.notifyDataSetChanged();
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
        new BottomSheet.Builder(getActivity(), R.style.MyBottomSheetStyle)
                .setSheet(R.menu.menu_concrete_task_options)
                .setListener(mMenuOptionSelected)
                .setTitle(task.getTask().getName())
                .grid()
                .show();
    }

    //отметить прогресс
    private void registerProgress() {
        FragmentTransaction ft;
        DialogFragment fragment;
        Task.ProgressTrackMode mode = mTargetTask.getTask().getProgressTrackMode();
        switch (mode) {
            case MARK:
            case SEQUENCE:
                setTargetTaskCompleted();
                break;
            case LIST:
                ft = getActivity().getSupportFragmentManager().beginTransaction();
                fragment = ChecklistDialogFragment.getInstance(mTargetTask.getTask().getId(), null, false);
                fragment.show(ft, "dialog_checklist");
                break;
            default:
                ft = getActivity().getSupportFragmentManager().beginTransaction();
                fragment = ProgressRegisterDialogFragment.newInstance(mTargetTask.getId());
                fragment.show(ft, "dialog_progress");
                break;
        }
    }

    //список дел изменен
    public void onChecklistChanged(ArrayList<CheckListItem> list) {
        long taskId = mTargetTask.getTask().getId();
        CheckListItemDAO.getDAO().replaceForTask(taskId, list)
                .subscribe(longs -> {
                    Toasty.success(getContext(), getString(R.string.progress_registered)).show();
                    mAdapter.notifyDataSetChanged();
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.remove_task_from_the_list)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes, (dialog, which) -> ConcreteTaskDAO.getDAO().
                                    markAsRemoved(mTargetTask.getId()).subscribe(integer -> {} ))
                            .setNegativeButton(R.string.not_yet, null).show();
                });
    }

    //пометить задачу как выполненную
    private void setTargetTaskCompleted() {
        mTargetTask.setAmountDone(1);
        mTargetTask.setRemoved(true);
        ConcreteTaskDAO.getDAO().update(mTargetTask)
        .concatMap(integer -> QueuedDAO.getDAO().removeTask(mTargetTask.getId()))
        .subscribe(integer -> Toasty.success(getContext(), getString(R.string.task_completion_registered)).show());
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
                .setIndependentViews(R.id.la_timer_outer, R.id.la_progress_circle)
                .setSwipeable(R.id.la_row_fg, R.id.la_row_bg, (viewID, position) -> {
                    if (viewID == R.id.sw_action_delete) {
                        long id = mTasks.get(position).getId();
                        Util.showConfirmationDialog("удалить задачу", getContext(),
                                (dialog, which) -> ConcreteTaskDAO.getDAO().markAsRemoved(id)
                                .concatMap(integer -> {
                                    Toasty.success(getContext(), getString(R.string.task_removed)).show();
                                    return QueuedDAO.getDAO().removeTask(id);
                                }).subscribe(integer -> {}));
                    } else if (viewID == R.id.sw_action_reschedule) {
                        rescheduleTask(mTasks.get(position));
                    }
                }).setClickable(new RecyclerTouchListener.OnRowClickListener() {
            @Override
            public void onRowClicked(int position) {
                showTaskBottomDialog(mTasks.get(position));
            }

            @Override
            public void onIndependentViewClicked(int independentViewID, int position) {
                switch (independentViewID) {
                    case R.id.la_timer_outer:
                        break;
                    case R.id.la_progress_circle:
                        break;
                }
            }
        });
    }

    //создать новую задачу
    public void createTask() {
        editTask(0L);
    }

    //редактировать задачу
    public void editTask(long taskId) {
        Intent intent = TaskEditActivity.getIntent(taskId, getActivity());
        startActivity(intent);
    }

    //перенести задачу
    public void rescheduleTask(ConcreteTask ct){
        Calendar date;
        if (ct.getDateTime() != null){
            date = Calendar.getInstance();
            date.setTimeInMillis(ct.getDateTime().getTimeInMillis());
        }
        else {
            date = Util.justDate(Calendar.getInstance());
        }
        date.add(Calendar.DATE, 1);
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar newDate = Util.justDate(Calendar.getInstance());
                    if (ct.getDateTime() != null){
                        newDate.setTimeInMillis(ct.getDateTime().getTimeInMillis());
                    }
                    newDate.set(year, monthOfYear, dayOfMonth);
                    if (Util.dayIsInThePast(newDate)){
                        Toasty.error(getContext(), getString(R.string.cannot_move_task_to_the_past)).show();
                    }
                    else if (newDate.compareTo(ct.getDateTime()) != 0){
                        ConcreteTaskDAO.getDAO().updateDateTime(ct.getId(), newDate)
                                .concatMap(integer -> {
                                    Toasty.success(getContext(), String.format(getString(R.string.task_rescheduled_to),
                                            Util.getFormattedDate(newDate, getContext()))).show();
                                   return QueuedDAO.getDAO().removeTask(ct.getId());
                                })
                                .concatMap(integer -> QueuedDAO.getDAO().addAllTodayTasks())
                                .subscribe(integer -> {});
                    }
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getActivity().getFragmentManager(), DIALOG_RESCHEDULE);
    }

    public enum DisplayMode {QUEUED, TODAY, WEEK, DATE, WITHOUT_DATE}

    public interface ControlsVisibility {
        void showControls();

        void hideControls();

        boolean controlsAreShown();
    }

}
