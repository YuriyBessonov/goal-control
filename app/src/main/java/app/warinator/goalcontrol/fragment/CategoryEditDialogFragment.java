package app.warinator.goalcontrol.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.model.Category;
import app.warinator.goalcontrol.utils.ColorUtil;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.SimpleColorDialog;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Фрагмент редактирования категории
 */
public class CategoryEditDialogFragment extends DialogFragment
        implements SimpleDialog.OnDialogResultListener {
    private static final String TAG_COLOR_PICKER = "color_picker";
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.la_dialog_header_front)
    RelativeLayout laDialogHeaderFront;
    @BindView(R.id.la_color)
    RelativeLayout laColor;
    @BindView(R.id.la_dialog_header)
    FrameLayout laDialogHeader;
    @BindView(R.id.btn_delete)
    Button btnDelete;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.til_name)
    TextInputLayout tilName;
    @BindView(R.id.separator)
    View vSeparator;
    private Category mCategory;
    private boolean mIsNew;
    private int mColor;
    private Action1<Category> mResAction;
    private CompositeSubscription mSub = new CompositeSubscription();

    public CategoryEditDialogFragment() {
    }

    public static CategoryEditDialogFragment newInstance(Category category, boolean newOne,
                                                         Action1<Category> resultAction) {
        CategoryEditDialogFragment fragment = new CategoryEditDialogFragment();
        fragment.mCategory = category;
        fragment.mIsNew = newOne;
        fragment.mResAction = resultAction;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_category_edit_dialog, container, false);
        ButterKnife.bind(this, v);
        if (mIsNew) {
            btnDelete.setVisibility(View.GONE);
            vSeparator.setVisibility(View.GONE);
        }
        tvDialogTitle.setText(R.string.category);
        laDialogHeaderFront.setBackgroundResource(R.drawable.pattern_category_edit_bgr);
        setColor(mCategory.getColor());
        etName.setText(mCategory.getName());
        btnCancel.setOnClickListener(v13 -> dismiss());
        btnOk.setOnClickListener(v14 -> {
            mCategory.setColor(mColor);
            confirmIfNameIsUnique();
        });
        btnDelete.setOnClickListener(v1 -> showDeleteConfirmationDialog());
        laColor.setOnClickListener(v12 -> showColorPicker());

        mSub.add(RxTextView.textChanges(etName).subscribe(charSequence -> validateName()));
        return v;
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
        mSub.add(CategoryDAO.getDAO().exists(etName.getText().toString()).subscribe(exists -> {
            if (!exists || etName.getText().toString().equals(mCategory.getName())) {
                mCategory.setName(etName.getText().toString());
                mResAction.call(mCategory);
                dismiss();
            } else {
                tilName.setError(getContext().getString(R.string.name_should_be_unique));
                btnOk.setEnabled(false);
            }
        }));
    }

    //Отобразить диалог подтверждения удаления
    private void showDeleteConfirmationDialog() {
        Util.showConfirmationDialog(getString(R.string.do_delete), getContext(), (dialog, which) -> {
            mResAction.call(null);
            dismiss();
            CategoryEditDialogFragment.this.dismiss();
        });
    }

    //Отобразить диалог выбора цвета
    private void showColorPicker() {
        SimpleColorDialog.build()
                .title(R.string.pick_color)
                .colors(getResources().getIntArray(R.array.palette_categories))
                .show(this, TAG_COLOR_PICKER);
    }

    //Установить цвет категории
    private void setColor(int pos) {
        int color = ColorUtil.getCategoryColor(pos, getContext());
        mColor = pos;
        laDialogHeader.setBackgroundColor(color);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSub.unsubscribe();
    }

    //Обработка выбранного цвета
    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (dialogTag.equals(TAG_COLOR_PICKER)) {
            int pos = extras.getInt(SimpleColorDialog.SELECTED_SINGLE_POSITION);
            setColor(pos);
        }
        return false;
    }


}
