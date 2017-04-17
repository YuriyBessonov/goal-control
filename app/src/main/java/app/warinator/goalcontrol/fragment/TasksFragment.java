package app.warinator.goalcontrol.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.Calendar;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.TasksProvider;
import app.warinator.goalcontrol.activity.TaskEditActivity;
import app.warinator.goalcontrol.adapter.TasksAdapter;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.main.CheckListItem;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Просмотр задач
 */
public class TasksFragment extends Fragment {
    public enum DisplayMode { QUEUED, TODAY, WEEK, DATE, WITHOUT_DATE};

    @BindView(R.id.cpv_tasks)
    CircularProgressView progressView;

    private RecyclerView mRecyclerView;
    private TasksAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;
    private ArrayList<ConcreteTask> mTasks;
    private ConcreteTask mTargetTask;
    private CompositeSubscription mSub = new CompositeSubscription();
    private Subscription mTasksSub;
    private RecyclerTouchListener mRecyclerTouchListener;
    private ItemTouchHelper.Callback mitemTouchCallback;

    private DisplayMode mMode;
    private TasksProvider mTasksProvider;



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

    public TasksFragment() {
    }

    private static final String ARG_MODE = "mode";
    public static TasksFragment getInstance(DisplayMode mode){
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

        if (savedInstanceState != null){
            mMode = DisplayMode.values()[savedInstanceState.getInt(ARG_MODE)];
        }
        else {
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

        //refreshList();
        mTasksProvider = new TasksProvider();
        setMode(mMode);

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
        mRecyclerView.addOnScrollListener(onScrollListener);

        return rootView;
    }

    public void setMode(DisplayMode mode){
        mMode = mode;
        switch (mMode){
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
                //TODO: pick date
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE,1);
                mTasksProvider.tasksForDate(cal);
                break;
            case WITHOUT_DATE:
                mTasksProvider.tasksWithNoDate();
                break;
        }
        progressView.setVisibility(View.VISIBLE);
        mTasksProvider.subscribe(cTasks -> {
            mTasks.clear();
            mTasks.addAll(cTasks);
            progressView.setVisibility(View.INVISIBLE);
            mAdapter.notifyDataSetChanged();
        });
    }

    private void refreshList(){
        if (mTasksSub != null && !mTasksSub.isUnsubscribed()){
            mTasksSub.unsubscribe();
        }
        mTasksSub = ConcreteTaskDAO.getDAO().getAll(true).subscribe(tasks -> {
            mTasks.clear();
            mTasks.addAll(tasks);
            progressView.setVisibility(View.INVISIBLE);
            mAdapter.notifyDataSetChanged();
        });
    }

    private void showTaskBottomDialog(ConcreteTask task) {
        mTargetTask = task;
        new BottomSheet.Builder(getActivity(), R.style.MyBottomSheetStyle)
                .setSheet(R.menu.menu_concrete_task_options)
                .setListener(mMenuOptionSelected)
                .setTitle(task.getTask().getName())
                .grid()
                .show();
    }


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

    private void registerProgress(){
        FragmentTransaction ft;
        DialogFragment fragment;
        Task.ProgressTrackMode mode = mTargetTask.getTask().getProgressTrackMode();
        switch (mode){
            case MARK:
                setTargetTaskCompleted();
                break;
            case LIST:
                ft = getActivity().getSupportFragmentManager().beginTransaction();
                fragment = ChecklistDialogFragment.getInstance(mTargetTask.getTask().getId(), null, false);
                fragment.show(ft, "dialog_checklist");
                break;
            case SEQUENCE:
                break;
            default:
                ft = getActivity().getSupportFragmentManager().beginTransaction();
                fragment = ProgressRegisterDialogFragment.newInstance(mTargetTask.getId());
                fragment.show(ft, "dialog_progress");
                break;
        }
    }

    public void onChecklistChanged(ArrayList<CheckListItem> list){
        long taskId = mTargetTask.getTask().getId();
        CheckListItemDAO.getDAO().replaceForTask(taskId, list)
                .subscribe(longs -> {
                    Toasty.success(getContext(),getString(R.string.progress_registered)).show();
                    mAdapter.notifyDataSetChanged();
                    //refreshList();
                });
    }

    private void setTargetTaskCompleted(){
        mTargetTask.setAmountDone(1);
        ConcreteTaskDAO.getDAO().update(mTargetTask).subscribe(integer -> {
            Toasty.success(getContext(),getString(R.string.progress_registered)).show();
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

    private void configureTouchListener(){
        mRecyclerTouchListener
        .setSwipeOptionViews(R.id.sw_action_delete, R.id.sw_action_reschedule)
        .setIndependentViews(R.id.la_timer_outer, R.id.la_progress_circle)
        .setSwipeable(R.id.la_row_fg, R.id.la_row_bg, (viewID, position) -> {
            if (viewID == R.id.sw_action_delete){
                Toast.makeText(getContext(),"DELETE",Toast.LENGTH_LONG).show();
            }
            else if (viewID == R.id.sw_action_reschedule){
                Toast.makeText(getContext(),"RESCHEDULE",Toast.LENGTH_LONG).show();
            }
        }).setClickable(new RecyclerTouchListener.OnRowClickListener() {
            @Override
            public void onRowClicked(int position) {
                showTaskBottomDialog(mTasks.get(position));
            }

            @Override
            public void onIndependentViewClicked(int independentViewID, int position) {
                switch (independentViewID){
                    case R.id.la_timer_outer:
                        break;
                    case R.id.la_progress_circle:
                        break;
                }
            }
        });
    }

    public void createTask() {
        editTask(0L);
    }

    public void editTask(long taskId){
        Intent intent = TaskEditActivity.getIntent(taskId, getActivity());
        startActivity(intent);
    }

    public interface ControlsVisibility {
        void showControls();
        void hideControls();
        boolean controlsAreShown();
    }

}
