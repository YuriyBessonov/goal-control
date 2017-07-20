package app.warinator.goalcontrol.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.UnitsAutocompleteAdapter;
import app.warinator.goalcontrol.database.DAO.TrackUnitDAO;
import app.warinator.goalcontrol.model.CheckListItem;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.model.TrackUnit;
import app.warinator.goalcontrol.ui_components.DelayAutocompleteTextView;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import rx.subscriptions.CompositeSubscription;

import static app.warinator.goalcontrol.model.Task.ProgressTrackMode;
import static app.warinator.goalcontrol.model.Task.ProgressTrackMode.LIST;
import static app.warinator.goalcontrol.model.Task.ProgressTrackMode.PERCENT;

/**
 * Фрагмент настройки учета прогресса задачи
 */
public class TaskProgressConfDialogFragment extends DialogFragment {
    private static final String TAG_DIALOG_LIST = "dialog_list_edit";
    private static final int DIALOG_AMT_TOTAL = 1;
    private static final int DIALOG_AMT_ONCE = 2;
    private static final int MAX_PERCENT = 100;
    private static final int MIN_VALUE = 1;
    private static final int POS_MANUAL = 0;
    private static final int POS_AUTO = 1;
    private static final String ARG_TASK = "task";
    private static final String ARG_PROGR_MODE = "progr_mode";
    private static final String ARG_UNITS = "units";
    private static final String ARG_AMT_TOTAL = "amt_total";
    private static final String ARG_AMT_ONCE = "amt_once";
    private static final String ARG_REP_COUNT = "rep_count";
    private static final String ARG_TODO_LIST = "todo_list";
    @BindView(R.id.sp_track)
    Spinner spTrackType;
    @BindView(R.id.btn_edit_amount_once)
    ImageButton btnEditAmountOnce;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.tv_amount_total)
    TextView tvAmountTotal;
    @BindView(R.id.tv_amount_total_lbl)
    TextView tvAmountTotalLbl;
    @BindView(R.id.tv_amount_once)
    TextView tvAmountOnce;
    @BindView(R.id.tv_amount_once_lbl)
    TextView tvAmountOnceLbl;
    @BindView(R.id.tv_list_items)
    TextView tvListItems;
    @BindView(R.id.la_track)
    RelativeLayout laTrackType;
    @BindView(R.id.la_amount_total)
    RelativeLayout laAmountTotal;
    @BindView(R.id.la_amount_total_sep)
    View laAmountTotalSep;
    @BindView(R.id.la_list_setup)
    RelativeLayout laListSetup;
    @BindView(R.id.la_list_setup_sep)
    View laListSetupSep;
    @BindView(R.id.la_units)
    LinearLayout laUnits;
    @BindView(R.id.actv_units_full)
    DelayAutocompleteTextView actvUnitsFull;
    @BindView(R.id.et_units_short)
    EditText etUnitsShort;
    @BindView(R.id.la_units_sep)
    View laUnitsSep;
    @BindView(R.id.la_amount_once)
    RelativeLayout laAmountOnce;
    @BindView(R.id.la_amount_once_sep)
    View laAmountOnceSep;
    @BindView(R.id.rbg_amount_setup)
    RadioRealButtonGroup rbgAmountSetup;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    private ChecklistDialogFragment mListEditFragment;
    private ProgressTrackMode mTrackMode;
    private CompositeSubscription mSub = new CompositeSubscription();
    private View.OnClickListener onLaTrackTypeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            spTrackType.performClick();
        }
    };
    private OnTaskProgressConfiguredListener mListener;
    private long mTaskId;
    private TrackUnit mUnits;
    private int mAmountTotal;
    private int mAmountOnce;
    private int mTaskRepeatCount;
    private boolean mAmountAuto;
    private ArrayList<CheckListItem> mTodoList;
    private int mListItemsCount;

    //Редактирование списка пунктов
    private View.OnClickListener onLaListSetupClick = v -> showListEditDialog();
    //NumberPicker'ы
    private View.OnClickListener onLaAmountTotalClick = v -> showNumberEditDialog(DIALOG_AMT_TOTAL);
    private View.OnClickListener onLaAmountOnceClick = v -> showNumberEditDialog(DIALOG_AMT_ONCE);
    //Выбор типа учета
    private AdapterView.OnItemSelectedListener onTrackTypeSelected =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateMode(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };
    private View.OnClickListener onOkBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mTrackMode == LIST && mListItemsCount == 0) {
                Toast.makeText(getContext(), R.string.list_must_not_be_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            if (mAmountAuto) {
                mAmountOnce = 0;
            }
            processUnits();
            mListener.onTaskProgressConfigured(mTrackMode, mUnits, mAmountTotal, mAmountOnce);
            dismiss();
        }
    };

    public TaskProgressConfDialogFragment() {
    }

    public static TaskProgressConfDialogFragment newInstance(long taskId, Task.ProgressTrackMode mode,
                                                             long unitsId, int amountTotal,
                                                             int amountOnce, int taskRepeatCount,
                                                             ArrayList<CheckListItem> todoList) {
        TaskProgressConfDialogFragment fragment = new TaskProgressConfDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TASK, taskId);
        args.putInt(ARG_PROGR_MODE, mode.ordinal());
        args.putLong(ARG_UNITS, unitsId);
        args.putInt(ARG_AMT_TOTAL, amountTotal);
        args.putInt(ARG_AMT_ONCE, amountOnce);
        args.putInt(ARG_REP_COUNT, taskRepeatCount);
        if (todoList != null) {
            args.putParcelableArrayList(ARG_TODO_LIST, todoList);
        }
        fragment.setArguments(args);
        return fragment;
    }

    //Обработать введенные значения единиц учета
    private void processUnits() {
        if (mUnits == null &&
                (!Util.editTextIsEmpty(actvUnitsFull) ||
                        !Util.editTextIsEmpty(etUnitsShort))) {
            mUnits = new TrackUnit();
        }

        if (!Util.editTextIsEmpty(actvUnitsFull)) {
            String inpFull = actvUnitsFull.getText().toString();
            if (!inpFull.equals(mUnits.getName())) {
                mUnits.setName(inpFull);
                mUnits.setId(0);
            }
            if (!Util.editTextIsEmpty(etUnitsShort)) {
                mUnits.setShortName(etUnitsShort.getText().toString());
            } else {
                String shortName = Util.makeShortName(inpFull);
                etUnitsShort.setText(shortName);
                mUnits.setShortName(shortName);
            }
        } else if (!Util.editTextIsEmpty(etUnitsShort)) {
            String name = etUnitsShort.getText().toString();
            mUnits = new TrackUnit(0, name, name);
            actvUnitsFull.setText(name);
        } else {
            mUnits = null;
        }

    }

    //Инициализировать view значениями из Bundle
    private void applyBundle(Bundle b) {
        mTaskId = b.getLong(ARG_TASK);
        mTrackMode = ProgressTrackMode.values()[b.getInt(ARG_PROGR_MODE)];
        spTrackType.setSelection(mTrackMode.ordinal());

        long unitsId = b.getLong(ARG_UNITS);
        if (unitsId > 0) {
            TrackUnitDAO.getDAO().get(unitsId).subscribe(trackUnit -> {
                mUnits = trackUnit;
                actvUnitsFull.setText(mUnits.getName());
                etUnitsShort.setText(mUnits.getShortName());
            });
        }

        if (b.getParcelableArrayList(ARG_TODO_LIST) != null) {
            mTodoList = b.getParcelableArrayList(ARG_TODO_LIST);
            updateTodoListItemsCount(mTodoList.size());
        }

        mTaskRepeatCount = b.getInt(ARG_REP_COUNT);
        mAmountTotal = b.getInt(ARG_AMT_TOTAL);
        if (mAmountTotal == 0) {
            mAmountTotal = mTaskRepeatCount;
        }
        tvAmountTotal.setText(String.valueOf(mAmountTotal));

        mAmountOnce = b.getInt(ARG_AMT_ONCE);
        mAmountAuto = false;
        if (mAmountOnce == 0) {//предполагается режим "авто"
            mAmountAuto = true;
            setAmountOnce(getAutoAmountOnce());
        } else {
            tvAmountOnce.setText(String.valueOf(mAmountOnce));
        }

        rbgAmountSetup.setPosition(mAmountAuto ? POS_AUTO : POS_MANUAL);
    }

    //Рассчитать однократный объём выполнения
    private int getAutoAmountOnce() {
        return (int) Math.ceil((double) mAmountTotal / (double) mTaskRepeatCount);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_progress_conf_dialog, container, false);
        ButterKnife.bind(this, v);
        tvDialogTitle.setText(R.string.task_option_progress);
        laTrackType.setOnClickListener(onLaTrackTypeClick);
        laAmountTotal.setOnClickListener(onLaAmountTotalClick);
        btnEditAmountOnce.setOnClickListener(onLaAmountOnceClick);
        spTrackType.setOnItemSelectedListener(onTrackTypeSelected);
        laListSetup.setOnClickListener(onLaListSetupClick);
        rbgAmountSetup.setOnPositionChangedListener((button, position) -> initAmountOnce(position));
        btnCancel.setOnClickListener(v1 -> dismiss());
        btnOk.setOnClickListener(onOkBtnClick);
        prepareTrackTypes();

        if (savedInstanceState != null) {
            applyBundle(savedInstanceState);
        } else if (getArguments() != null) {
            applyBundle(getArguments());
        }

        updateMode();
        setUnitsAutocompletion();
        return v;
    }

    //Указывать единицы за раз автоматически или по умолчанию
    private void initAmountOnce(int pos) {
        if (pos == POS_AUTO) {
            mAmountAuto = true;
            setAmountOnce(getAutoAmountOnce());
            tvAmountOnce.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGrey));
            btnEditAmountOnce.setVisibility(View.INVISIBLE);
        } else {
            mAmountAuto = false;
            tvAmountOnce.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGreyDark));
            btnEditAmountOnce.setVisibility(View.VISIBLE);
        }
    }

    //Задать донократный объем выполнения
    private void setAmountOnce(int amount) {
        mAmountOnce = amount;
        tvAmountOnce.setText(String.valueOf(mAmountOnce));
    }

    //Задать общий объем выполнения
    private void setAmountTotal(int amount) {
        mAmountTotal = amount;
        tvAmountTotal.setText(String.valueOf(mAmountTotal));
    }

    //Настроить типы учета
    private void prepareTrackTypes() {
        String[] trackModes = getResources().getStringArray(R.array.progress_track_mode);
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<String>(getContext(), R.layout.custom_spinner_item, trackModes);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTrackType.setAdapter(spinnerArrayAdapter);
    }

    //Отобразить диалог редактирования списка дел
    private void showListEditDialog() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        if (mListEditFragment == null) {
            mListEditFragment = ChecklistDialogFragment.getInstance(mTaskId, mTodoList, true);
        }
        mListEditFragment.show(ft, TAG_DIALOG_LIST);
    }

    //Задать тип учета
    private void updateMode() {
        int pos = spTrackType.getSelectedItemPosition();
        updateMode(pos);
    }

    //Обноаить режим учета, соответствующий выбранной позиции в списке
    private void updateMode(int pos) {
        mTrackMode = ProgressTrackMode.values()[pos];
        if (mTrackMode == PERCENT) {
            setAmountTotal(MAX_PERCENT);
            if (mAmountAuto) {
                setAmountOnce(getAutoAmountOnce());
            } else if (mAmountOnce > MAX_PERCENT) {
                setAmountOnce(MAX_PERCENT);
            }
        }
        if (mTrackMode == ProgressTrackMode.UNITS) {
            laUnits.setVisibility(View.VISIBLE);
            laUnitsSep.setVisibility(View.VISIBLE);
        } else {
            laUnits.setVisibility(View.GONE);
            laUnitsSep.setVisibility(View.GONE);
        }
        if (mTrackMode == ProgressTrackMode.UNITS) {
            laAmountTotal.setVisibility(View.VISIBLE);
            laAmountTotalSep.setVisibility(View.VISIBLE);
            if (mAmountOnce > mAmountTotal) {
                setAmountTotal(mAmountOnce);
            }
        } else {
            laAmountTotal.setVisibility(View.GONE);
            laAmountTotalSep.setVisibility(View.GONE);
        }
        if (mTrackMode == ProgressTrackMode.UNITS || mTrackMode == ProgressTrackMode.PERCENT) {
            laAmountOnce.setVisibility(View.VISIBLE);
            laAmountOnceSep.setVisibility(View.VISIBLE);
        } else {
            laAmountOnce.setVisibility(View.GONE);
            laAmountOnceSep.setVisibility(View.GONE);
        }
        if (mTrackMode == ProgressTrackMode.LIST) {
            laListSetup.setVisibility(View.VISIBLE);
            laListSetupSep.setVisibility(View.VISIBLE);
            setAmountOnce(MIN_VALUE);
        } else {
            laListSetup.setVisibility(View.GONE);
            laListSetupSep.setVisibility(View.GONE);
        }
    }


    //Настроить автодополнение единиц учета
    private void setUnitsAutocompletion() {
        actvUnitsFull.setThreshold(2);
        actvUnitsFull.setAdapter(new UnitsAutocompleteAdapter(getContext()));
        actvUnitsFull.setOnItemClickListener((parent, view, position, id) -> {
            mUnits = (TrackUnit) parent.getItemAtPosition(position);
            actvUnitsFull.setText(mUnits.getName());
            etUnitsShort.setText(mUnits.getShortName());
        });
    }

    //Обноаить размер списка дел
    public void updateTodoListItemsCount(int newCount) {
        mListItemsCount = newCount;
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.elements)).append(": ").append(newCount);
        tvListItems.setText(sb.toString());
    }

    //Отобразить диалог редактирования числа
    private void showNumberEditDialog(int tag) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        DialogInterface.OnClickListener clickListener;
        final EditText input = new EditText(getContext());
        if (tag == DIALOG_AMT_ONCE) {
            input.setText(String.valueOf(mAmountOnce));
            alert.setTitle(R.string.per_one_session);
            clickListener = (dialog, which) -> {
                int newValue = Integer.parseInt(input.getText().toString());
                if (newValue > 0) {
                    if (newValue > mAmountTotal) {
                        if (mTrackMode == ProgressTrackMode.PERCENT) {
                            newValue = mAmountTotal;
                        } else {
                            setAmountTotal(newValue);
                        }
                    }
                    setAmountOnce(newValue);
                }
            };
        } else {
            alert.setTitle(R.string.total_amount);
            input.setText(String.valueOf(mAmountTotal));
            clickListener = (dialog, which) -> {
                int newValue = Integer.parseInt(input.getText().toString());
                if (newValue > 0) {
                    setAmountTotal(newValue);
                    if (mAmountAuto) {
                        setAmountOnce(getAutoAmountOnce());
                    } else if (newValue < mAmountOnce) {
                        setAmountOnce(newValue);
                    }
                }
            };
        }
        LinearLayout la = new LinearLayout(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        input.setGravity(Gravity.CENTER);
        input.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        la.addView(input);
        alert.setView(la);
        alert.setPositiveButton(getString(R.string.okay), clickListener);
        alert.setNegativeButton(getString(R.string.cancel), (dialog, whichButton) -> {
        });
        alert.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTaskProgressConfiguredListener) {
            mListener = (OnTaskProgressConfiguredListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + getString(R.string.must_implement)
                    + OnTaskProgressConfiguredListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnTaskProgressConfiguredListener {
        void onTaskProgressConfigured(Task.ProgressTrackMode mode, TrackUnit units, int amountTotal,
                                      int amountOnce);
    }
}
