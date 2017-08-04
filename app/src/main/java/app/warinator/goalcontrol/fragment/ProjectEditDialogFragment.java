package app.warinator.goalcontrol.fragment;


import android.app.Dialog;
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

import com.jakewharton.rxbinding.widget.RxTextView;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.model.Project;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.SimpleColorDialog;
import es.dmoral.toasty.Toasty;
import rx.subscriptions.CompositeSubscription;

/**
 * Фрагмент редактирования проекта
 */
public class ProjectEditDialogFragment extends DialogFragment implements SimpleDialog.OnDialogResultListener {
    private static final String TAG_COLOR_PICKER = "color_picker";

    private static final String ARG_PROJECT = "project";
    private static final String DIALOG_PICK_PARENT = "pick_parent";

    @BindView(R.id.la_project_dialog_header)
    FrameLayout laHeader;
    @BindView(R.id.la_parent)
    RelativeLayout laParent;
    @BindView(R.id.la_color)
    RelativeLayout laColor;
    @BindView(R.id.tv_parent)
    TextView tvParent;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.til_name)
    TextInputLayout tilName;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_remove_parent)
    ImageButton btnRemoveParent;

    @ColorInt
    int[] mPalette;
    private Project mProject;
    private Project mProjectNew;
    private boolean mNewOne;
    private CompositeSubscription mSub = new CompositeSubscription();
    private OnProjectEditedListener mListener;


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
            if (mProject != null) {
                mNewOne = (mProject.getId() == 0);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProjectEditedListener) {
            mListener = (OnProjectEditedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + getString(R.string.must_implement)
                    + OnProjectEditedListener.class.getSimpleName());
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
        btnRemoveParent.setVisibility(View.INVISIBLE);
        tvParent.setText(R.string.not_defined);

        if (mNewOne) {
            mProjectNew = new Project();
        } else {
            mProjectNew = new Project(mProject);
            etName.setText(mProject.getName());
            if (mProject.getParentId() > 0) {
                mSub.add(ProjectDAO.getDAO().get(mProject.getParentId()).subscribe(this::setParent));
            }
        }
        setColor(mProjectNew.getColor());

        laColor.setOnClickListener(v1 -> showColorPicker());
        laParent.setOnClickListener(v14 -> pickParent());
        btnRemoveParent.setOnClickListener(v16 -> removeParent());
        btnOk.setOnClickListener(v18 -> confirmIfNameIsUnique());
        btnCancel.setOnClickListener(v19 -> dismiss());
        mSub.add(RxTextView.textChanges(etName).subscribe(charSequence -> validateName()));
        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Util.disableTitle(dialog);
        return dialog;
    }


    //Проверить корректность имени
    private void validateName() {
        if (Util.editTextIsEmpty(etName)) {
            tilName.setError(getString(R.string.err_name_not_specified));
            btnOk.setEnabled(false);
        } else {
            tilName.setErrorEnabled(false);
            btnOk.setEnabled(true);
        }
    }

    //Проверить уникальность имени
    private void confirmIfNameIsUnique() {
        mSub.add(ProjectDAO.getDAO().exists(etName.getText().toString()).subscribe(exists -> {
            if (!exists || etName.getText().toString().equals(mProject.getName())) {
                mProjectNew.setName(etName.getText().toString());
                mListener.onProjectEdited(mProjectNew);
                dismiss();
            } else {
                tilName.setError(getString(R.string.name_should_be_unique));
                btnOk.setEnabled(false);
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

    //Отобразить диалог выбора родительского проекта
    private void pickParent() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ProjectsDialogFragment fragment = ProjectsDialogFragment.newInstance();
        fragment.show(ft, DIALOG_PICK_PARENT);
    }

    //Задать родителя проекту
    public void setParent(Project parent) {
        if (mProjectNew.getId() == parent.getId()) {
            Toasty.error(getContext(),
                    getString(R.string.project_cannot_be_the_parent_of_itself)).show();
            return;
        }
        mProjectNew.setParentId(parent.getId());
        tvParent.setText(parent.getName());
        btnRemoveParent.setVisibility(View.VISIBLE);
    }


    //Задать родительский проект по умолчанию
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSub.unsubscribe();
    }

    public interface OnProjectEditedListener {
        void onProjectEdited(Project project);
    }
}

