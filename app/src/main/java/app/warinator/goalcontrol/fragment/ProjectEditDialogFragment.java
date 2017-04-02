package app.warinator.goalcontrol.fragment;


import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.SimpleColorDialog;

/**
 * Редактирование проекта
 */
public class ProjectEditDialogFragment extends DialogFragment implements SimpleDialog.OnDialogResultListener {
    private static final String TAG_DIALOG_DATE = "dialog_date";
    private static final String TAG_COLOR_PICKER = "color_picker";
    @BindView(R.id.la_project_dialog_header)
    FrameLayout laHeader;
    @BindView(R.id.la_deadline)
    RelativeLayout laDeadline;
    @BindView(R.id.la_parent)
    RelativeLayout laParent;
    @BindView(R.id.la_color)
    RelativeLayout laColor;
    @BindView(R.id.la_category)
    RelativeLayout laCategory;
    @BindView(R.id.btn_remove_date)
    ImageButton btnRemoveDate;
    @BindView(R.id.btn_remove_parent)
    ImageButton btnRemoveParent;
    @BindView(R.id.btn_reset_color)
    ImageButton btnResetColor;
    @BindView(R.id.btn_reset_category)
    ImageButton btnResetCategory;
    @BindView(R.id.tv_deadline)
    TextView tvDeadline;
    @BindView(R.id.tv_color)
    TextView tvColor;

    @ColorInt
    int[] mPalette;
    private int mColorPos = 0;
    private Calendar mDate;

    //Обновить дату
    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            btnRemoveDate.setVisibility(View.VISIBLE);
            if (mDate == null) {
                mDate = Calendar.getInstance();
            }
            mDate.set(year, monthOfYear, dayOfMonth);
            tvDeadline.setText(Util.getFormattedDate(mDate, getContext()));
        }
    };

    public ProjectEditDialogFragment() {}

    public static ProjectEditDialogFragment newInstance() {
        return new ProjectEditDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_project_edit_dialog, container, false);
        ButterKnife.bind(this, v);
        laColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });
        btnResetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetColor();
            }
        });
        btnRemoveDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDate();
            }
        });
        laDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPalette();
    }

    //Задание палитры и добавление цвета по умолчанию
    private void initPalette() {
        mPalette = getResources().getIntArray(R.array.palette_material);
        mPalette[0] = ContextCompat.getColor(getContext(), R.color.colorPrimary);
    }

    //Выбор цвета
    private void showColorPicker() {
        SimpleColorDialog.build()
                .title(R.string.pick_color)
                .colors(mPalette)
                .choicePreset(mColorPos)
                .show(this, TAG_COLOR_PICKER);
    }

    //Задание цвета
    private void setColor(int pos) {
        mColorPos = pos;
        laHeader.setBackgroundColor(mPalette[mColorPos]);
        if (mColorPos == 0) {
            btnResetColor.setVisibility(View.INVISIBLE);
            tvColor.setVisibility(View.VISIBLE);
        } else {
            btnResetColor.setVisibility(View.VISIBLE);
            tvColor.setVisibility(View.INVISIBLE);
        }
    }

    private void resetColor() {
        setColor(0);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (dialogTag.equals(TAG_COLOR_PICKER)) {
            int pos = extras.getInt(SimpleColorDialog.SELECTED_SINGLE_POSITION);
            setColor(pos);
        }
        return false;
    }

    private void removeDate() {
        btnRemoveDate.setVisibility(View.INVISIBLE);
        mDate = null;
        tvDeadline.setText(R.string.not_defined);
    }

    //Выбор даты
    private void showDatePicker() {
        Calendar date = (mDate == null) ? Calendar.getInstance() : mDate;
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                onDateSetListener,
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getActivity().getFragmentManager(), TAG_DIALOG_DATE);
    }

}

