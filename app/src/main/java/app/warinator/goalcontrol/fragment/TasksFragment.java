package app.warinator.goalcontrol.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
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
import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.RemindersManager;
import app.warinator.goalcontrol.TasksComparator;
import app.warinator.goalcontrol.TasksFilter;
import app.warinator.goalcontrol.TasksProvider;
import app.warinator.goalcontrol.activity.TaskEditActivity;
import app.warinator.goalcontrol.activity.TaskInfoActivity;
import app.warinator.goalcontrol.adapter.TasksAdapter;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.model.main.CheckListItem;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
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
    private Subscription mOrderSub;
    private RecyclerTouchListener mRecyclerTouchListener;
    private ItemTouchHelper.Callback mitemTouchCallback;

    private DisplayMode mMode;
    private TasksProvider mTasksProvider;


    /*
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
    */

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
                    startActivity(TaskInfoActivity.getIntent(getActivity(), mTargetTask.getTask().getId()));
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

    public TasksFragment() {}

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

            @Override
            public void onItemMoved(int fromPos, int toPos) {
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
        return rootView;
    }

    //задать режим отображаемых задач
    public void setMode(DisplayMode mode) {
        if (mMode == DisplayMode.QUEUED){
            saveTasksOrder();
        }
        mMode = mode;
        /*
        if (mode == DisplayMode.QUEUED) {
            mRecyclerView.addOnScrollListener(onScrollListener);
        } else {
            mRecyclerView.removeOnScrollListener(onScrollListener);
        }
        */
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
            mAdapter.unsibscribeAll();
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


    public void showTaskOptions(long taskId){
        ConcreteTaskDAO.getDAO().get(taskId).subscribe(this::showTaskBottomDialog);
    }

    //список дел изменен
    public void onChecklistChanged(ArrayList<CheckListItem> list, int checkedDiff) {
        long taskId = mTargetTask.getTask().getId();
        CheckListItemDAO.getDAO().replaceForTask(taskId, list)
                .concatMap(longs -> {
                    mTargetTask.setAmountDone(mTargetTask.getAmountDone()+checkedDiff);
                    return ConcreteTaskDAO.getDAO().update(mTargetTask);
                })
                .subscribe(aInt -> {
                    Toasty.success(getContext(), getString(R.string.progress_registered)).show();
                    mAdapter.notifyDataSetChanged();
                    confirmTargetTaskDeletion();
                });
    }

    //пометить задачу как выполненную
    private void setTargetTaskCompleted() {
        if (mTargetTask.getAmountDone() > 0){
            mTargetTask.setAmountDone(0);
        }
        else {
            mTargetTask.setAmountDone(1);
        }
        ConcreteTaskDAO.getDAO().update(mTargetTask).subscribe(integer -> {
            if (mTargetTask.getAmountDone() > 0){
                Toasty.success(getContext(), getString(R.string.task_completion_registered)).show();
            }
            else {
                Toasty.warning(getContext(), getString(R.string.task_completion_cancelled)).show();
            }
            confirmTargetTaskDeletion();
        });
    }

    private void confirmTargetTaskDeletion(){
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.remove_task_from_the_list)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.yes, (dialog, which) ->
                        removeTask(mTargetTask.getId()))
                .setNegativeButton(R.string.not_yet, null).show();
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
                    switch (viewID){
                        case R.id.sw_action_delete:
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
                        break;
                }
            }
        });
    }

    @Override
    public void onStop() {
        if (mMode == DisplayMode.QUEUED){
            saveTasksOrder();
        }
        super.onStop();
    }

    private void saveTasksOrder(){
        List<Observable<Integer>> obsList = new ArrayList<>();
        int i = 1;
        for (ConcreteTask ct : mTasks){
            obsList.add(QueuedDAO.getDAO().updatePos(ct.getId(), i++));
        }
        if (mOrderSub != null && !mOrderSub.isUnsubscribed()){
            mOrderSub.unsubscribe();
        }
        mOrderSub = Observable.merge(obsList).toList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(integers -> {
                    if (TimerManager.getInstance(getContext()) != null){
                        TimerManager.getInstance(getContext()).refreshOrder();
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
                    //TODO: вернуть
                    /*if (Util.dayIsInThePast(newDate)){
                        Toasty.error(getContext(), getString(R.string.cannot_move_task_to_the_past)).show();
                    }
                    else */if (newDate.compareTo(ct.getDateTime()) != 0){
                        ConcreteTaskDAO.getDAO().updateDateTime(ct.getId(), newDate)
                                .concatMap(integer -> {
                                    Toasty.success(getContext(), String.format(getString(R.string.task_rescheduled_to),
                                            Util.getFormattedDate(newDate, getContext()))).show();
                                    return QueuedDAO.getDAO().addAllTodayTasks();
                                })
                                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    if (DateUtils.isToday(newDate.getTimeInMillis())){
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

    private void deleteTask(ConcreteTask ct){
        long id = ct.getId();
        Calendar tomorrow = Util.justDate(Calendar.getInstance());
        tomorrow.add(Calendar.DATE, 1);
        if (mMode == DisplayMode.QUEUED && (ct.getDateTime() == null ||
                ct.getDateTime().compareTo(tomorrow) >= 0)){
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.how_do_you_want_to_delete_task)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNeutralButton(R.string.from_the_list, (dialog, which) ->
                            QueuedDAO.getDAO().removeTask(id).subscribe(
                                    integer -> Toasty.success(getContext(),
                            getString(R.string.task_removed_from_the_list)).show()))
                    .setPositiveButton(R.string.completely, (dialog, which) -> removeTask(id))
                    .setNegativeButton(R.string.cancel, null).show();
        }
        else {
            Util.showConfirmationDialog(getString(R.string.remove_task), getContext(),
                    (dialog, which) -> removeTask(id));
        }
    }

    private void removeTask(long id){
        QueuedDAO.getDAO().removeTask(id).concatMap(integer -> ConcreteTaskDAO.getDAO()
                .deleteWithoutTrigger(id))
        //ConcreteTaskDAO.getDAO().markAsRemoved(id)
         //       .concatMap(integer -> QueuedDAO.getDAO().removeTask(id))
        .subscribe(integer -> Toasty.success(getContext(), getString(R.string.task_removed)).show());
    }

    public enum DisplayMode {QUEUED, TODAY, WEEK, DATE, WITHOUT_DATE}

    /*
    public interface ControlsVisibility {
        void showControls();

        void hideControls();

        boolean controlsAreShown();
    }
    */

}
