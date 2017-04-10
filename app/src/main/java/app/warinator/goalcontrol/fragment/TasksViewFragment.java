package app.warinator.goalcontrol.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.activity.TaskEditActivity;
import app.warinator.goalcontrol.adapter.TasksAdapter;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import github.nisrulz.recyclerviewhelper.RVHItemClickListener;
import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Просмотр задач
 */
public class TasksViewFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private TasksAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;
    private ArrayList<ConcreteTask> mTasks;
    private CompositeSubscription mSub = new CompositeSubscription();
    private RecyclerTouchListener mRecyclerTouchListener;
    private ItemTouchHelper.Callback mitemTouchCallback;

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

    public TasksViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tasks_view, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_tasks);

        mRecyclerTouchListener = new RecyclerTouchListener(getActivity(), mRecyclerView);
        configureTouchListener();

        mTasks = new ArrayList<>();
        mAdapter = new TasksAdapter(mTasks, getContext(), new TasksAdapter.Callback() {
            @Override
            public void cancelDrag() {
            }
        });
        mSub.add(ConcreteTaskDAO.getDAO().getAll(false).subscribe(new Action1<List<ConcreteTask>>() {
            @Override
            public void call(List<ConcreteTask> tasks) {
                mTasks.addAll(tasks);
                mAdapter.notifyDataSetChanged();
            }
        }));

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

        mRecyclerView.addOnItemTouchListener(new RVHItemClickListener(getContext(), mOnTaskClick));
        return rootView;
    }

    private RVHItemClickListener.OnItemClickListener mOnTaskClick = new RVHItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            new BottomSheet.Builder(getActivity(), R.style.MyBottomSheetStyle)
                    .setSheet(R.menu.menu_task_options)
                    .setListener(mMenuOptionSelected)
                    .setTitle(mTasks.get(position).getTask().getName())
                    .grid()
                    .show();
        }
    };

    private BottomSheetListener mMenuOptionSelected = new BottomSheetListener() {
        @Override
        public void onSheetShown(@NonNull BottomSheet bottomSheet) {
        }

        @Override
        public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_task_edit:
                    break;
                case R.id.action_task_info:
                    break;
                case R.id.action_task_register_progress:
                    break;
            }
        }

        @Override
        public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @DismissEvent int i) {
        }
    };

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
        .setSwipeable(R.id.la_row_fg, R.id.la_row_bg, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
            @Override
            public void onSwipeOptionClicked(int viewID, int position) {
                if (viewID == R.id.sw_action_delete){
                    Toast.makeText(getContext(),"DELETE",Toast.LENGTH_LONG).show();
                }
                else if (viewID == R.id.sw_action_reschedule){
                    Toast.makeText(getContext(),"RESCHEDULE",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void createTask() {
        Intent intent = new Intent(getActivity(), TaskEditActivity.class);
        intent.putExtra(TaskEditActivity.ARG_TASK_ID, 0L);
        startActivity(intent);
    }

    public interface ControlsVisibility {
        void showControls();
        void hideControls();
        boolean controlsAreShown();
    }

}
