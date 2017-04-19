package app.warinator.goalcontrol.fragment;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.Project;
import app.warinator.goalcontrol.model.main.Task;
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

    public TaskFilterDialogFragment() {}

    private List<Task> mTasks;
    private CompositeSubscription mSub = new CompositeSubscription();

    private List<Project> mProjects;
    private List<Category> mCategories;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_filter_dialog, container, false);
        ButterKnife.bind(this,v);
        tvDialogTitle.setText(R.string.filter);

        mSub.add(TaskDAO.getDAO().getAll(false).subscribe(tasks -> {
            mTasks = tasks;
            String[] names = new String[tasks.size()+1];
            names[0] = getContext().getString(R.string.all);
            for (int i=1; i<=tasks.size(); i++){
                names[i] = tasks.get(i-1).getName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spTask.setAdapter(adapter);
        }));

        mSub.add(ProjectDAO.getDAO().getAll(false).subscribe(projects -> {
            mProjects = projects;
            String[] names = new String[projects.size()+1];
            names[0] = getContext().getString(R.string.all);
            for (int i=1; i<=projects.size(); i++){
                names[i] = projects.get(i-1).getName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spProject.setAdapter(adapter);
        }));

        mSub.add(CategoryDAO.getDAO().getAll(false).subscribe(categories -> {
            mCategories = categories;
            String[] names = new String[categories.size()+1];
            names[0] = getContext().getString(R.string.all);
            for (int i=1; i<=categories.size(); i++){
                names[i] = categories.get(i-1).getName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spCategory.setAdapter(adapter);
        }));


        String[] prio = getResources().getStringArray(R.array.priorities);
        String[] priorities = new String[prio.length + 1];
        priorities[0] = getContext().getString(R.string.all);
        System.arraycopy(prio, 0, priorities, 1, prio.length);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(adapter);

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
            dismiss();
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
                   break;
               case R.id.sp_project:
                   break;
               case R.id.sp_category:
                   break;
               case R.id.sp_priority:
                   break;
           }
       }

       @Override
       public void onNothingSelected(AdapterView<?> parent) {
       }
   };

}
