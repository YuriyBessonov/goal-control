package app.warinator.goalcontrol.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;

public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG_DIALOG_DATE = "dialog_date";
    private Toolbar mToolbar;
    @BindView(R.id.sp_stat_units)
    Spinner spStatUnits;
    @BindView(R.id.sp_interval)
    Spinner spInterval;

    @BindView(R.id.la_specific_interval)
    LinearLayout laSpecificInterval;
    @BindView(R.id.tv_date_from)
    TextView tvDateFrom;
    @BindView(R.id.tv_date_to)
    TextView tvDateTo;
    @BindView(R.id.iv_amount_icon)
    ImageView ivAmountIcon;
    @BindView(R.id.tv_amount)
    TextView tvAmount;
    @BindView(R.id.iv_configure)
    ImageView ivConfigure;

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
        PIE, RADAR, BARS
    }

    private enum IntervalType {
        THIS_WEEK, PREV_WEEK, STARTING_WITH, MONTH, YEAR
    }

    private enum StatUnits {
        TIME, PROGRESS
    }

    private StatUnits mStatUnits;
    private IntervalType mIntervalType;
    private IntervalType mIntervalTypePrev;
    private ChartType mChartType;

    private Calendar from;
    private Calendar to;

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
        spInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IntervalType intervalType = IntervalType.values()[position];
                if (intervalType == mIntervalType){
                    return;
                }
                if (intervalType == IntervalType.STARTING_WITH || intervalType == IntervalType.MONTH){
                    mIntervalTypePrev = mIntervalType;
                    mIntervalType = intervalType;
                    showDatePicker();
                }
                else {
                    setupInterval(intervalType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        rbgChartType.setOnPositionChangedListener((button, position) -> {
            if (position != mChartType.ordinal()){
                setupChartType(ChartType.values()[position]);
            }
        });


        Calendar d1 = Calendar.getInstance();
        Calendar d2 = Calendar.getInstance();
        d1.add(Calendar.DATE, -14);
        d2.add(Calendar.DATE, 1);


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

        ConcreteTaskDAO.getDAO().getProgressStatistics(d1,d2, ConcreteTaskDAO.Group.CATEGORIES).subscribe(statisticItems -> {
            for (ConcreteTaskDAO.StatisticItem item : statisticItems){
                Log.v("THE_QUERY_R", ""+item.groupId+" "+item.groupAmount);
            }
        });
        */
    }

    private void setupStatistics(StatUnits statUnits, IntervalType intervalType, ChartType chartType){
        mStatUnits = statUnits;

        setupInterval(intervalType);
        setupChartType(chartType);

        if (statUnits == StatUnits.TIME){
            ivAmountIcon.setImageResource(R.drawable.ic_time);
            laIdle.setVisibility(View.VISIBLE);
        }
        else {
            ivAmountIcon.setImageResource(R.drawable.ic_trending_up);
            laIdle.setVisibility(View.GONE);
        }

    }

    private void setupChartType(ChartType chartType){
        mChartType = chartType;
        switch (chartType){
            case PIE:
                chartPie.setVisibility(View.VISIBLE);
                chartRadar.setVisibility(View.GONE);
                chartBars.setVisibility(View.GONE);
                break;
            case RADAR:
                chartPie.setVisibility(View.GONE);
                chartRadar.setVisibility(View.VISIBLE);
                chartBars.setVisibility(View.GONE);
                break;
            case BARS:
                chartPie.setVisibility(View.GONE);
                chartRadar.setVisibility(View.GONE);
                chartBars.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupInterval(IntervalType intervalType){
        mIntervalType = intervalType;

        if (intervalType != IntervalType.STARTING_WITH && intervalType != IntervalType.MONTH){
            to = Calendar.getInstance();
            to.add(Calendar.DATE, 1);
            to = Util.justDate(to);
            from = Calendar.getInstance();
            from.setTimeInMillis(to.getTimeInMillis());
            ivConfigure.setVisibility(View.GONE);
            laSpecificInterval.setEnabled(false);
        }
        else {
            ivConfigure.setVisibility(View.VISIBLE);
            laSpecificInterval.setOnClickListener(v -> {
                mIntervalTypePrev = mIntervalType;
                showDatePicker();
            });
            laSpecificInterval.setEnabled(true);
        }
        switch (intervalType){
            case THIS_WEEK:
                from.setFirstDayOfWeek(Calendar.MONDAY);
                while (from.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
                    from.add(Calendar.DATE,-1);
                }
                break;
            case PREV_WEEK:
                from.add(Calendar.DATE, -7);
                while (from.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
                    from.add(Calendar.DATE,-1);
                }
                to.setTimeInMillis(from.getTimeInMillis());
                to.add(Calendar.DATE, 7);
                break;
            case YEAR:
                from.add(Calendar.YEAR, -1);
                break;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        tvDateFrom.setText(String.format(getString(R.string.from_date), formatter.format(from.getTime())));
        Calendar displTo = Calendar.getInstance();
        displTo.setTimeInMillis(to.getTimeInMillis());
        displTo.add(Calendar.DATE, -1);
        tvDateTo.setText(String.format(getString(R.string.to_date), formatter.format(displTo.getTime())));
    }

    private void showDatePicker(){
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        tomorrow = Util.justDate(tomorrow);
        Calendar then = Calendar.getInstance();
        then.setTimeInMillis(tomorrow.getTimeInMillis());
        if (mIntervalType == IntervalType.STARTING_WITH){
            then.add(Calendar.MONTH, -3);
        }
        else {
            then.add(Calendar.YEAR, -1);
        }

        Calendar finalTomorrow = tomorrow;
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar date = Calendar.getInstance();
                    date.set(year, monthOfYear, dayOfMonth, 0, 0);
                    if (mIntervalType == IntervalType.MONTH){
                        if (monthOfYear ==  Calendar.getInstance().get(Calendar.MONTH)){
                            if (year == Calendar.getInstance().get(Calendar.YEAR)){
                                //текущий месяц
                                to.setTimeInMillis(finalTomorrow.getTimeInMillis());
                                from.setTimeInMillis(finalTomorrow.getTimeInMillis());
                                from.set(Calendar.DAY_OF_MONTH, 1);
                            }
                            else {
                                //этот же месяц в прошлом году
                                from.setTimeInMillis(then.getTimeInMillis());
                                to.setTimeInMillis(then.getTimeInMillis());
                                to.set(Calendar.DAY_OF_MONTH, 1);
                                to.add(Calendar.MONTH, 1);
                            }
                        }
                        else {
                            date.set(Calendar.DAY_OF_MONTH, 1);
                            from.setTimeInMillis(date.getTimeInMillis());
                            date.add(Calendar.MONTH, 1);
                            to.setTimeInMillis(date.getTimeInMillis());
                        }
                    }
                    else {
                        to.setTimeInMillis(finalTomorrow.getTimeInMillis());
                        from.setTimeInMillis(Util.justDate(date).getTimeInMillis());
                    }
                    setupInterval(mIntervalType);
                },
                tomorrow.get(Calendar.YEAR),
                tomorrow.get(Calendar.MONTH),
                tomorrow.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setMaxDate(Calendar.getInstance());
        dpd.setMinDate(then);
        dpd.setOnCancelListener(dialog -> {
            mIntervalType = mIntervalTypePrev;
            spInterval.setSelection(mIntervalType.ordinal());
        });
        dpd.show(getFragmentManager(), TAG_DIALOG_DATE);
    }

    private void displayChart(){

    }

}
