package app.warinator.goalcontrol;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

import app.warinator.goalcontrol.model.EditOption;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskEditActivity extends AppCompatActivity {

    @BindView(R.id.et_task_name)
    EditText etTaskName;
    private static final int[] mOptionLabels = {R.string.task_option_project, R.string.task_option_time, R.string.task_option_priority, R.string.task_option_category,
            R.string.task_option_progress, R.string.task_option_chrono, R.string.task_option_alarm, R.string.task_option_comment };
    @BindView(R.id.rv_task_edit_options)
    RecyclerView rvTaskEditOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_edit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.rv_task_edit_options);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        String[] icons =getResources().getStringArray(R.array.task_option_items);
        EditOption[] options = new EditOption[icons.length];
        for (int i = 0; i<icons.length; i++){
            String name = getString(mOptionLabels[i]);
            options[i] = new EditOption(mOptionLabels[i], name, icons[i]);
        }
        EditOptionsAdapter mAdapter = new EditOptionsAdapter(options, mEditOptionCallback);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(this,R.drawable.line_divider));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

    }


    private EditOptionsCallback mEditOptionCallback = new EditOptionsCallback() {
        @Override
        public void handleEditOptionClick(int pos, int optResId) {
            FragmentTransaction ft;
            switch (optResId){
                case R.string.task_option_project:
                    break;
                case R.string.task_option_time:
                    ft = getSupportFragmentManager().beginTransaction();
                    TaskTimingDialogFragment newTdFragment = TaskTimingDialogFragment.newInstance();
                    newTdFragment.show(ft, "dialog_deadline");
                    break;
                case R.string.task_option_progress:
                    ft = getSupportFragmentManager().beginTransaction();
                    TaskProgressConfDialogFragment newPcFragment = TaskProgressConfDialogFragment.newInstance();
                    newPcFragment.show(ft, "dialog_progress_conf");
                    break;
                case R.string.task_option_priority:
                    break;
                case R.string.task_option_chrono:
                    ft = getSupportFragmentManager().beginTransaction();
                    TaskChronoDialogFragment newTcDialogFragment = TaskChronoDialogFragment.newInstance();
                    newTcDialogFragment.show(ft, "dialog_chrono");
                    break;
                case R.string.task_option_category:
                    break;
                case R.string.task_option_alarm:
                    break;
                case R.string.task_option_comment:
                    break;
            }
        }
    };

}