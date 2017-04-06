package app.warinator.goalcontrol.fragment;

import android.content.DialogInterface;
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
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.color.SimpleColorDialog;
import rx.functions.Action1;

public class CategoryEditDialogFragment extends DialogFragment implements SimpleDialog.OnDialogResultListener {
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
    public CategoryEditDialogFragment() {
    }

    public static CategoryEditDialogFragment newInstance(Category category, boolean newOne, Action1<Category> resultAction) {
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
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategory.setColor(mColor);
                mCategory.setName(etName.getText().toString());
                mResAction.call(mCategory);
                dismiss();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
        laColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

        RxTextView.textChanges(etName).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                validateName();
            }
        });

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

    private void showDeleteConfirmationDialog() {
        Util.showConfirmationDialog(getString(R.string.do_delete), getContext(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mResAction.call(null);
                dismiss();
                CategoryEditDialogFragment.this.dismiss();
            }
        });
    }

    private void showColorPicker() {
        SimpleColorDialog.build()
                .title(R.string.pick_color)
                .colors(getResources().getIntArray(R.array.palette_material_light))
                .show(this, TAG_COLOR_PICKER);
    }

    private void setColor(int pos) {
        int color = getResources().getIntArray(R.array.palette_material_light)[pos];
        mColor = pos;
        laDialogHeader.setBackgroundColor(color);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (dialogTag.equals(TAG_COLOR_PICKER)) {
            int pos = extras.getInt(SimpleColorDialog.SELECTED_SINGLE_POSITION);
            setColor(pos);
        }
        return false;
    }


}
