package app.warinator.goalcontrol.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.model.Category;
import app.warinator.goalcontrol.model.Project;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.tasks.TasksFilter;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;


public class TaskFilterDialogFragment extends DialogFragment {
    @BindView(R.id.la_task)
    RelativeLayout laTask;
    @BindView(R.id.sp_task)
    Spinner spTask;
    @BindView(R.id.la_project)
    RelativeLayout laProject;
    @BindView(R.id.sp_project)
    Spinner spProject;
    @BindView(R.id.la_category)
    RelativeLayout laCategory;
    @BindView(R.id.sp_category)
    Spinner spCategory;
    @BindView(R.id.la_priority)
    RelativeLayout laPriority;
    @BindView(R.id.sp_priority)
    Spinner spPriority;

    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.btn_reset)
    Button btnReset;

    public TaskFilterDialogFragment() {}

    public static final String ARG_FILTER = "filter";
    public static TaskFilterDialogFragment getInstance(TasksFilter tasksFilter){
        TaskFilterDialogFragment fragment = new TaskFilterDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, tasksFilter);
        fragment.setArguments(args);
        return fragment;
    }

    private List<Task> mTasks;
    private List<Project> mProjects;
    private List<Category> mCategories;
    private CompositeSubscription mSub = new CompositeSubscription();
    private TasksFilter mTasksFilter;
    private OnFilterSetListener mListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_filter_dialog, container, false);
        ButterKnife.bind(this,v);
        tvDialogTitle.setText(R.string.filter);

        if (savedInstanceState != null){
            mTasksFilter = savedInstanceState.getParcelable(ARG_FILTER);
        }
        else {
            mTasksFilter = getArguments().getParcelable(ARG_FILTER);
        }

        mSub.add(TaskDAO.getDAO().getAll(false, false).subscribe(tasks -> {
            mTasks = tasks;
            String[] names = new String[tasks.size()+1];
            names[0] = getContext().getString(R.string.all);
            int sel = 0;
            for (int i=1; i<=tasks.size(); i++){
                names[i] = tasks.get(i-1).getName();
                if (mTasksFilter.getTaskId() == tasks.get(i-1).getId()){
                    sel = i;
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spTask.setAdapter(adapter);
            spTask.setSelection(sel);
        }));

        mSub.add(ProjectDAO.getDAO().getAll(false, false).subscribe(projects -> {
            mProjects = projects;
            String[] names = new String[projects.size()+1];
            names[0] = getContext().getString(R.string.all);
            int sel = 0;
            for (int i=1; i<=projects.size(); i++){
                names[i] = projects.get(i-1).getName();
                if (mTasksFilter.getProjectId() == projects.get(i-1).getId()){
                    sel = i;
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spProject.setAdapter(adapter);
            spProject.setSelection(sel);
        }));

        mSub.add(CategoryDAO.getDAO().getAll(false, false).subscribe(categories -> {
            mCategories = categories;
            String[] names = new String[categories.size()+1];
            names[0] = getContext().getString(R.string.all);
            int sel = 0;
            for (int i=1; i<=categories.size(); i++){
                names[i] = categories.get(i-1).getName();
                if (mTasksFilter.getCategoryId() == categories.get(i-1).getId()){
                    sel = i;
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spCategory.setAdapter(adapter);
            spCategory.setSelection(sel);
        }));


        String[] prio = getResources().getStringArray(R.array.priorities);
        String[] priorities = new String[prio.length + 1];
        priorities[0] = getContext().getString(R.string.all);
        System.arraycopy(prio, 0, priorities, 1, prio.length);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(adapter);
        if (mTasksFilter.getPriority() != TasksFilter.ALL){
            spPriority.setSelection(mTasksFilter.getPriority()+1);
        }

        spTask.setOnItemSelectedListener(mOnItemSelectedListener);
        spProject.setOnItemSelectedListener(mOnItemSelectedListener);
        spCategory.setOnItemSelectedListener(mOnItemSelectedListener);
        spPriority.setOnItemSelectedListener(mOnItemSelectedListener);

        laTask.setOnClickListener(mOnOptionClickListener);
        laProject.setOnClickListener(mOnOptionClickListener);
        laCategory.setOnClickListener(mOnOptionClickListener);
        laPriority.setOnClickListener(mOnOptionClickListener);

        btnCancel.setOnClickListener(v1 -> dismiss());
        btnOk.setOnClickListener(v1 -> {
            mListener.onFilterSet(mTasksFilter);
            dismiss();
        });
        btnReset.setOnClickListener(v1 -> {
            mTasksFilter = new TasksFilter();
            spTask.setSelection(0);
            spProject.setSelection(0);
            spCategory.setSelection(0);
            spPriority.setSelection(0);
        });
        return v;
    }

    private View.OnClickListener mOnOptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.la_task:
                    spTask.performClick();
                    break;
                case R.id.la_project:
                    spProject.performClick();
                    break;
                case R.id.la_category:
                    spCategory.performClick();
                    break;
                case R.id.la_priority:
                    spPriority.performClick();
                    break;
            }
        }
    };

   private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
       @Override
       public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
           switch (parent.getId()){
               case R.id.sp_task:
                   mTasksFilter.setTaskId(position > 0 ? mTasks.get(position-1).getId() : TasksFilter.ALL);
                   break;
               case R.id.sp_project:
                   mTasksFilter.setProjectId(position > 0 ? mProjects.get(position-1).getId() : TasksFilter.ALL);
                   break;
               case R.id.sp_category:
                   mTasksFilter.setCategoryId(position > 0 ? mCategories.get(position-1).getId() : TasksFilter.ALL);
                   break;
               case R.id.sp_priority:
                   mTasksFilter.setPriority(position > 0 ? position - 1 : TasksFilter.ALL);
                   break;
           }
       }

       @Override
       public void onNothingSelected(AdapterView<?> parent) {
       }
   };


    @Override
    public void onAttach(Context context) {
        if (context instanceof OnFilterSetListener) {
            mListener = (OnFilterSetListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " должен реализовывать " + OnFilterSetListener.class.getSimpleName());
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public interface OnFilterSetListener {
        void onFilterSet(TasksFilter tasksFilter);
    }

}
