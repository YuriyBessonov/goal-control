package app.warinator.goalcontrol.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import app.warinator.goalcontrol.MaterialDrawer;
import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.TasksComparator;
import app.warinator.goalcontrol.TasksFilter;
import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.database.DAO.TrackUnitDAO;
import app.warinator.goalcontrol.fragment.CategoriesDialogFragment;
import app.warinator.goalcontrol.fragment.ChecklistDialogFragment;
import app.warinator.goalcontrol.fragment.ProjectEditDialogFragment;
import app.warinator.goalcontrol.fragment.ProjectsDialogFragment;
import app.warinator.goalcontrol.fragment.TaskFilterDialogFragment;
import app.warinator.goalcontrol.fragment.TaskSortDialogFragment;
import app.warinator.goalcontrol.fragment.TasksFragment;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.CheckListItem;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Project;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.model.main.TrackUnit;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;

/**
 * Главная activity
 */
public class MainActivity extends AppCompatActivity implements
        //TasksFragment.ControlsVisibility,
        ProjectEditDialogFragment.OnProjectEditedListener,
        CategoriesDialogFragment.OnCategorySelectedListener,
        ProjectsDialogFragment.OnProjectPickedListener,
        ChecklistDialogFragment.OnChecklistChangedListener,
        TaskSortDialogFragment.OnSortCriteriaSetListener,
        TaskFilterDialogFragment.OnFilterSetListener{

    private static final String FRAGMENT_TASKS = "fragment_tasks";
    private static final String FRAGMENT_CATEGORY = "fragment_category";
    private static final String FRAGMENT_PROJECTS = "fragment_projects";
    private static final String DIALOG_DATE = "dialog_date";
    private static final String ARG_TASK_ID = "task_id";

    @BindView(R.id.controls_container)
    CardView cvContainer;
    @BindView(R.id.fragment_container)
    FrameLayout laFragmentContainer;
    @BindView(R.id.fragment_controls_container)
    FrameLayout laFragmentControlsContainer;
    @BindView(R.id.calendar_view)
    HorizontalCalendarView calendarView;
    FragmentManager mFragmentManager;
    private HorizontalCalendar mHorizontalCalendar;
    private Toolbar mToolbar;
    private String mCurrentFragment;
    private Menu mMenu;


    public static Intent getTaskOptionsIntent(Context context, long taskId){
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_TASK_ID, taskId);
        return intent;
    }

    private void showTaskOptions(long taskId){
        TasksFragment fragment = (TasksFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_TASKS);
        if (fragment != null){
            fragment.showTaskOptions(taskId);
        }
    }

    //Выбор из бокового меню
    private Drawer.OnDrawerItemClickListener mOnDrawerItemClickListener = (view, position, drawerItem) -> {
        int option = (int) drawerItem.getTag();
        mToolbar.setTitle(option);
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
            default:
                break;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(mToolbar);
        MaterialDrawer.build(this, mToolbar).setOnDrawerItemClickListener(mOnDrawerItemClickListener);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mFragmentManager = getSupportFragmentManager();

        if (laFragmentContainer != null) {
            if (savedInstanceState != null) {
                return;
            }
            showTasks(TasksFragment.DisplayMode.QUEUED);
            mToolbar.setTitle(R.string.drawer_item_task_current);
        }

         /*
        if (laFragmentControlsContainer != null) {
            if (savedInstanceState != null) {
                return;
            }

            TimerControlsFragment fragment = new TimerControlsFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_controls_container, fragment).commit();
        }
        */

        Toast.makeText(this, "UNDER CONSTRUCTION", Toast.LENGTH_SHORT).show();
       // FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //TaskSortDialogFragment f = new TaskSortDialogFragment();
        //TaskFilterDialogFragment f = new TaskFilterDialogFragment();
       // f.show(ft,"sort");

        if (getIntent() != null){
            long taskId = getIntent().getLongExtra(ARG_TASK_ID,0);
            if (taskId > 0){
                showTaskOptions(taskId);
            }
        }

    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //TODO: избавиться
    private void dummyStuff(){
    }

    //TODO: избавиться
    private void dbStuff() {
        ProjectDAO.getDAO().add(new Project(0, "Имбирь", null, 0, 0, 0)).toBlocking().single();
        ProjectDAO.getDAO().add(new Project(0, "Сахар", null, 1, 0, 0)).toBlocking().single();
        ProjectDAO.getDAO().add(new Project(0, "Соль", null, 2, 0, 0)).toBlocking().single();
        ProjectDAO.getDAO().add(new Project(0, "Перец", null, 3, 0, 0)).toBlocking().single();
        ProjectDAO.getDAO().add(new Project(0, "Корица", null, 4, 0, 0)).toBlocking().single();

        CategoryDAO.getDAO().add(new Category(0, "Мята", 0)).toBlocking().single();
        CategoryDAO.getDAO().add(new Category(0, "Ромашка", 2)).toBlocking().single();
        CategoryDAO.getDAO().add(new Category(0, "Зверобой", 4)).toBlocking().single();
        CategoryDAO.getDAO().add(new Category(0, "Крапива", 6)).toBlocking().single();
        CategoryDAO.getDAO().add(new Category(0, "Мандрагора", 8)).toBlocking().single();


        TrackUnitDAO.getDAO().add(new TrackUnit(0, "страницы", "стр.")).toBlocking().single();
        Task task = new Task();
        task.setName("Задача раз");
        Project p = new Project();
        p.setId(1);
        task.setProject(p);
        task.setPriority(Task.Priority.MINOR);
        Category c = new Category();
        c.setId(1);
        task.setCategory(c);
        Calendar cal = Calendar.getInstance();
        cal.set(2015, 7, 11, 8, 0);
        task.setReminder(cal);
        task.setBeginDate(cal);
        task.setNote("Примечание к задаче раз");
        task.setIcon(7);
        task.setRepeatable(true);
        task.setWithTime(true);
        task.setProgressTrackMode(Task.ProgressTrackMode.UNITS);
        TrackUnit units = new TrackUnit();
        units.setId(1);
        task.setUnits(units);
        task.setAmountOnce(17);
        task.setAmountTotal(234);
        task.setChronoTrackMode(Task.ChronoTrackMode.INTERVAL);
        task.setWorkTime(1000 * 60 * 32);
        task.setSmallBreakTime(1000 * 60 * 8);
        task.setBigBreakTime(1000 * 60 * 16);
        task.setIntervalsCount(6);
        task.setBigBreakEvery(3);
        TaskDAO.getDAO().add(task).toBlocking().single();

        task = new Task();
        task.setName("Задача с очень длинным именем. Даже слишком.");
        p = new Project();
        p.setId(2);
        task.setProject(p);
        task.setPriority(Task.Priority.MEDIUM);
        c = new Category();
        c.setId(2);
        task.setCategory(c);
        cal = Calendar.getInstance();
        cal.set(2015, 7, 11, 13, 31);
        task.setReminder(cal);
        task.setBeginDate(cal);
        task.setIcon(14);
        task.setRepeatable(false);
        task.setWithTime(true);
        task.setProgressTrackMode(Task.ProgressTrackMode.PERCENT);
        task.setAmountOnce(25);
        task.setAmountTotal(136);
        task.setChronoTrackMode(Task.ChronoTrackMode.DIRECT);
        TaskDAO.getDAO().add(task).toBlocking().single();


        task = new Task();
        task.setName("Задача два");
        p = new Project();
        p.setId(3);
        task.setProject(p);
        task.setPriority(Task.Priority.HIGH);
        c = new Category();
        c.setId(3);
        task.setCategory(c);
        task.setNote("Примечание к задаче два");
        task.setIcon(11);
        cal.set(2019, 2, 22, 05, 50);
        task.setBeginDate(cal);
        task.setRepeatable(true);
        task.setWithTime(false);
        task.setProgressTrackMode(Task.ProgressTrackMode.UNITS);
        task.setAmountOnce(44);
        task.setAmountTotal(444);
        task.setChronoTrackMode(Task.ChronoTrackMode.COUNTDOWN);
        task.setWorkTime(1000 * 60 * 32);
        TaskDAO.getDAO().add(task).toBlocking().single();


        task = new Task();
        task.setName("Задача три");
        p = new Project();
        p.setId(4);
        task.setProject(p);
        task.setPriority(Task.Priority.CRITICAL);
        task.setIcon(33);
        task.setRepeatable(false);
        task.setWithTime(false);
        task.setProgressTrackMode(Task.ProgressTrackMode.MARK);
        task.setChronoTrackMode(Task.ChronoTrackMode.NONE);
        TaskDAO.getDAO().add(task).toBlocking().single();


        task = new Task();
        task.setName("Задача никакая");
        task.setPriority(Task.Priority.LOW);
        task.setIcon(21);
        task.setRepeatable(false);
        task.setWithTime(false);
        task.setProgressTrackMode(Task.ProgressTrackMode.MARK);
        task.setChronoTrackMode(Task.ChronoTrackMode.NONE);
        TaskDAO.getDAO().add(task).toBlocking().single();

        Task t = new Task();
        t.setId(1);
        cal = Calendar.getInstance();
        cal.set(2017, 7, 9, 17, 21);
        ConcreteTask ct = new ConcreteTask(0, t, cal, 0, 15, 60 * 1000 * 93, false);
        ConcreteTaskDAO.getDAO().add(ct).toBlocking().single();

        t = new Task();
        t.setId(2);
        cal = Calendar.getInstance();
        cal.set(2016, 4, 11, 8, 00);
        ct = new ConcreteTask(0, t, cal, 0, 0, 60 * 1000 * 12, false);
        ConcreteTaskDAO.getDAO().add(ct).toBlocking().single();

        t = new Task();
        t.setId(3);
        cal = Calendar.getInstance();
        cal.set(2017, 6, 24, 23, 59);
        ct = new ConcreteTask(0, t, cal, 0, 0, 60 * 1000 * 6, false);
        ConcreteTaskDAO.getDAO().add(ct).toBlocking().single();

        t = new Task();
        t.setId(4);
        cal = Calendar.getInstance();
        cal.set(2017, 6, 26, 11, 11);
        ct = new ConcreteTask(0, t, cal, 0, 0, 60 * 1000 * 42, false);
        ConcreteTaskDAO.getDAO().add(ct).toBlocking().single();


        t = new Task();
        t.setId(5);
        ct = new ConcreteTask(0, t, null, 0, 0, 0, false);
        ConcreteTaskDAO.getDAO().add(ct).subscribe();
    }

    //Заменить текущий фрагмент в контейнере
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
                if (fragmentTasks != null){
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    TaskSortDialogFragment f = TaskSortDialogFragment.getInstance(fragmentTasks.getSortCriteria());
                    f.show(ft,"sort");
                }
                break;
            case R.id.action_filter:
                fragmentTasks = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
                if (fragmentTasks != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    TaskFilterDialogFragment f = TaskFilterDialogFragment.getInstance(fragmentTasks.getFilter());
                    f.show(ft, "filter");
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
        return super.onCreateOptionsMenu(menu);
    }

    //Перейти к редактированию категорий
    private void showCategories() {
        if (mMenu != null){
            mMenu.findItem(R.id.action_sort).setVisible(false);
            mMenu.findItem(R.id.action_filter).setVisible(false);
            mMenu.findItem(R.id.action_add).setVisible(true);
        }
        CategoriesDialogFragment fragment = CategoriesDialogFragment.newInstance();
        setMainFragment(fragment, FRAGMENT_CATEGORY);
        //hideControls();
    }

    //Перейти к редактированию проектов
    private void showProjects() {
        if (mMenu != null){
            mMenu.findItem(R.id.action_sort).setVisible(false);
            mMenu.findItem(R.id.action_filter).setVisible(false);
            mMenu.findItem(R.id.action_add).setVisible(false);
        }
        ProjectsDialogFragment fragment = ProjectsDialogFragment.newInstance();
        setMainFragment(fragment, FRAGMENT_PROJECTS);
        //hideControls();
    }

    //Отобразить задачи в заданном режиме
    private void showTasks(TasksFragment.DisplayMode mode) {
        if (mMenu != null){
            mMenu.findItem(R.id.action_sort).setVisible(true);
            mMenu.findItem(R.id.action_filter).setVisible(true);
            mMenu.findItem(R.id.action_add).setVisible(true);
        }
        TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
        if (fragment == null) {
            fragment = TasksFragment.getInstance(mode);
            setMainFragment(fragment, FRAGMENT_TASKS);
            //mToolbar.setTitle(R.string.drawer_item_task_current);
        } else {
            fragment.setMode(mode);
        }
        if (mode == TasksFragment.DisplayMode.DATE){
            pickDate();
            if (mMenu != null){
                mMenu.findItem(R.id.action_pick_date).setVisible(true);
            }
        }
        else if (mMenu != null){
                mMenu.findItem(R.id.action_pick_date).setVisible(false);
            }
        /*
        if (mode == TasksFragment.DisplayMode.QUEUED){
            showControls();
        }
        else {
            hideControls();
        }
        */
    }

    //TODO: убрать или заменить горизонтальный календарь
    private void setupCalendarView(Calendar date) {
        if (mHorizontalCalendar != null) {
            calendarView.setOnFlingListener(null);
        }
        Calendar d1 = Calendar.getInstance();
        Calendar d2 = Calendar.getInstance();
        d1.setTimeInMillis(date.getTimeInMillis());
        d2.setTimeInMillis(date.getTimeInMillis());
        d1.add(Calendar.MONTH, -1);
        d2.add(Calendar.MONTH, 1);

        mHorizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendar_view)
                .startDate(d1.getTime())
                .endDate(d2.getTime())
                .centerToday(false)
                .build();
        mHorizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {
                //setDisplayingDate(date);
            }
        });
        calendarView.setVisibility(View.VISIBLE);
        mHorizontalCalendar.selectDate(date.getTime(), true);
    }

    private void setDisplayingDate(Calendar date){
        TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
        if (fragment != null){
            fragment.setDisplayedDate(date);
        }
        mToolbar.setTitle(Util.getFormattedDate(date, this));
    }

    //Выбрать дату
    private void pickDate() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, monthOfYear, dayOfMonth);
                    setDisplayingDate(cal);
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getFragmentManager(), DIALOG_DATE);
    }

    private class Callbacks
    {

    }

    @Override
    public void onSortCriteriaSet(ArrayList<TasksComparator.SortCriterion> criteria) {
        TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
        if (fragment != null){
            fragment.setSortCriteria(criteria);
        }
    }

    @Override
    public void onFilterSet(TasksFilter tasksFilter) {
        TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
        if (fragment != null){
            fragment.setFilter(tasksFilter);
        }
    }

    @Override
    public void onProjectEdited(Project project) {
        ProjectsDialogFragment fragment = (ProjectsDialogFragment) mFragmentManager.findFragmentByTag(FRAGMENT_PROJECTS);
        fragment.onProjectEdited(project);
    }

    @Override
    public void onCategorySelected(Category category) {
        ProjectsDialogFragment fragment = (ProjectsDialogFragment) mFragmentManager.findFragmentByTag(FRAGMENT_PROJECTS);
        fragment.onCategoryPicked(category);
    }

    @Override
    public void onProjectPicked(Project parent) {
        ProjectsDialogFragment fragment = (ProjectsDialogFragment) mFragmentManager.findFragmentByTag(FRAGMENT_PROJECTS);
        fragment.onParentPicked(parent);
    }

    @Override
    public void onCheckListChanged(ArrayList<CheckListItem> list) {
    }

    @Override
    public void onCheckListEditDone(ArrayList<CheckListItem> list, boolean cancelled) {
        if (!cancelled) {
            TasksFragment fragment = (TasksFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
            fragment.onChecklistChanged(list);
        }
    }

    @Override
    protected void onDestroy() {
        Log.v("THE_TIMER", "ACTIVITY DESTROYED");
        super.onDestroy();
    }

/*
    @Override
    public boolean controlsAreShown() {
        return cvContainer.getVisibility() == View.VISIBLE;
    }

    //Показать фрагмент управления задачами
    @Override
    public void showControls() {
        cvContainer.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                cvContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    //Скрыть фрагмент управления задачами
    @Override
    public void hideControls() {
        cvContainer.animate().translationY(cvContainer.getHeight()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cvContainer.setVisibility(View.INVISIBLE);
            }
        });
    }
    */
}
