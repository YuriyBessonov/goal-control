package app.warinator.goalcontrol.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.Project;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.SimpleColorDialog;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Редактирование проекта
 */
public class ProjectEditDialogFragment extends DialogFragment implements SimpleDialog.OnDialogResultListener {
    private static final String TAG_DIALOG_DATE = "dialog_date";
    private static final String TAG_COLOR_PICKER = "color_picker";

    private static final String ARG_PROJECT = "project";
    private static final String DIALOG_PICK_CATEGORY = "pick_category";
    private static final String DIALOG_PICK_PARENT = "pick_parent";

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
    @BindView(R.id.btn_reset_category)
    ImageButton btnResetCategory;
    @BindView(R.id.tv_deadline)
    TextView tvDeadline;
    @BindView(R.id.tv_parent)
    TextView tvParent;
    @BindView(R.id.tv_category)
    TextView tvCategory;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.til_name)
    TextInputLayout tilName;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;

    @BindViews({R.id.btn_remove_date, R.id.btn_remove_parent, R.id.btn_reset_category})
    List<ImageButton> resetButtons;

    @ColorInt
    int[] mPalette;
    private Project mProject;
    private Project mProjectNew;
    private boolean mNewOne;
    private CompositeSubscription mSub = new CompositeSubscription();
    private OnProjectEditedListener mListener;

    //Обновить дату
    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            btnRemoveDate.setVisibility(View.VISIBLE);
            if (mProjectNew.getDeadline() == null) {
                mProjectNew.setDeadline(Calendar.getInstance());
            }
            mProjectNew.getDeadline().set(year, monthOfYear, dayOfMonth);
            tvDeadline.setText(Util.getFormattedDate(mProjectNew.getDeadline(), getContext()));
        }
    };


    public ProjectEditDialogFragment() {
    }

    public static ProjectEditDialogFragment newInstance(Project project) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROJECT, project);
        ProjectEditDialogFragment fragment = new ProjectEditDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mProject = (Project) getArguments().getSerializable(ARG_PROJECT);
            if (mProject != null){
                mNewOne = (mProject.getId() == 0);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProjectEditedListener){
            mListener = (OnProjectEditedListener)context;
        }
        else {
            throw new RuntimeException("Родитель должен реализовывать"+
                    OnProjectEditedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_project_edit_dialog, container, false);
        ButterKnife.bind(this, v);

        initPalette();
        ButterKnife.apply(resetButtons, Util.VISIBILITY, View.INVISIBLE);
        tvDeadline.setText(R.string.not_defined);
        tvParent.setText(R.string.not_defined);
        tvCategory.setText(R.string.common);

        if (mNewOne){
            mProjectNew = new Project();
        }
        else {
            mProjectNew = new Project(mProject);
            etName.setText(mProject.getName());
            if (mProject.getDeadline() != null) {
                btnRemoveDate.setVisibility(View.VISIBLE);
                tvDeadline.setText(Util.getFormattedDate(mProjectNew.getDeadline(), getContext()));
            }
            if (mProject.getCategoryId() > 0) {
                mSub.add(CategoryDAO.getDAO().get(mProject.getCategoryId()).subscribe(new Action1<Category>() {
                    @Override
                    public void call(Category category) {
                        setCategory(category);
                    }
                }));
            }
            if (mProject.getParentId() > 0) {
                mSub.add(ProjectDAO.getDAO().get(mProject.getParentId()).subscribe(new Action1<Project>() {
                    @Override
                    public void call(Project parent) {
                       setParent(parent);
                    }
                }));
            }
        }
        setColor(mProjectNew.getColor());

        laColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });
        laDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        laCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickCategory();
            }
        });
        laParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickParent();
            }
        });
        btnRemoveDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDate();
            }
        });
        btnRemoveParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeParent();
            }
        });
        btnResetCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCategory();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmIfNameIsUnique();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mSub.add(RxTextView.textChanges(etName).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                validateName();
            }
        }));
        return v;
    }

    private void validateName() {
        if (Util.editTextIsEmpty(etName)) {
            tilName.setError(getString(R.string.err_name_not_specified));
            btnOk.setEnabled(false);
        } else {
            tilName.setErrorEnabled(false);
            btnOk.setEnabled(true);
        }
    }

    private void confirmIfNameIsUnique(){
        mSub.add(ProjectDAO.getDAO().exists(etName.getText().toString()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean exists) {
                if (!exists || etName.getText().toString().equals(mProject.getName())){
                    mProjectNew.setName(etName.getText().toString());
                    mListener.onProjectEdited(mProjectNew);
                    dismiss();
                }
                else {
                    tilName.setError(getString(R.string.name_should_be_unique));
                    btnOk.setEnabled(false);
                }
            }
        }));
    }

    //Задание палитры и добавление цвета по умолчанию
    private void initPalette() {
        mPalette = getResources().getIntArray(R.array.palette_projects);
    }

    //Выбор цвета
    private void showColorPicker() {
        SimpleColorDialog.build()
                .title(R.string.pick_color)
                .colors(mPalette)
                .choicePreset(mProjectNew.getColor())
                .show(this, TAG_COLOR_PICKER);
    }

    //Задание цвета
    private void setColor(int pos) {
        mProjectNew.setColor(pos);
        laHeader.setBackgroundColor(mPalette[pos]);
    }

    private void pickCategory() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        CategoriesDialogFragment fragment = CategoriesDialogFragment.newInstance();
        fragment.show(ft, DIALOG_PICK_CATEGORY);
    }

    private void pickParent() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ProjectsDialogFragment fragment = ProjectsDialogFragment.newInstance();
        fragment.show(ft, DIALOG_PICK_PARENT);
    }

    public void setCategory(Category category){
        mProjectNew.setCategoryId(category.getId());
        tvCategory.setText(category.getName());
        btnResetCategory.setVisibility(View.VISIBLE);
    }

    public void setParent(Project parent) {
        if (mProjectNew.getId() == parent.getId()){
            Toast.makeText(getContext(),
                    getString(R.string.project_cannot_be_the_parent_of_itself),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mProjectNew.setParentId(parent.getId());
        tvParent.setText(parent.getName());
        btnRemoveParent.setVisibility(View.VISIBLE);
    }


    private void resetCategory() {
        btnResetCategory.setVisibility(View.INVISIBLE);
        tvCategory.setText(R.string.common);
        mProjectNew.setCategoryId(0);
    }

    private void removeParent() {
        btnRemoveParent.setVisibility(View.INVISIBLE);
        tvParent.setText(R.string.not_specified);
        mProjectNew.setParentId(0);
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
        mProjectNew.setDeadline(null);
        tvDeadline.setText(R.string.not_defined);
    }

    //Выбор даты
    private void showDatePicker() {
        Calendar date = (mProjectNew.getDeadline() == null) ? Calendar.getInstance() : mProjectNew.getDeadline();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                onDateSetListener,
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getActivity().getFragmentManager(), TAG_DIALOG_DATE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSub.unsubscribe();
    }

    public interface OnProjectEditedListener {
        void onProjectEdited(Project project);
    }
}

