package app.warinator.goalcontrol.fragment;

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

import app.warinator.goalcontrol.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;


public class TaskProgressConfDialogFragment extends DialogFragment implements ListEditDialogFragment.ListChangedCallback {
    private static final String TAG_DIALOG_LIST = "dialog_list_edit";

    private enum TrackMode {
        MARK, UNITS, PERCENT, SEQUENCE, LIST
    };
    private ListEditDialogFragment mListEditFragment;
    private TrackMode mode;
    private static final int DIALOG_AMT_TOTAL = 1;
    private static final int DIALOG_AMT_ONCE = 2;
    private static final int MAX_PERCENT = 100;
    private static final int MIN_VALUE = 1;
    private static final int POS_MANUAL = 0;
    private static final int POS_AUTO = 1;

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
    @BindView(R.id.la_units_sep)
    View laUnitsSep;
    @BindView(R.id.la_amount_once)
    RelativeLayout laAmountOnce;
    @BindView(R.id.la_amount_once_sep)
    View laAmountOnceSep;

    @BindView(R.id.rbg_amount_setup)
    RadioRealButtonGroup rbgAmountSetup;

    final int[] trackTypesIds = {R.string.track_type_mark, R.string.track_type_units, R.string.track_type_percent,
            R.string.track_type_sequence, R.string.track_type_list};


    public TaskProgressConfDialogFragment() {

    }

    public static TaskProgressConfDialogFragment newInstance() {
        return new TaskProgressConfDialogFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_progress_conf_dialog, container, false);
        ButterKnife.bind(this,v);
        tvDialogTitle.setText(R.string.task_option_progress);
        laTrackType.setOnClickListener(onLaTrackTypeClick);
        laAmountTotal.setOnClickListener(onLaAmountTotalClick);
        btnEditAmountOnce.setOnClickListener(onLaAmountOnceClick);
        spTrackType.setOnItemSelectedListener(onTrackTypeSelected);
        laListSetup.setOnClickListener(onLaListSetupClick);
        rbgAmountSetup.setOnPositionChangedListener(new RadioRealButtonGroup.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(RadioRealButton button, int position) {
                initAmountOnce(position);
            }
        });
        rbgAmountSetup.setPosition(POS_MANUAL);
        prepareTrackTypes();
        updateMode();
        return v;
    }

    private void initAmountOnce(int pos){
        if (pos == POS_AUTO){
            tvAmountOnce.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGrey));
            btnEditAmountOnce.setVisibility(View.INVISIBLE);
        }
        else {
            tvAmountOnce.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreyDark));
            btnEditAmountOnce.setVisibility(View.VISIBLE);
        }
    }

    private void prepareTrackTypes(){
        String[] trackTypes = new String[trackTypesIds.length];
        for (int i=0; i< trackTypes.length; i++){
            trackTypes[i] = getString(trackTypesIds[i]);
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.custom_spinner_item, trackTypes); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTrackType.setAdapter(spinnerArrayAdapter);
    }

    private View.OnClickListener onLaTrackTypeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            spTrackType.performClick();
        }
    };

    private View.OnClickListener onLaAmountTotalClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showNumberEditDialog(DIALOG_AMT_TOTAL);
        }
    };

    private View.OnClickListener onLaAmountOnceClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showNumberEditDialog(DIALOG_AMT_ONCE);
        }
    };

    private View.OnClickListener onLaListSetupClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showListEditDialog();
        }
    };

    private void showListEditDialog(){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        if (mListEditFragment == null){
            mListEditFragment = ListEditDialogFragment.getInstance(this);
        }
        mListEditFragment.show(ft, TAG_DIALOG_LIST);
    }

    private void updateMode(){
        int pos = spTrackType.getSelectedItemPosition();
        updateMode(pos);
    }

    private void updateMode(int pos){
        mode = TrackMode.values()[pos];
        if (mode == TrackMode.PERCENT){
            int onceAmount = Integer.parseInt(tvAmountOnce.getText().toString());
            if (onceAmount > MAX_PERCENT){
                tvAmountOnce.setText(String.valueOf(MAX_PERCENT));
            }
        }
        if (mode == TrackMode.UNITS){
            laUnits.setVisibility(View.VISIBLE);
            laUnitsSep.setVisibility(View.VISIBLE);
        }
        else {
            laUnits.setVisibility(View.GONE);
            laUnitsSep.setVisibility(View.GONE);
        }
        if (mode == TrackMode.UNITS ){
            laAmountTotal.setVisibility(View.VISIBLE);
            laAmountTotalSep.setVisibility(View.VISIBLE);
            int totalAmount = Integer.parseInt(tvAmountTotal.getText().toString());
            int onceAmount = Integer.parseInt(tvAmountOnce.getText().toString());
            if (onceAmount > totalAmount){
                tvAmountTotal.setText(String.valueOf(onceAmount));
            }
        }
        else {
            laAmountTotal.setVisibility(View.GONE);
            laAmountTotalSep.setVisibility(View.GONE);
        }
        if (mode != TrackMode.MARK && mode != TrackMode.SEQUENCE){
            laAmountOnce.setVisibility(View.VISIBLE);
            laAmountOnceSep.setVisibility(View.VISIBLE);
        }
        else {
            laAmountOnce.setVisibility(View.GONE);
            laAmountOnceSep.setVisibility(View.GONE);
        }
        if (mode == TrackMode.LIST){
            laListSetup.setVisibility(View.VISIBLE);
            laListSetupSep.setVisibility(View.VISIBLE);
            tvAmountOnce.setText(String.valueOf(MIN_VALUE));
        }
        else {
            laListSetup.setVisibility(View.GONE);
            laListSetupSep.setVisibility(View.GONE);
        }
    }


    private AdapterView.OnItemSelectedListener onTrackTypeSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            updateMode(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };


    @Override
    public void updateItemsCount(int newCount) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.elements)).append(": ").append(newCount);
        tvListItems.setText(sb.toString());
    }

    private void showNumberEditDialog(int tag){
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        DialogInterface.OnClickListener clickListener;
        final EditText input = new EditText(getContext());
        if (tag == DIALOG_AMT_ONCE){
            input.setText(tvAmountOnce.getText());
            alert.setTitle(R.string.per_one_session);
            clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int totalAmount;
                    if (mode == TrackMode.PERCENT){
                        totalAmount = MAX_PERCENT;
                    }
                    else {
                        totalAmount = Integer.parseInt(tvAmountTotal.getText().toString());
                    }
                    int newValue = Integer.parseInt(input.getText().toString());
                    if (newValue > 0){
                        if (newValue > totalAmount){
                            if (mode == TrackMode.PERCENT){
                                newValue = totalAmount;
                            }
                            else {
                                tvAmountTotal.setText(String.valueOf(newValue));
                            }
                        }
                        tvAmountOnce.setText(String.valueOf(newValue));
                    }
                }
            };
        }
        else {
            alert.setTitle(R.string.total_amount);
            input.setText(tvAmountTotal.getText());
            clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int onceAmount = Integer.parseInt(tvAmountOnce.getText().toString());
                    int newValue = Integer.parseInt(input.getText().toString());
                    if (newValue > 0){
                        if (newValue < onceAmount){
                            tvAmountOnce.setText(String.valueOf(newValue));
                        }
                        tvAmountTotal.setText(String.valueOf(newValue));
                    }
                }
            };
        }
        LinearLayout la = new LinearLayout(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        input.setGravity(Gravity.CENTER);
        input.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        la.addView(input);
        alert.setView(la);
        alert.setPositiveButton(getString(R.string.okay), clickListener);
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }


}
