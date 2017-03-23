package app.warinator.goalcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements TasksViewFragment.ControlsVisibility{
    @BindView(R.id.controls_container)
    CardView cvContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
        MaterialDrawer.build(this,toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            TasksViewFragment fragment = new TasksViewFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment).commit();
        }


        if (findViewById(R.id.fragment_controls_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            ControlsFragment fragment = new ControlsFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_controls_container, fragment).commit();
        }

        Intent intent = new Intent(this, TaskEditActivity.class);
        startActivity(intent);
    }

    @Override
    public void showControls(){
        cvContainer.setVisibility(View.VISIBLE);
    }
    @Override
    public void hideControls(){
        cvContainer.setVisibility(View.GONE);
    }

    @Override
    public void returnControls(){
        cvContainer.setVisibility(View.INVISIBLE);
        cvContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean controlsAreShown() {
        return cvContainer.getVisibility() == View.VISIBLE;
    }
}
