package app.warinator.goalcontrol.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO.StatUnits;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO.StatisticItem;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Активность статистики
 */
public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG_DIALOG_DATE = "dialog_date";
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
    @BindView(R.id.rb_bar)
    RadioRealButton rbBar;
    @BindView(R.id.sp_chart_items)
    Spinner spChartItems;
    @BindView(R.id.chart_pie)
    PieChart chartPie;
    @BindView(R.id.chart_bars)
    BarChart chartBars;
    @BindView(R.id.chart_line)
    LineChart chartLine;
    @BindView(R.id.la_idle)
    RelativeLayout laIdle;
    @BindView(R.id.cb_include_removed)
    CheckBox cbIncludeRemoved;
    @BindView(R.id.tv_include_removed)
    TextView tvIncludeRemoved;
    private Toolbar mToolbar;
    private StatUnits mStatUnits;
    private IntervalType mIntervalType;
    private IntervalType mIntervalTypePrev;
    private ChartType mChartType;
    private ConcreteTaskDAO.Group mChartItems;
    private boolean mIncludeRemoved;
    private CompositeSubscription mSub;
    private Calendar from;
    private Calendar to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ButterKnife.bind(this);

        mSub = new CompositeSubscription();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //единицы (время/прогресс)
        ArrayAdapter<String> statUnitsAdapter = new ArrayAdapter<>(this, R.layout.toolbar_spinner_item,
                getResources().getStringArray(R.array.statistic_unit));
        statUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatUnits.setAdapter(statUnitsAdapter);

        //период статистики
        ArrayAdapter<String> intervalTypeAdapter = new ArrayAdapter<>(this, R.layout.toolbar_spinner_item,
                getResources().getStringArray(R.array.statistic_interval_type));
        intervalTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInterval.setAdapter(intervalTypeAdapter);

        //элементы статистики
        ArrayAdapter<String> chartItemsAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item,
                getResources().getStringArray(R.array.statistic_chart_items));
        chartItemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spChartItems.setAdapter(chartItemsAdapter);

        rbgChartType.setPosition(ChartType.PIE.ordinal());
        mIncludeRemoved = true;
        cbIncludeRemoved.setChecked(mIncludeRemoved);
        setupCharts();
        setupChartType(ChartType.PIE);
        setupStatUnits(StatUnits.TIME);
        setupInterval(IntervalType.PREV_WEEK);
        setupChartItems(ConcreteTaskDAO.Group.TASKS);

        spInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IntervalType intervalType = IntervalType.values()[position];
                if (intervalType == mIntervalType) {
                    return;
                }
                if (intervalType == IntervalType.STARTING_WITH || intervalType == IntervalType.MONTH) {
                    mIntervalTypePrev = mIntervalType;
                    mIntervalType = intervalType;
                    showDatePicker();
                } else {
                    setupInterval(intervalType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spStatUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupStatUnits(StatUnits.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spChartItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupChartItems(ConcreteTaskDAO.Group.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        rbgChartType.setOnPositionChangedListener((button, position) -> {
            if (position != mChartType.ordinal()) {
                setupChartType(ChartType.values()[position]);
            }
        });

        cbIncludeRemoved.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mIncludeRemoved = isChecked;
            refreshCharts();
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //Настроить единицы статистики
    private void setupStatUnits(StatUnits statUnits) {
        StatUnits old = mStatUnits;
        if (statUnits != mStatUnits) {
            mStatUnits = statUnits;
            if (old != null) {
                refreshCharts();
            }

            if (statUnits == StatUnits.TIME) {
                ivAmountIcon.setImageResource(R.drawable.ic_time);
            } else {
                ivAmountIcon.setImageResource(R.drawable.ic_trending_up);
            }
        }
    }

    //Настроить тип графика
    private void setupChartType(ChartType chartType) {
        mChartType = chartType;
        switch (chartType) {
            case PIE:
                chartPie.setVisibility(View.VISIBLE);
                chartBars.setVisibility(View.GONE);
                break;
            case BARS:
                chartPie.setVisibility(View.GONE);
                chartBars.setVisibility(View.VISIBLE);
                break;
        }
    }

    //Настроить тип группировки данных статистики
    private void setupChartItems(ConcreteTaskDAO.Group items) {
        if (mChartItems != items) {
            ConcreteTaskDAO.Group oldItems = mChartItems;
            mChartItems = items;
            if (oldItems != null) {
                refreshCharts();
            }
        }
    }

    //Настроить интервал времени статистики
    private void setupInterval(IntervalType intervalType) {
        IntervalType old = mIntervalType;
        mIntervalType = intervalType;

        if (intervalType != IntervalType.STARTING_WITH && intervalType != IntervalType.MONTH) {
            to = Calendar.getInstance();
            to = Util.justDate(to);
            from = Util.justDate(to.getTimeInMillis());
            to.add(Calendar.DATE, 1);
            ivConfigure.setVisibility(View.GONE);
            laSpecificInterval.setEnabled(false);
        } else {
            ivConfigure.setVisibility(View.VISIBLE);
            laSpecificInterval.setOnClickListener(v -> {
                mIntervalTypePrev = mIntervalType;
                showDatePicker();
            });
            laSpecificInterval.setEnabled(true);
        }
        switch (intervalType) {
            case THIS_WEEK:
                from.setFirstDayOfWeek(Calendar.MONDAY);
                while (from.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    from.add(Calendar.DATE, -1);
                }
                break;
            case PREV_WEEK:
                from.add(Calendar.DATE, -7);
                while (from.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    from.add(Calendar.DATE, -1);
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
        if (old != null) {
            refreshCharts();
        }

    }

    //Открыть диалог выбора даты
    private void showDatePicker() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);
        tomorrow = Util.justDate(tomorrow);
        Calendar then = Calendar.getInstance();
        then.setTimeInMillis(tomorrow.getTimeInMillis());
        if (mIntervalType == IntervalType.STARTING_WITH) {
            then.add(Calendar.MONTH, -3);
        } else {
            then.add(Calendar.YEAR, -1);
        }

        Calendar finalTomorrow = tomorrow;
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar date = Calendar.getInstance();
                    date.set(year, monthOfYear, dayOfMonth, 0, 0);
                    if (mIntervalType == IntervalType.MONTH) {
                        if (monthOfYear == Calendar.getInstance().get(Calendar.MONTH)) {
                            if (year == Calendar.getInstance().get(Calendar.YEAR)) {
                                //текущий месяц
                                to.setTimeInMillis(finalTomorrow.getTimeInMillis());
                                from.setTimeInMillis(finalTomorrow.getTimeInMillis());
                                from.set(Calendar.DAY_OF_MONTH, 1);
                            } else {
                                //этот же месяц в прошлом году
                                from.setTimeInMillis(then.getTimeInMillis());
                                to.setTimeInMillis(then.getTimeInMillis());
                                to.set(Calendar.DAY_OF_MONTH, 1);
                                to.add(Calendar.MONTH, 1);
                            }
                        } else {
                            date.set(Calendar.DAY_OF_MONTH, 1);
                            from.setTimeInMillis(date.getTimeInMillis());
                            date.add(Calendar.MONTH, 1);
                            to.setTimeInMillis(date.getTimeInMillis());
                        }
                    } else {
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

    //Обновить диаграммы
    private void refreshCharts() {

        Observable<List<StatisticItem>> obsChart =
                ConcreteTaskDAO.getDAO().getStatistics(mStatUnits, from, to,
                        mChartItems, mIncludeRemoved, 0);
        Observable<List<StatisticItem>> obsLine =
                ConcreteTaskDAO.getDAO().getStatistics(mStatUnits, from, to,
                        ConcreteTaskDAO.Group.DAY, mIncludeRemoved, 0);
        mSub.add(obsChart.observeOn(Schedulers.computation()).map(statisticItems -> {
            Collections.sort(statisticItems, (o1, o2) -> {
                if (o1.groupAmount < o2.groupAmount) {
                    return -1;
                }
                if (o1.groupAmount > o2.groupAmount) {
                    return 1;
                }
                return 0;
            });
            return statisticItems;
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(statisticItems -> {
                    refreshTotalAmount(statisticItems);
                    refreshPieChart(statisticItems);
                    refreshBarsChart(statisticItems);
                }));

        mSub.add(obsLine.map(statisticItems -> {
            int days = (int) Math.floor((to.getTimeInMillis() - from.getTimeInMillis())
                    / TimeUnit.DAYS.toMillis(1));
            StatisticItem[] itemsArr = new StatisticItem[days];
            for (StatisticItem item : statisticItems) {
                long d = Util.justDate(item.groupId).getTimeInMillis() -
                        Util.justDate(from).getTimeInMillis();
                int ind = (int) (d / TimeUnit.DAYS.toMillis(1));
                if (ind >= 0 && ind < days) {
                    itemsArr[ind] = item;
                }
            }
            Calendar cal = Util.justDate(from);
            for (int i = 0; i < days; i++, cal.add(Calendar.DATE, 1)) {
                if (itemsArr[i] == null) {
                    itemsArr[i] = new StatisticItem();
                    itemsArr[i].groupAmount = 0;
                }
                itemsArr[i].groupId = cal.getTimeInMillis();
                itemsArr[i].label = Util.getFormattedDate(cal, StatisticsActivity.this);
            }
            return Arrays.asList(itemsArr);
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::refreshLineChart));

    }

    //Обновить суммарное время/прогресс за текущий период
    private void refreshTotalAmount(List<StatisticItem> items) {
        long sum = 0;
        for (StatisticItem item : items) {
            sum += item.groupAmount;
        }
        if (mStatUnits == StatUnits.TIME) {
            tvAmount.setText(Util.getFormattedTimeAmt(sum, this));
        } else {
            tvAmount.setText(String.format(Locale.getDefault(), "%+d%%", sum));
        }

    }

    //Обновить круговую диаграмму
    private void refreshPieChart(List<StatisticItem> items) {
        List<PieEntry> entries = new ArrayList<>();
        for (StatisticItem item : items) {
            if (item.groupAmount > 0) {
                if (item.label.isEmpty()) {
                    if (mChartItems == ConcreteTaskDAO.Group.CATEGORIES) {
                        item.label = getString(R.string.common);
                    } else if (mChartItems == ConcreteTaskDAO.Group.PROJECTS) {
                        item.label = getString(R.string.by_default);
                    }
                }
                entries.add(new PieEntry((long) item.groupAmount, item.label));
            }
        }
        PieDataSet pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setColors(getResources().getIntArray(R.array.palette_chart_1));

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            if (mStatUnits == StatUnits.TIME) {
                return Util.getFormattedTimeAmt((long) value, StatisticsActivity.this);
            } else {
                return String.format(Locale.getDefault(), "%+d%%", (int) value);
            }
        });
        chartPie.setData(pieData);
        chartPie.highlightValues(null);
        chartPie.invalidate();
    }

    //Обновить столбчатую диаграмму
    private void refreshBarsChart(List<StatisticItem> items) {
        List<BarEntry> entries = new ArrayList<>();
        float x = 0;
        for (StatisticItem item : items) {
            if (item.groupAmount != 0) {
                entries.add(new BarEntry(x++, (long) item.groupAmount));
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        int[] colors = getResources().getIntArray(R.array.palette_chart_1);
        dataSet.setColors(colors);
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            if (mStatUnits == StatUnits.TIME) {
                return Util.getFormattedTimeAmt((long) value, StatisticsActivity.this);
            } else {
                return String.format(Locale.getDefault(), "%+d%%", (int) value);
            }
        });
        dataSet.setHighlightEnabled(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);
        chartBars.setFitBars(true);
        chartBars.setDrawGridBackground(false);
        chartBars.setPinchZoom(false);

        XAxis xAxis = chartBars.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setEnabled(false);

        chartBars.getAxisLeft().setEnabled(false);
        chartBars.getAxisRight().setValueFormatter((value, axis) -> {
            if (mStatUnits == StatUnits.TIME) {
                return Util.getFormattedTimeAmt((long) value, StatisticsActivity.this);
            } else {
                return String.format(Locale.getDefault(), "%+d%%", (int) value);
            }
        });
        if (mStatUnits == StatUnits.TIME) {
            chartBars.getAxisRight().setGranularity(TimeUnit.MINUTES.toMillis(1));
        } else {
            chartBars.getAxisRight().setGranularity(1);
        }

        Legend legend = chartBars.getLegend();
        int i = 0;
        List<LegendEntry> legendEntries = new ArrayList<>();
        for (StatisticItem item : items) {
            if (item.groupAmount != 0) {
                legendEntries.add(new LegendEntry(item.label, Legend.LegendForm.DEFAULT,
                        legend.getFormSize(), legend.getFormLineWidth(),
                        legend.getFormLineDashEffect(), colors[i]));
                i = (i + 1) % colors.length;
            }
        }
        legend.setCustom(legendEntries);

        chartBars.setData(data);
        chartBars.highlightValues(null);
        chartBars.invalidate();
    }

    @Override
    protected void onDestroy() {
        if (!mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
        super.onDestroy();
    }

    //Обновить график динамики прогресса/времени
    private void refreshLineChart(List<StatisticItem> items) {
        List<Entry> entries = new ArrayList<>();
        float x = 0;
        for (StatisticItem item : items) {
            entries.add(new Entry(x++, (long) item.groupAmount));
        }
        if (entries.size() == 0) {
            entries.add(new Entry(x, 0));
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.statistics_by_days));
        dataSet.setColor(ContextCompat.getColor(StatisticsActivity.this, R.color.colorPrimaryDark));
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            if (value > 0) {
                if (mStatUnits == StatUnits.TIME) {
                    return Util.getFormattedTimeAmt((long) value, StatisticsActivity.this);
                } else {
                    return String.format(Locale.getDefault(), "+%d%%", (int) value);
                }
            } else {
                return "";
            }
        });
        dataSet.setHighlightEnabled(false);
        dataSet.setCircleColor(ContextCompat.getColor(StatisticsActivity.this, R.color.colorAccent));
        dataSet.setCircleRadius(2);

        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        XAxis xAxis = chartLine.getXAxis();
        xAxis.setGranularity(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter((value, axis) -> {
            Calendar date = Util.justDate(from);
            date.add(Calendar.DATE, (int) value);
            if (mIntervalType == IntervalType.THIS_WEEK || mIntervalType == IntervalType.PREV_WEEK) {
                return new SimpleDateFormat("E", Locale.getDefault()).format(date.getTime());
            } else {
                return new SimpleDateFormat("d MMM", Locale.getDefault()).format(date.getTime());
            }
        });

        YAxis leftAxis = chartLine.getAxisLeft();
        leftAxis.setValueFormatter((value, axis) -> {
            if (mStatUnits == StatUnits.TIME) {
                return Util.getFormattedTimeAmt((long) value, StatisticsActivity.this);
            } else {
                return String.format(Locale.getDefault(), "%d%%", (int) value);
            }
        });
        YAxis rightAxis = chartLine.getAxisRight();
        rightAxis.setDrawLabels(false);

        LineData data = new LineData(dataSet);
        chartLine.setData(data);
        chartLine.highlightValues(null);
        chartLine.invalidate();
    }

    //Настроить диаграммы
    private void setupCharts() {
        chartPie.setDrawHoleEnabled(false);
        chartPie.getDescription().setEnabled(false);
        setupLegend(chartPie.getLegend());
        chartPie.setEntryLabelTextSize(9f);

        chartBars.getDescription().setEnabled(false);
        setupLegend(chartBars.getLegend());

        chartLine.getDescription().setEnabled(false);
    }

    //Настроить легенду статистики
    private void setupLegend(Legend legend) {
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);
    }

    //Тип графика
    private enum ChartType {
        PIE, BARS
    }

    //Тип интервала статистики
    private enum IntervalType {
        THIS_WEEK, PREV_WEEK, STARTING_WITH, MONTH, YEAR
    }

}
