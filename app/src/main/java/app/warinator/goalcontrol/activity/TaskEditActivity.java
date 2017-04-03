package app.warinator.goalcontrol.activity;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mikepenz.iconics.view.IconicsImageView;

import app.warinator.goalcontrol.EditOptionsCallback;
import app.warinator.goalcontrol.NotesEditDialogFragment;
import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.EditOptionsAdapter;
import app.warinator.goalcontrol.adapter.PrioritiesRecyclerViewAdapter;
import app.warinator.goalcontrol.fragment.CategoriesDialogFragment;
import app.warinator.goalcontrol.fragment.IconPickerDialogFragment;
import app.warinator.goalcontrol.fragment.PriorityDialogFragment;
import app.warinator.goalcontrol.fragment.TaskChronoDialogFragment;
import app.warinator.goalcontrol.fragment.TaskProgressConfDialogFragment;
import app.warinator.goalcontrol.fragment.TaskTimingDialogFragment;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.misc.EditOption;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Редактирование задачи
 */
public class TaskEditActivity extends AppCompatActivity implements
        IconPickerDialogFragment.IconPickedCallback,
        CategoriesDialogFragment.CategorySelectedCallback,
        NotesEditDialogFragment.OnNoteEditedCallback,
        PriorityDialogFragment.PrioritySelectedCallback
{

    private static final int[] mOptionLabels = {R.string.task_option_project, R.string.task_option_time, R.string.task_option_priority, R.string.task_option_category,
            R.string.task_option_progress, R.string.task_option_chrono, R.string.task_option_alarm, R.string.task_option_comment};
    @BindView(R.id.et_name)
    EditText etTaskName;
    @BindView(R.id.la_task_icon)
    FrameLayout laTaskIcon;
    @BindView(R.id.iiv_task_icon)
    IconicsImageView iivTaskIcon;
    @BindView(R.id.rv_task_edit_options)
    RecyclerView rvTaskEditOptions;
    private EditOption[] mOptions;
    private EditOptionsAdapter mAdapter;

    //Выбор пункта настроек
    private EditOptionsCallback mEditOptionCallback = new EditOptionsCallback() {
        @Override
        public void handleEditOptionClick(int pos, int optResId) {
            FragmentTransaction ft;
            DialogFragment fragment;
            switch (optResId) {
                case R.string.task_option_project:
                    break;
                case R.string.task_option_time:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskTimingDialogFragment.newInstance();
                    fragment.show(ft, "dialog_deadline");
                    break;
                case R.string.task_option_progress:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskProgressConfDialogFragment.newInstance();
                    fragment.show(ft, "dialog_progress_conf");
                    break;
                case R.string.task_option_priority:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = PriorityDialogFragment.newInstance();
                    fragment.show(ft, "dialog_priority");
                    break;
                case R.string.task_option_chrono:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = TaskChronoDialogFragment.newInstance();
                    fragment.show(ft, "dialog_chrono");
                    break;
                case R.string.task_option_category:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = CategoriesDialogFragment.newInstance();
                    fragment.show(ft, "dialog_edit_category");
                    break;
                case R.string.task_option_alarm:
                    break;
                case R.string.task_option_comment:
                    ft = getSupportFragmentManager().beginTransaction();
                    fragment = NotesEditDialogFragment.newInstance("Sample text");
                    fragment.show(ft, "dialog_note");
                    break;
            }
        }
    };
    //Выбор иконки
    private View.OnClickListener onTaskIconClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            IconPickerDialogFragment fragment = IconPickerDialogFragment.newInstance();
            fragment.show(ft, "dialog_icon_picker");
        }
    };

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
        String[] icons = getResources().getStringArray(R.array.task_option_items);
        mOptions = new EditOption[icons.length];
        for (int i = 0; i < icons.length; i++) {
            String name = getString(mOptionLabels[i]);
            mOptions[i] = new EditOption(mOptionLabels[i], name, icons[i]);
            if (mOptionLabels[i] == R.string.task_option_progress ||
                    mOptionLabels[i] == R.string.task_option_category ||
                    mOptionLabels[i] == R.string.task_option_priority ||
                    mOptionLabels[i] == R.string.task_option_project) {
                mOptions[i].setSwitcheable(false);
            } else {
                mOptions[i].setSwitcheable(true);
            }
        }
        mAdapter = new EditOptionsAdapter(mOptions, mEditOptionCallback);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mDividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        laTaskIcon.setOnClickListener(onTaskIconClick);
    }




    @Override
    public void onIconPicked(String icon) {
        iivTaskIcon.setIcon(icon);
    }

    @Override
    public void onCategorySelected(Category category) {
        Toast.makeText(this, "Выбрана категория "+category.getName(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNoteEdited(String note) {
        setOptionInfo(R.string.task_option_comment, note);
    }

    private void setOptionInfo(int labelId, String info){
        for (int i=0; i < mOptionLabels.length; i++){
            if (mOptionLabels[i] == labelId){
                mOptions[i].setInfo(info);
                mAdapter.notifyItemChanged(i);
            }
        }
    }

    @Override
    public void onPrioritySelected(int pos) {
        setOptionInfo(R.string.task_option_priority,
                getResources().getStringArray(R.array.priorities)[pos]);
    }
}
