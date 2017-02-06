package app.warinator.goalcontrol;

import android.graphics.drawable.Drawable;
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

import app.warinator.goalcontrol.model.DummyTask;

public class TasksViewFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private TasksAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;

    public TasksViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tasks_view, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.rv_tasks);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        DummyTask tasks[] = new DummyTask[25];
        for (int i=0; i<tasks.length; i++){
            tasks[i] = new DummyTask();
        }
        mAdapter = new TasksAdapter(tasks);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(),R.drawable.line_divider));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        return rootView;
    }

}
