package app.warinator.goalcontrol.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.mikepenz.materialdrawer.Drawer;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.fragment.CategoriesDialogFragment;
import app.warinator.goalcontrol.fragment.ChecklistDialogFragment;
import app.warinator.goalcontrol.fragment.ProgressRegisterDialogFragment;
import app.warinator.goalcontrol.fragment.ProjectEditDialogFragment;
import app.warinator.goalcontrol.fragment.ProjectsDialogFragment;
import app.warinator.goalcontrol.fragment.TaskFilterDialogFragment;
import app.warinator.goalcontrol.fragment.TaskSortDialogFragment;
import app.warinator.goalcontrol.fragment.TasksFragment;
import app.warinator.goalcontrol.model.CheckListItem;
import app.warinator.goalcontrol.model.Project;
import app.warinator.goalcontrol.tasks.TasksComparator;
import app.warinator.goalcontrol.tasks.TasksFilter;
import app.warinator.goalcontrol.ui_components.MaterialDrawer;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Главная активность
 */
public class MainActivity extends AppCompatActivity implements
        ProjectEditDialogFragment.OnProjectEditedListener,
        ProjectsDialogFragment.OnProjectPickedListener,
        ChecklistDialogFragment.OnChecklistChangedListener,
        TaskSortDialogFragment.OnSortCriteriaSetListener,
        TaskFilterDialogFragment.OnFilterSetListener,
        ProgressRegisterDialogFragment.OnCustomProgressSetListener {

    private static final String FRAGMENT_TASKS = "fragment_tasks";
    private static final String FRAGMENT_CATEGORY = "fragment_category";
    private static final String FRAGMENT_PROJECTS = "fragment_projects";
    private static final String FRAGMENT_SORT = "fragment_sort";
    private static final String FRAGMENT_FILTER = "fragment_filter";
    private static final String DIALOG_DATE = "dialog_date";
    private static final String ARG_TASK_ID = "task_id";
    private static final String STATE_SELECTED_RES = "selected_res";
    private static final String STATE_DATE = "date";
    private static final String STATE_DRAWER_POS = "drawer_pos";
    private static final int DEFAULT_POSITION = 1;

    @BindView(R.id.fragment_container)
    FrameLayout laFragmentContainer;

    private FragmentManager mFragmentManager;
    private Toolbar mToolbar;
    private String mCurrentFragment;
    private Menu mMenu;
    private Calendar mDate;
    private int mSelectedRes;
    private Drawer mDrawer;

    //выбор из бокового меню
    private Drawer.OnDrawerItemClickListener mOnDrawerItemClickListener = (view, position, drawerItem) -> {
        int option = (int) drawerItem.getTag();
        if (option != R.string.drawer_item_main_statistics && option != R.string.drawer_item_aux_about &&
                option != R.string.drawer_item_aux_help) {
            mToolbar.setTitle(option);
        }
        mSelectedRes = option;
        switch (option) {
            case R.string.drawer_item_main_categories:
                showCategories();
                break;
            case R.string.drawer_item_main_projects_and_tasks:
                showProjects();
                break;
            case R.string.drawer_item_task_current:
                showTasks(TasksFragment.DisplayMode.QUEUED);
                break;
            case R.string.drawer_item_task_today:
                showTasks(TasksFragment.DisplayMode.TODAY);
                break;
            case R.string.drawer_item_task_week:
                showTasks(TasksFragment.DisplayMode.WEEK);
                break;
            case R.string.drawer_item_task_date:
                showTasks(TasksFragment.DisplayMode.DATE);
                break;
            case R.string.drawer_item_task_no_date:
                showTasks(TasksFragment.DisplayMode.WITHOUT_DATE);
                break;
            case R.string.drawer_item_main_statistics:
                Intent intent = new Intent(this, StatisticsActivity.class);
                startActivity(intent);
                break;
            case R.string.drawer_item_aux_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.string.drawer_item_aux_help:
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
                break;
            default:
                break;
        }
        return false;
    };

    //получить намерение запуска активности
    public static Intent getTaskOptionsIntent(Context context, long taskId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_TASK_ID, taskId);
        return intent;
    }

    //показать опции задачи
    private void showTaskOptions(long taskId) {
        TasksFragment fragment = (TasksFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TASKS);
        if (fragment != null) {
            fragment.showTaskOptions(taskId);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(mToolbar);
        mDrawer = MaterialDrawer.build(this, mToolbar);
        mDrawer.setOnDrawerItemClickListener(mOnDrawerItemClickListener);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mFragmentManager = getSupportFragmentManager();
        mDate = Calendar.getInstance();

        if (savedInstanceState != null) {
            mSelectedRes = savedInstanceState.getInt(STATE_SELECTED_RES);
            mDate.setTimeInMillis(savedInstanceState.getLong(STATE_DATE));
            mDrawer.setSelectionAtPosition(savedInstanceState.getInt(STATE_DRAWER_POS), false);

            if (mSelectedRes == R.string.drawer_item_main_projects_and_tasks) {
                if (mFragmentManager.findFragmentByTag(FRAGMENT_PROJECTS) == null) {
                    showProjects();
                }
                setMenuItemsVisibility(false, false, false, false);
                mCurrentFragment = FRAGMENT_PROJECTS;
            } else if (mSelectedRes == R.string.drawer_item_main_categories) {
                if (mFragmentManager.findFragmentByTag(FRAGMENT_CATEGORY) == null) {
                    showCategories();
                }
                setMenuItemsVisibility(true, false, false, false);
                mCurrentFragment = FRAGMENT_CATEGORY;
            } else {
                if (mSelectedRes == R.string.drawer_item_task_date) {
                    setMenuItemsVisibility(true, true, true, true);
                } else {
                    setMenuItemsVisibility(true, true, true, false);
                }
                mCurrentFragment = FRAGMENT_TASKS;
                if (mFragmentManager.findFragmentByTag(FRAGMENT_TASKS) == null) {
                    switch (mSelectedRes) {
                        case R.string.drawer_item_task_current:
                            showTasks(TasksFragment.DisplayMode.QUEUED);
                            break;
                        case R.string.drawer_item_task_today:
                            showTasks(TasksFragment.DisplayMode.TODAY);
                            break;
                        case R.string.drawer_item_task_week:
                            showTasks(TasksFragment.DisplayMode.WEEK);
                            break;
                        case R.string.drawer_item_task_date:
                            setDisplayingDate(mDate);
                            break;
                        case R.string.drawer_item_task_no_date:
                            showTasks(TasksFragment.DisplayMode.WITHOUT_DATE);
                            break;
                    }
                }
            }
            if (mSelectedRes != R.string.drawer_item_task_date) {
                mToolbar.setTitle(mSelectedRes);
            } else {
                setDisplayingDate(mDate);
            }
        } else {
            showTasks(TasksFragment.DisplayMode.QUEUED);
            mSelectedRes = R.string.drawer_item_task_current;
            mToolbar.setTitle(mSelectedRes);
            mDrawer.setSelectionAtPosition(DEFAULT_POSITION, false);
            mCurrentFragment = FRAGMENT_TASKS;

            if (getIntent() != null) {
                long taskId = getIntent().getLongExtra(ARG_TASK_ID, 0);
                if (taskId > 0) {
                    showTaskOptions(taskId);
                }
            }
        }
    }

    //заменить текущий фрагмент в контейнере
    private void setMainFragment(Fragment fragment, String tag) {
        if (mFragmentManager.findFragmentByTag(tag) == null) {
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag).commit();
            mFragmentManager.executePendingTransactions();
        }
        mCurrentFragment = tag;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment;
        TasksFragment fragmentTasks;
        switch (id) {
            case R.id.action_add:
                if (mCurrentFragment.equals(FRAGMENT_CATEGORY)) {
                    fragment = mFragmentManager.findFragmentByTag(FRAGMENT_CATEGORY);
                    ((CategoriesDialogFragment) fragment).createItem();
                } else if (mCurrentFragment.equals(FRAGMENT_TASKS)) {
                    fragment = mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
                    ((TasksFragment) fragment).createTask();
                }

                break;
            case R.id.action_pick_date:
                pickDate();
                break;
            case R.id.action_sort:
                fragmentTasks = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
                if (fragmentTasks != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    TaskSortDialogFragment f = TaskSortDialogFragment.getInstance(fragmentTasks.getSortCriteria());
                    f.show(ft, FRAGMENT_SORT);
                }
                break;
            case R.id.action_filter:
                fragmentTasks = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
                if (fragmentTasks != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    TaskFilterDialogFragment f = TaskFilterDialogFragment.getInstance(fragmentTasks.getFilter());
                    f.show(ft, FRAGMENT_FILTER);
                }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        mMenu = menu;
        if (mSelectedRes > 0) {
            if (mSelectedRes == R.string.drawer_item_main_categories) {
                setMenuItemsVisibility(true, false, false, false);
            } else if (mSelectedRes == R.string.drawer_item_main_projects_and_tasks) {
                setMenuItemsVisibility(false, false, false, false);
            } else if (mSelectedRes == R.string.drawer_item_task_date) {
                setMenuItemsVisibility(true, true, true, true);
            } else {
                setMenuItemsVisibility(true, true, true, false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    //перейти к редактированию категорий
    private void showCategories() {
        setMenuItemsVisibility(true, false, false, false);
        CategoriesDialogFragment fragment = CategoriesDialogFragment.newInstance();
        setMainFragment(fragment, FRAGMENT_CATEGORY);
    }

    //перейти к редактированию проектов
    private void showProjects() {
        setMenuItemsVisibility(false, false, false, false);
        ProjectsDialogFragment fragment = ProjectsDialogFragment.newInstance();
        setMainFragment(fragment, FRAGMENT_PROJECTS);
    }

    //отобразить задачи в заданном режиме
    private void showTasks(TasksFragment.DisplayMode mode) {
        TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
        if (fragment == null) {
            fragment = TasksFragment.getInstance(mode);
            setMainFragment(fragment, FRAGMENT_TASKS);
        } else {
            fragment.setMode(mode);
        }
        if (mode == TasksFragment.DisplayMode.DATE) {
            pickDate();
            setMenuItemsVisibility(true, true, true, true);
        } else {
            setMenuItemsVisibility(true, true, true, false);
        }
    }

    //установить дату отображаемых задач
    private void setDisplayingDate(Calendar date) {
        TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
        if (fragment != null) {
            fragment.setDisplayedDate(date);
        }
        mToolbar.setTitle(Util.getFormattedDate(date, this, false));
    }

    //выбрать дату
    private void pickDate() {

        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    mDate.set(year, monthOfYear, dayOfMonth);
                    if (Util.compareDays(mDate, Calendar.getInstance()) == 0) {
                        int todayPos = MaterialDrawer.getItemPosition(mDrawer, R.string.drawer_item_task_today);
                        mDrawer.setSelectionAtPosition(todayPos, true);
                    } else {
                        setDisplayingDate(mDate);
                    }
                },
                mDate.get(Calendar.YEAR),
                mDate.get(Calendar.MONTH),
                mDate.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setOnCancelListener(dialog -> mDrawer.setSelectionAtPosition(MaterialDrawer.
                getItemPosition(mDrawer, R.string.drawer_item_task_today)));
        dpd.show(getFragmentManager(), DIALOG_DATE);
    }

    //задать видимость пунктов меню
    private void setMenuItemsVisibility(boolean add, boolean sort, boolean filter, boolean date) {
        if (mMenu != null) {
            mMenu.findItem(R.id.action_add).setVisible(add);
            mMenu.findItem(R.id.action_sort).setVisible(sort);
            mMenu.findItem(R.id.action_filter).setVisible(filter);
            mMenu.findItem(R.id.action_pick_date).setVisible(date);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedState) {
        savedState.putInt(STATE_SELECTED_RES, mSelectedRes);
        savedState.putLong(STATE_DATE, mDate.getTimeInMillis());
        savedState.putInt(STATE_DRAWER_POS, mDrawer.getCurrentSelectedPosition());
        super.onSaveInstanceState(savedState);
    }


    //обработка задания критерия сортировки
    @Override
    public void onSortCriteriaSet(ArrayList<TasksComparator.SortCriterion> criteria) {
        TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
        if (fragment != null) {
            fragment.setSortCriteria(criteria);
        }
    }

    //обработка задания фильтра
    @Override
    public void onFilterSet(TasksFilter tasksFilter) {
        TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
        if (fragment != null) {
            fragment.setFilter(tasksFilter);
        }
    }

    //обработка редактирования проекта
    @Override
    public void onProjectEdited(Project project) {
        ProjectsDialogFragment fragment = (ProjectsDialogFragment) mFragmentManager.findFragmentByTag(FRAGMENT_PROJECTS);
        fragment.onProjectEdited(project);
    }

    //обработка выбора проекта
    @Override
    public void onProjectPicked(Project parent) {
        ProjectsDialogFragment fragment = (ProjectsDialogFragment) mFragmentManager.findFragmentByTag(FRAGMENT_PROJECTS);
        fragment.onParentPicked(parent);
    }

    @Override
    public void onCheckListChanged(ArrayList<CheckListItem> list) {
    }

    //обработка завершения редактирования списка дел
    @Override
    public void onCheckListEditDone(ArrayList<CheckListItem> list, boolean cancelled, int checkedDiff) {
        if (!cancelled) {
            TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
            fragment.onChecklistChanged(list, checkedDiff);
        }
    }

    @Override
    public void onCustomProgressSet(long amtDone) {
        TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
        fragment.onCustomProgressSet(amtDone);
    }
}
