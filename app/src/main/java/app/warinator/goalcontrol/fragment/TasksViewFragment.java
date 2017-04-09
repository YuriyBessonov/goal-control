package app.warinator.goalcontrol.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nikhilpanju.recyclerviewenhanced.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.TasksAdapter;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.main.ConcreteTask;
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
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerTouchListener = new RecyclerTouchListener(getActivity(), mRecyclerView);
        configureTouchListener();

        mTasks = new ArrayList<>();
        mAdapter = new TasksAdapter(mTasks, getContext());
        mSub.add(ConcreteTaskDAO.getDAO().getAll(false).subscribe(new Action1<List<ConcreteTask>>() {
            @Override
            public void call(List<ConcreteTask> tasks) {
                mTasks.addAll(tasks);
                mAdapter.notifyDataSetChanged();
            }
        }));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider_dark));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mRecyclerView.addOnScrollListener(onScrollListener);
        return rootView;
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
        .setSwipeOptionViews(R.id.add)
        .setSwipeable(R.id.la_row_fg, R.id.la_row_bg, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
            @Override
            public void onSwipeOptionClicked(int viewID, int position) {
                if (viewID == R.id.add){
                    Toast.makeText(getContext(),"Swipe option click",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public interface ControlsVisibility {
        void showControls();
        void hideControls();
        boolean controlsAreShown();
    }
}
