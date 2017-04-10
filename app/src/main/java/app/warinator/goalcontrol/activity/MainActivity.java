package app.warinator.goalcontrol.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.Calendar;

import app.warinator.goalcontrol.MaterialDrawer;
import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.fragment.CategoriesDialogFragment;
import app.warinator.goalcontrol.fragment.ProjectEditDialogFragment;
import app.warinator.goalcontrol.fragment.ProjectsDialogFragment;
import app.warinator.goalcontrol.fragment.TasksFragment;
import app.warinator.goalcontrol.fragment.TimerControlsFragment;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.ConcreteTask;
import app.warinator.goalcontrol.model.main.Project;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.model.main.TrackUnit;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Главная activity
 */
public class MainActivity extends AppCompatActivity
        implements TasksFragment.ControlsVisibility,
        ProjectEditDialogFragment.OnProjectEditedListener,
        CategoriesDialogFragment.OnCategorySelectedListener,
        ProjectsDialogFragment.OnProjectPickedListener {
    private static final String FRAGMENT_TASKS = "fragment_tasks";
    private static final String FRAGMENT_CATEGORY = "fragment_category";
    private static final String FRAGMENT_PROJECTS = "fragment_projects";
    @BindView(R.id.controls_container)
    CardView cvContainer;

    @BindView(R.id.fragment_container)
    FrameLayout laFragmentContainer;
    @BindView(R.id.fragment_controls_container)
    FrameLayout laFragmentControlsContainer;
    FragmentManager mFragmentManager;
    private Toolbar mToolbar;
    private String mCurrentFragment;

    //Выбор из бокового меню
    private Drawer.OnDrawerItemClickListener mOnDrawerItemClickListener = new Drawer.OnDrawerItemClickListener() {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            int option = (int) drawerItem.getTag();
            switch (option) {
                case R.string.drawer_item_main_categories:
                    showCategories();
                    break;
                case R.string.drawer_item_main_projects:
                    showProjects();
                    break;
                case R.string.drawer_item_task_current:
                    showTasks();
                default:
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //dbStuff();

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
            showTasks();
        }


        if (laFragmentControlsContainer != null) {
            if (savedInstanceState != null) {
                return;
            }

            TimerControlsFragment fragment = new TimerControlsFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_controls_container, fragment).commit();
        }

        //TaskDAO.getDAO().get(2L).subscribe(new Action1<Task>() {
         //   @Override
         //   public void call(Task task) {
        //        Toast.makeText(MainActivity.this, task.getName(), Toast.LENGTH_SHORT).show();
        //    }
       // });

        //Intent intent = new Intent(this, TaskEditActivity.class);
        //startActivity(intent);

        //FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //ProjectEditDialogFragment fragment = ProjectEditDialogFragment.newInstance();
        //fragment.show(ft, "dialog_edit_project");
        Toast.makeText(this, "UNDER CONSTRUCTION", Toast.LENGTH_SHORT).show();
    }
    //TODO: избавиться
    private void dbStuff(){
        /*
        ProjectDAO.getDAO().add(new Project(0, "Имбирь", null, 0, 0, 0)).toBlocking().single();
        ProjectDAO.getDAO().add(new Project(0, "Сахар", null, 1, 0, 0)).toBlocking().single();
        ProjectDAO.getDAO().add(new Project(0, "Соль", null, 2, 0, 0)).toBlocking().single();
        ProjectDAO.getDAO().add(new Project(0, "Перец", null, 3, 0, 0)).toBlocking().single();
        ProjectDAO.getDAO().add(new Project(0, "Корица", null, 4, 0, 0)).toBlocking().single();

        CategoryDAO.getDAO().add(new Category(0,"Мята",0)).toBlocking().single();
        CategoryDAO.getDAO().add(new Category(0,"Ромашка",2)).toBlocking().single();
        CategoryDAO.getDAO().add(new Category(0,"Зверобой",4)).toBlocking().single();
        CategoryDAO.getDAO().add(new Category(0,"Крапива",6)).toBlocking().single();
        CategoryDAO.getDAO().add(new Category(0,"Мандрагора",8)).toBlocking().single();
        */

        //TrackUnitDAO.getDAO().add(new TrackUnit(0, "страницы", "стр.")).toBlocking().single();
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
        cal.set(2015,7,11, 8, 0);
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
        task.setWorkTime(1000*60*32);
        task.setSmallBreakTime(1000*60*8);
        task.setBigBreakTime(1000*60*16);
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
        cal.set(2015,7,11, 13, 31);
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
        cal.set(2019,2,22, 05, 50);
        task.setBeginDate(cal);
        task.setRepeatable(true);
        task.setWithTime(false);
        task.setProgressTrackMode(Task.ProgressTrackMode.UNITS);
        task.setAmountOnce(44);
        task.setAmountTotal(444);
        task.setChronoTrackMode(Task.ChronoTrackMode.COUNTDOWN);
        task.setWorkTime(1000*60*32);
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

        Task t = new Task();
        t.setId(1);
        cal = Calendar.getInstance();
        cal.set(2017, 7, 9, 17, 21);
        ConcreteTask ct = new ConcreteTask(0, t, cal, 0, 15, 60*1000*93);
        ConcreteTaskDAO.getDAO().add(ct).toBlocking().single();

        t = new Task();
        t.setId(2);
        cal = Calendar.getInstance();
        cal.set(2016, 4, 11, 8, 00);
        ct = new ConcreteTask(0, t, cal, 0, 0, 60*1000*12);
        ConcreteTaskDAO.getDAO().add(ct).toBlocking().single();

        t = new Task();
        t.setId(3);
        cal = Calendar.getInstance();
        cal.set(2017, 6, 24, 23, 59);
        ct = new ConcreteTask(0, t, cal, 0, 0, 60*1000*6);
        ConcreteTaskDAO.getDAO().add(ct).toBlocking().single();

        t = new Task();
        t.setId(4);
        cal = Calendar.getInstance();
        cal.set(2017, 6, 26, 11, 11);
        ct = new ConcreteTask(0, t, cal, 0, 0, 60*1000*42);
        ConcreteTaskDAO.getDAO().add(ct).toBlocking().single();


        t = new Task();
        t.setId(4);
        ct = new ConcreteTask(0, t, null, 0, 0, 0);
        ConcreteTaskDAO.getDAO().add(ct).subscribe();
    }

    //Заменить текущий фрагмент в контейнере
    private void setMainFragment(Fragment fragment, String tag) {
        if (mFragmentManager.findFragmentByTag(tag) == null) {
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag).commit();
        }
        mCurrentFragment = tag;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment;
        switch (id) {
            case R.id.action_add:
                if (mCurrentFragment.equals(FRAGMENT_CATEGORY)){
                    fragment = mFragmentManager.findFragmentByTag(FRAGMENT_CATEGORY);
                    ((CategoriesDialogFragment) fragment).createItem();
                }
                else if (mCurrentFragment.equals(FRAGMENT_TASKS)){
                    fragment = mFragmentManager.findFragmentByTag(FRAGMENT_TASKS);
                    ((TasksFragment) fragment).createTask();
                }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

       //Перейти к редактированию категорий
    private void showCategories() {
        CategoriesDialogFragment fragment = CategoriesDialogFragment.newInstance();
        setMainFragment(fragment, FRAGMENT_CATEGORY);
        hideControls();
        mToolbar.setTitle(R.string.drawer_item_main_categories);
    }

    //Перейти к редактированию проектов
    private void showProjects() {
        ProjectsDialogFragment fragment = ProjectsDialogFragment.newInstance();
        setMainFragment(fragment, FRAGMENT_PROJECTS);
        hideControls();
        mToolbar.setTitle(R.string.drawer_item_main_projects);
    }

    private void showTasks(){
        TasksFragment fragment = new TasksFragment();
        setMainFragment(fragment, FRAGMENT_TASKS);
        mToolbar.setTitle(R.string.drawer_item_task_current);
    }

    @Override
    public boolean controlsAreShown() {
        return cvContainer.getVisibility() == View.VISIBLE;
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
}
