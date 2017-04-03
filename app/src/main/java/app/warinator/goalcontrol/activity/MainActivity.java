package app.warinator.goalcontrol.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
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

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import app.warinator.goalcontrol.MaterialDrawer;
import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.fragment.CategoriesDialogFragment;
import app.warinator.goalcontrol.fragment.ControlsFragment;
import app.warinator.goalcontrol.fragment.TasksViewFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Главная activity
 */
public class MainActivity extends AppCompatActivity
        implements TasksViewFragment.ControlsVisibility {
    private static final String FRAGMENT_TASKS = "fragment_tasks";
    private static final String FRAGMENT_CATEGORY = "fragment_category";
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
                default:
            }
            return false;
        }
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

            TasksViewFragment fragment = new TasksViewFragment();
            setMainFragment(fragment, FRAGMENT_TASKS);
        }


        if (laFragmentControlsContainer != null) {
            if (savedInstanceState != null) {
                return;
            }

            ControlsFragment fragment = new ControlsFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_controls_container, fragment).commit();
        }


        Intent intent = new Intent(this, TaskEditActivity.class);
        startActivity(intent);

        //FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //ProjectEditDialogFragment fragment = ProjectEditDialogFragment.newInstance();
        //fragment.show(ft, "dialog_edit_project");
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
                fragment = mFragmentManager.findFragmentByTag(FRAGMENT_CATEGORY);
                if (fragment != null) {
                    ((CategoriesDialogFragment) fragment).createItem();
                }
                break;
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

    @Override
    public boolean controlsAreShown() {
        return cvContainer.getVisibility() == View.VISIBLE;
    }

}
