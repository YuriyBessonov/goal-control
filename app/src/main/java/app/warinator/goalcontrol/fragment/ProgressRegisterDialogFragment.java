package app.warinator.goalcontrol.fragment;


import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.xw.repo.BubbleSeekBar;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.model.ConcreteTask;
import app.warinator.goalcontrol.model.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import rx.Observable;
import rx.functions.Func1;

public class ProgressRegisterDialogFragment extends DialogFragment {
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.tv_sign)
    TextView tvSign;
    @BindView(R.id.til_today_progress)
    TextInputLayout tilTodayProgress;
    @BindView(R.id.et_today_progress)
    EditText etTodayProgress;
    @BindView(R.id.tv_percent)
    TextView tvPercent;
    @BindView(R.id.tv_progress_comment)
    TextView tvProgressComment;
    @BindView(R.id.tv_all_done)
    TextView tvAllDone;
    @BindView(R.id.tv_all_need)
    TextView tvAllNeed;
    @BindView(R.id.tv_units)
    TextView tvUnits;
    @BindView(R.id.sb_progress)
    BubbleSeekBar sbProgress;

    private static final String ARG_TASK = "task";

    long mId;
    private ConcreteTask mConcreteTask;
    private int mAllDone;
    private int mAllNeed;
    private int mDoneToday;
    private int mNeedToday;

    public ProgressRegisterDialogFragment() {
    }


    public static ProgressRegisterDialogFragment newInstance(long concreteTaskId) {
        ProgressRegisterDialogFragment fragment = new ProgressRegisterDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TASK, concreteTaskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            mId = savedInstanceState.getLong(ARG_TASK);
        }
        else {
            mId = getArguments().getLong(ARG_TASK);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_progress_register_dialog, container, false);
        ButterKnife.bind(this, v);


        ConcreteTaskDAO.getDAO().get(mId).concatMap(new Func1<ConcreteTask, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(ConcreteTask cTask) {
                mConcreteTask = cTask;
                return ConcreteTaskDAO.getDAO().getTotalAmountDone(cTask.getTask().getId());
            }
        }).concatMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer allDone) {
                mAllDone = allDone;
                return ConcreteTaskDAO.getDAO().getTimesLeftStartingToday(mConcreteTask.getTask().getId());
            }
        }).subscribe(timesLeft -> {
            if (mConcreteTask.getTask().getProgressTrackMode() == Task.ProgressTrackMode.UNITS){
                if (mConcreteTask.getTask().getUnits() != null){
                    tilTodayProgress.setHint(mConcreteTask.getTask().getUnits().getName());
                    tvUnits.setText(mConcreteTask.getTask().getUnits().getShortName());
                }
                else {
                    tilTodayProgress.setHint(getString(R.string.units));
                    tvUnits.setText(getString(R.string.units));
                }

                tvPercent.setVisibility(View.GONE);
            }
            else {
                tilTodayProgress.setHint("");
                tvUnits.setText(R.string.percents);
            }

            mAllNeed = mConcreteTask.getTask().getAmountTotal();
            tvAllNeed.setText(String.valueOf(mAllNeed));
            sbProgress.getConfigBuilder().max(mAllNeed).build();

            tvAllDone.setText(String.valueOf(mAllDone));
            sbProgress.setProgress(mAllDone);

            mNeedToday = mConcreteTask.getAmtToday();
        });

        tvDialogTitle.setText(R.string.progress_today);
        etTodayProgress.setText(String.valueOf(0));

        sbProgress.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnActionUp(int progress, float progressFloat) {
                tvAllDone.setText(String.valueOf(progress));
                mDoneToday = progress - mAllDone;
                etTodayProgress.setText(String.valueOf(Math.abs(mDoneToday)));

            }

            @Override
            public void getProgressOnFinally(int progress, float progressFloat) {
                tvAllDone.setText(String.valueOf(progress));
            }
        });


        RxTextView.textChanges(etTodayProgress).subscribe(charSequence -> {
            int newVal = 0;
            if (charSequence.length() > 0){
                newVal = Integer.parseInt(charSequence.toString());
                if (newVal > mAllNeed - mAllDone && mDoneToday > 0){
                    etTodayProgress.setText(String.valueOf(mAllNeed - mAllDone));
                    return;
                }
            }
            if (newVal != Math.abs(mDoneToday)){
                mDoneToday = newVal;
                sbProgress.setProgress(mAllDone + mDoneToday);
            }
            if (mDoneToday > 0){
                tvSign.setText(R.string.plus);
                tvSign.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
            }
            else if (mDoneToday < 0){
                tvSign.setText(R.string.minus);
                tvSign.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            }
            else {
                tvSign.setText("");
            }
            tvProgressComment.setText(getProgressComment());
        });


        btnCancel.setOnClickListener(v1 -> dismiss());
        btnOk.setOnClickListener(v12 -> {
            mConcreteTask.setAmountDone(mConcreteTask.getAmountDone() + mDoneToday);
            ConcreteTaskDAO.getDAO().update(mConcreteTask)
            .subscribe(integer -> {
                Toasty.success(getContext(),getString(R.string.progress_registered)).show();
                dismiss();
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.remove_task_from_the_list)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, (dialog, which) -> removeTask())
                        .setNegativeButton(R.string.not_yet, null).show();
            });
        });
        return v;
    }

    private void removeTask(){
        ConcreteTaskDAO.getDAO()
                .markAsRemoved(mConcreteTask.getId()).subscribe(aInt -> {} );
    }


    private String getProgressComment(){
        String s;
        if (mDoneToday < 0){
            s = getString(R.string.how_did_it_happen);
        }
        else if (mDoneToday == 0){
            s = "";
        }
        else if (mDoneToday <= mNeedToday*0.3){
            s = getString(R.string.not_bad);
        }
        else if (mDoneToday <= mNeedToday*0.8){
            s = getString(R.string.good);
        }
        else if (mDoneToday <= mNeedToday){
            s = getString(R.string.great);
        }
        else {
            s = getString(R.string.excellent);
        }
        return s;
    }

}
