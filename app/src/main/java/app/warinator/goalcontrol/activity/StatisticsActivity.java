package app.warinator.goalcontrol.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;

import java.util.Calendar;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;

public class StatisticsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    @BindView(R.id.sp_stat_units)
    Spinner spStatUnits;
    @BindView(R.id.sp_interval)
    Spinner spInterval;

    @BindView(R.id.la_specific_interval)
    LinearLayout laSpecificInterval;
    @BindView(R.id.tv_specific_top)
    TextView tvSpecificTop;
    @BindView(R.id.tv_specific_bottom)
    TextView tvSpecificBottom;
    @BindView(R.id.iv_amount_icon)
    ImageView ivAmountIcon;
    @BindView(R.id.tv_amount)
    TextView tvAmount;

    @BindView(R.id.rbg_chart_type)
    RadioRealButtonGroup rbgChartType;
    @BindView(R.id.rb_pie)
    RadioRealButton rbPie;
    @BindView(R.id.rb_radar)
    RadioRealButton rbRadar;
    @BindView(R.id.rb_bar)
    RadioRealButton rbBar;
    @BindView(R.id.sp_chart_items)
    Spinner spChartItems;
    @BindView(R.id.chart_pie)
    PieChart chartPie;
    @BindView(R.id.chart_radar)
    RadarChart chartRadar;
    @BindView(R.id.chart_bars)
    BarChart chartBars;
    @BindView(R.id.la_idle)
    RelativeLayout laIdle;
    @BindView(R.id.cb_include_idle)
    CheckBox cbIncludeIdle;
    @BindView(R.id.tv_include_idle)
    TextView tvIncludeIdle;

    private enum ChartType {
        PIE, BAR, LINE
    }

    private enum IntervalType {
        THIS_WEEK, PREV_WEEK, LAST_DAYS, MONTH, YEAR
    }

    private enum StatUnits {
        TIME, PROGRESS
    }

    private StatUnits mStatUnits;
    private IntervalType mIntervalType;
    private ChartType mChartType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ButterKnife.bind(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayAdapter<String> statUnitsAdapter = new ArrayAdapter<>(this, R.layout.toolbar_spinner_item,
                getResources().getStringArray(R.array.statistic_unit));
        statUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatUnits.setAdapter(statUnitsAdapter);

        ArrayAdapter<String> intervalTypeAdapter = new ArrayAdapter<>(this, R.layout.toolbar_spinner_item,
                getResources().getStringArray(R.array.statistic_interval_type));
        intervalTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInterval.setAdapter(intervalTypeAdapter);

        ArrayAdapter<String> chartItemsAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item,
                getResources().getStringArray(R.array.statistic_chart_items));
        chartItemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spChartItems.setAdapter(chartItemsAdapter);

        rbgChartType.setPosition(ChartType.PIE.ordinal());
        setupStatistics(StatUnits.TIME, IntervalType.THIS_WEEK, ChartType.PIE);

        Calendar d1 = Calendar.getInstance();
        Calendar d2 = Calendar.getInstance();
        d1.add(Calendar.DATE, -14);
        d2.add(Calendar.DATE, 1);


        ConcreteTaskDAO.getDAO().getProgressStatistics(d1,d2, ConcreteTaskDAO.Group.CATEGORIES).subscribe(statisticItems -> {
            for (ConcreteTaskDAO.StatisticItem item : statisticItems){
                Log.v("THE_QUERY_R", ""+item.groupId+" "+item.groupAmount);
            }
        });
        /*
        ConcreteTaskDAO.getDAO().getProgressStatistics(d1,d2, ConcreteTaskDAO.Group.TASKS).subscribe(statisticItems -> {
            for (ConcreteTaskDAO.StatisticItem item : statisticItems){
                Log.v("THE_QUERY_R", ""+item.groupId+" "+item.groupAmount);
            }
        });
        Log.v("THE_QUERY_R","------------------");
 ConcreteTaskDAO.getDAO().getProgressStatistics(d1,d2, ConcreteTaskDAO.Group.PROJECTS).subscribe(statisticItems -> {
            for (ConcreteTaskDAO.StatisticItem item : statisticItems){
                Log.v("THE_QUERY_R", ""+item.groupId+" "+item.groupAmount);
            }
        });
        Log.v("THE_QUERY_R","------------------");

        */
    }

    private void setupStatistics(StatUnits statUnits, IntervalType intervalType, ChartType chartType){
        mStatUnits = statUnits;
        mIntervalType = intervalType;
        mChartType = chartType;

        if (statUnits == StatUnits.TIME){
            ivAmountIcon.setImageResource(R.drawable.ic_time);
            laIdle.setVisibility(View.VISIBLE);
        }
        else {
            ivAmountIcon.setImageResource(R.drawable.ic_trending_up);
            laIdle.setVisibility(View.GONE);
        }


    }

    private void displayChart(){

    }

}
