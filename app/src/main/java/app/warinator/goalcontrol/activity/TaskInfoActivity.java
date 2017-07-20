package app.warinator.goalcontrol.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.natasa.progressviews.CircleProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.model.CheckListItem;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.utils.ColorUtil;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.GONE;

/**
 * Активность сведений о задаче
 */
public class TaskInfoActivity extends AppCompatActivity {

    private static final String ARG_TASK_ID = "task_id";
    @BindView(R.id.toolbar_task)
    Toolbar toolbar;
    @BindView(R.id.iiv_task_icon)
    IconicsImageView iivTaskIcon;
    @BindView(R.id.tv_task_name)
    TextView tvTaskName;
    @BindView(R.id.tv_project_name)
    TextView tvProjectName;
    @BindView(R.id.tv_progress)
    TextView tvProgress;
    @BindView(R.id.pb_progress_real)
    CircleProgressBar pbProgress;
    @BindView(R.id.tv_time_spent)
    TextView tvTimeSpent;
    @BindView(R.id.tv_amount_done)
    TextView tvAmountDone;
    @BindView(R.id.tv_status)
    TextView tvStatus;
    @BindView(R.id.tv_priority)
    TextView tvPriority;
    @BindView(R.id.tv_category)
    TextView tvCategory;

    @BindView(R.id.tv_begin_date)
    TextView tvBeginDate;
    @BindView(R.id.tv_end_date)
    TextView tvEndDate;
    @BindView(R.id.tv_days_passed)
    TextView tvDaysPassed;
    @BindView(R.id.tv_days_left)
    TextView tvDaysLeft;
    @BindView(R.id.tv_times_done)
    TextView tvTimesDone;
    @BindView(R.id.tv_times_left)
    TextView tvTimesLeft;

    @BindView(R.id.tv_progress_avg)
    TextView tvProgressAvg;
    @BindView(R.id.tv_progress_max)
    TextView tvProgressMax;
    @BindView(R.id.tv_time_avg)
    TextView tvTimeAvg;
    @BindView(R.id.tv_time_max)
    TextView tvTimeMax;

    @BindView(R.id.la_progress_avg)
    RelativeLayout laProgressAvg;
    @BindView(R.id.la_progress_max)
    RelativeLayout laProgressMax;
    @BindView(R.id.la_begin_date)
    RelativeLayout laBeginDate;
    @BindView(R.id.la_end_date)
    RelativeLayout laEndDate;
    @BindView(R.id.la_days_passed)
    RelativeLayout laDaysPassed;
    @BindView(R.id.la_days_left)
    RelativeLayout laDaysLeft;
    @BindView(R.id.la_times_done)
    RelativeLayout laTimesDone;
    @BindView(R.id.la_times_left)
    RelativeLayout laTimesLeft;

    @BindView(R.id.cv_note)
    CardView cvNote;
    @BindView(R.id.tv_note)
    TextView tvNote;

    @BindView(R.id.btn_back)
    ImageButton btnBack;
    @BindView(R.id.btn_edit)
    ImageButton btnEdit;

    @BindView(R.id.la_root)
    LinearLayout laRoot;
    @BindView(R.id.cv_details)
    CardView cvDetails;
    @BindView(R.id.cv_stat)
    CardView cvStat;

    @BindView(R.id.sp_chart_data)
    Spinner spChartData;
    @BindView(R.id.chart_line)
    LineChart chartLine;

    private Calendar mBeginDate;
    private Calendar mEndDate;
    private Calendar mToday;

    private Task mTask;
    private int mTotalAmt;
    private ChartUnits mChartUnits;

    ;
    private CompositeSubscription mSub;

    public static Intent getIntent(Context context, long taskId) {
        Intent intent = new Intent(context, TaskInfoActivity.class);
        intent.putExtra(ARG_TASK_ID, taskId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);
        ButterKnife.bind(this);

        mSub = new CompositeSubscription();

        if (savedInstanceState == null) {
            Bundle b = getIntent().getExtras();
            long taskId = b.getLong(ARG_TASK_ID, 0);
            if (taskId != 0) {
                mSub.add(TaskDAO.getDAO().get(taskId).subscribe(task -> {
                    mTask = task;
                    setupTask();
                }));
            }
        }

        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> startActivity(TaskEditActivity.getIntent(mTask.getId(),
                TaskInfoActivity.this)));

        String[] statisticsUnits = {getString(R.string.time), getString(R.string.progress)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statisticsUnits);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spChartData.setAdapter(adapter);
        mChartUnits = ChartUnits.TIME;
        spChartData.setSelection(mChartUnits.ordinal());
        spChartData.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mChartUnits = ChartUnits.values()[position];
                if (mBeginDate != null && Util.compareDays(mBeginDate, mToday) <= 0) {
                    getStatistics();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        chartLine.getDescription().setEnabled(false);
    }

    //Получить статистику по задаче
    private void getStatistics() {
        Observable<List<ConcreteTaskDAO.StatisticItem>> obsLine;
        Calendar endDate =
                Util.justDate(Math.min(mToday.getTimeInMillis(), mEndDate.getTimeInMillis()));
        endDate.add(Calendar.DATE, 1);
        if (mChartUnits == ChartUnits.TIME) {
            obsLine = ConcreteTaskDAO.getDAO().getStatistics(ConcreteTaskDAO.StatUnits.TIME,
                    mBeginDate, endDate, ConcreteTaskDAO.Group.DAY, true, mTask.getId());
        } else {
            obsLine = ConcreteTaskDAO.getDAO().getTaskAmtByDays(mBeginDate, endDate, mTask.getId());
        }

        mSub.add(obsLine.map(statisticItems -> {
            int days = (int) Math.floor((endDate.getTimeInMillis() - mBeginDate.getTimeInMillis())
                    / TimeUnit.DAYS.toMillis(1));
            ConcreteTaskDAO.StatisticItem[] itemsArr = new ConcreteTaskDAO.StatisticItem[days];
            for (ConcreteTaskDAO.StatisticItem item : statisticItems) {
                long d = Util.justDate(item.groupId).getTimeInMillis() -
                        Util.justDate(mBeginDate).getTimeInMillis();
                int ind = (int) (d / TimeUnit.DAYS.toMillis(1));
                if (ind >= 0 && ind < days) {
                    itemsArr[ind] = item;
                }
            }
            Calendar cal = Util.justDate(mBeginDate);
            for (int i = 0; i < days; i++, cal.add(Calendar.DATE, 1)) {
                if (itemsArr[i] == null) {
                    itemsArr[i] = new ConcreteTaskDAO.StatisticItem();
                    itemsArr[i].groupAmount = 0;
                } else if (mChartUnits == ChartUnits.PROGRESS && mTotalAmt != 0) {
                    itemsArr[i].groupAmount = itemsArr[i].groupAmount / mTotalAmt * 100.0;
                }
                itemsArr[i].groupId = cal.getTimeInMillis();
                itemsArr[i].label = Util.getFormattedDate(cal, TaskInfoActivity.this);
            }
            return Arrays.asList(itemsArr);
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(this::showStatistics));
    }

    //Отобразить данные статистики по задаче
    private void showStatistics(List<ConcreteTaskDAO.StatisticItem> items) {
        List<Entry> entries = new ArrayList<>();
        float x = 0;
        for (ConcreteTaskDAO.StatisticItem item : items) {
            entries.add(new Entry(x++, (float) item.groupAmount));
        }
        if (entries.size() == 0) {
            entries.add(new Entry(x, 0));
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.statistics_by_days));
        dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            if (value > 0) {
                if (mChartUnits == ChartUnits.TIME) {
                    return Util.getFormattedTimeAmt((long) value, this);
                } else {
                    return String.format(Locale.getDefault(), "%+.1f%%", value);
                }
            } else {
                return "";
            }
        });
        dataSet.setHighlightEnabled(false);
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.colorAccent));
        dataSet.setCircleRadius(2);

        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        XAxis xAxis = chartLine.getXAxis();
        xAxis.setGranularity(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter((value, axis) -> {
            Calendar date = Util.justDate(mBeginDate);
            date.add(Calendar.DATE, (int) value);
            return new SimpleDateFormat("d MMM", Locale.getDefault()).format(date.getTime());
        });

        YAxis leftAxis = chartLine.getAxisLeft();
        leftAxis.setValueFormatter((value, axis) -> {
            if (mChartUnits == ChartUnits.TIME) {
                return Util.getFormattedTimeAmt((long) value, this);
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

    //Отобразить информацию о задаче
    public void setupTask() {
        tvTaskName.setText(mTask.getName());
        iivTaskIcon.setIcon(GoogleMaterial.Icon.values()[mTask.getIcon()]);
        int projectCol;
        if (mTask.getProject() != null) {
            projectCol = ColorUtil.getProjectColor(mTask.getProject().getColor(), this);
            tvProjectName.setText(mTask.getProject().getName());
        } else {
            projectCol = ColorUtil.getProjectColor(ColorUtil.COLOR_DEFAULT, this);
            tvProjectName.setText(R.string.by_default);
        }
        toolbar.setBackgroundColor(projectCol);
        laRoot.setBackgroundColor(projectCol);

        int prioCol = getResources().getIntArray
                (R.array.palette_priorities)[mTask.getPriority().ordinal()];
        tvPriority.setText(getResources()
                .getStringArray(R.array.priorities)[mTask.getPriority().ordinal()]);
        tvPriority.setTextColor(prioCol);

        int categoryCol;
        if (mTask.getCategory() != null) {
            tvCategory.setText(mTask.getCategory().getName());
            categoryCol = ColorUtil.getCategoryColor(mTask.getCategory().getColor(), this);
        } else {
            tvCategory.setText(R.string.common);
            categoryCol = ColorUtil.getCategoryColor(ColorUtil.COLOR_DEFAULT, this);
        }
        tvCategory.getBackground().setColorFilter(categoryCol, PorterDuff.Mode.SRC_ATOP);

        if (mTask.getNote() != null) {
            tvNote.setText(mTask.getNote());
        } else {
            cvNote.setVisibility(GONE);
        }

        if (mTask.getBeginDate() == null) {
            cvDetails.setVisibility(GONE);
            cvStat.setVisibility(GONE);
        }

        pbProgress.setStartPositionInDegrees(270);
        Task.ProgressTrackMode mode = mTask.getProgressTrackMode();
        int totalAmt = 0;
        if (mode == Task.ProgressTrackMode.LIST) {
            mSub.add(CheckListItemDAO.getDAO()
                    .getAllForTask(mTask.getId(), false).subscribe(checkListItems -> {
                        int allNeed = checkListItems.size();
                        mTotalAmt = allNeed;
                        int amtDone = 0;
                        for (CheckListItem item : checkListItems) {
                            if (item.isCompleted()) {
                                amtDone++;
                            }
                        }
                        int percent = (int) Math.round(((double) amtDone / (double) allNeed) * 100.0);
                        if (percent == 100 && tvProgress.getText().toString().isEmpty()) {
                            tvProgress.setText(R.string.completed);
                        }
                        tvProgress.setText(String.format(Locale.getDefault(), "%d%%", percent));
                        pbProgress.setProgress(percent);
                        tvAmountDone.setText(String.format(Locale.getDefault(),
                                "%d/%d", amtDone, allNeed));
                    }));
        } else if (mode != Task.ProgressTrackMode.MARK && mode != Task.ProgressTrackMode.SEQUENCE) {
            totalAmt = mTask.getAmountTotal();
        }

        int finalTotalAmt = totalAmt;
        mSub.add(ConcreteTaskDAO.getDAO().getByTaskId(mTask.getId(), false).subscribe(tasks -> {
            int amtDone = 0;//суммарный объем выполнения
            int amtMax = Integer.MIN_VALUE;//макс. объём выполнения за один раз
            long totalTime = 0;//суммарное учтённое время
            long timeMax = 0;//максимальное учтенное время за один раз
            mBeginDate = mTask.getBeginDate();//
            if (mBeginDate == null) {
                mBeginDate = Calendar.getInstance();
            } else {
                mBeginDate = Util.justDate(mBeginDate);
            }
            mEndDate = Util.justDate(mBeginDate);
            mToday = Util.justDate(Calendar.getInstance());

            int completedTimes = 0;//сколько раз был учтён прогресс
            long timeTrackedTimes = 0;//сколько раз было учтено время
            int timesUntilToday = 0;

            for (ConcreteTask ct : tasks) {
                amtDone += ct.getAmountDone();
                if (ct.getAmountDone() > 0) {
                    completedTimes++;
                }
                if (ct.getAmountDone() > amtMax) {
                    amtMax = ct.getAmountDone();
                }
                totalTime += ct.getTimeSpent();
                if (ct.getTimeSpent() > 0) {
                    timeTrackedTimes++;
                }
                if (ct.getTimeSpent() > timeMax) {
                    timeMax = ct.getTimeSpent();
                }
                if (ct.getDateTime() != null) {
                    if (Util.compareDays(ct.getDateTime(), mEndDate) > 0) {
                        mEndDate = Util.justDate(ct.getDateTime());
                    }
                    if (Util.compareDays(ct.getDateTime(), mToday) <= 0) {
                        timesUntilToday++;
                    }
                    if (Util.compareDays(ct.getDateTime(), mBeginDate) < 0) {
                        mBeginDate = Util.justDate(ct.getDateTime());
                    }
                }
            }

            int percent = 0;
            if (mode != Task.ProgressTrackMode.LIST) {
                mTotalAmt = finalTotalAmt > 0 ? finalTotalAmt : tasks.size();
                percent = (int) Math.round(((double) amtDone / (double) mTotalAmt) * 100.0);
                tvProgress.setText(String.format(Locale.getDefault(), "%d%%", percent));
                pbProgress.setProgress(percent);
                String unitsStr;
                if (mode == Task.ProgressTrackMode.UNITS) {
                    unitsStr = mTask.getUnits() != null ? mTask.getUnits().getShortName() : "";
                } else if (mode == Task.ProgressTrackMode.PERCENT) {
                    unitsStr = "%";
                } else {
                    unitsStr = getString(R.string.times);
                }

                tvAmountDone.setText(String.format(Locale.getDefault(), "%d/%d %s", amtDone,
                        mTotalAmt, unitsStr));
            }
            tvTimeSpent.setText(Util.getFormattedTimeAmt(totalTime, this));


            if (mTask.getBeginDate() == null) {
                laBeginDate.setVisibility(GONE);
                laEndDate.setVisibility(GONE);
                laDaysPassed.setVisibility(GONE);
                laDaysLeft.setVisibility(GONE);
                laTimesDone.setVisibility(GONE);
                laTimesLeft.setVisibility(GONE);

                if (percent == 100) {
                    tvStatus.setText(R.string.completed);
                } else if (tvStatus.getText().toString().isEmpty()) {
                    tvStatus.setText(R.string.in_progress);
                }
            } else {
                tvBeginDate.setText(Util.getFormattedDate(mBeginDate, this));
                tvEndDate.setText(Util.getFormattedDate(mEndDate, this));

                int daysTotal = Util.daysDifference(mBeginDate, mEndDate) + 1;
                int daysPassed = Util.daysDifference(mBeginDate, mToday);
                if (daysPassed < 0) {
                    laDaysPassed.setVisibility(GONE);
                    laDaysLeft.setVisibility(GONE);
                    cvStat.setVisibility(GONE);
                } else {
                    getStatistics();
                    if (daysPassed <= daysTotal) {
                        tvDaysPassed.setText(String.valueOf(daysPassed));
                    } else {
                        tvDaysPassed.setText(String.format(Locale.getDefault(), "%d+", daysTotal));
                    }
                }

                int daysLeft = daysTotal - daysPassed;
                if (daysLeft < 0) {
                    daysLeft = 0;
                }
                tvDaysLeft.setText(String.valueOf(daysLeft));

                tvTimesDone.setText(String.valueOf(completedTimes));
                tvTimesLeft.setText(String.valueOf(tasks.size() - completedTimes));

                if (percent == 100) {
                    tvStatus.setText(R.string.completed);
                } else if (tvStatus.getText().toString().isEmpty()) {
                    if (Util.compareDays(mBeginDate, mToday) > 0) {
                        tvStatus.setText(R.string.not_started);
                    } else if (Util.compareDays(mEndDate, mToday) < 0) {
                        tvStatus.setText(R.string.overdue);
                    } else {
                        tvStatus.setText(R.string.in_progress);
                    }
                }
            }


            if (mode == Task.ProgressTrackMode.LIST) {
                laProgressAvg.setVisibility(GONE);
                laProgressMax.setVisibility(GONE);
            } else {
                double amtAvg = (double) amtDone / timesUntilToday;
                int progressAvg = (int) Math.round(amtAvg / mTotalAmt * 100);
                if (progressAvg > 100) {
                    tvProgressAvg.setText("-");
                } else {
                    tvProgressAvg.setText(String.format(Locale.getDefault(), "%+d%%", progressAvg));
                }
                int progressMax = (int) Math.round((double) amtMax / mTotalAmt * 100);
                tvProgressMax.setText(String.format(Locale.getDefault(), "%+d%%", progressMax));

            }

            long timeAvg = Math.round((double) totalTime / timeTrackedTimes);
            tvTimeAvg.setText(Util.getFormattedTimeAmt(timeAvg, this));
            tvTimeMax.setText(Util.getFormattedTimeAmt(timeMax, this));

        }));
    }

    @Override
    protected void onDestroy() {
        if (!mSub.isUnsubscribed()) {
            mSub.unsubscribe();
        }
        super.onDestroy();
    }

    //Единицы графика
    public enum ChartUnits {TIME, PROGRESS}
}
