package app.warinator.goalcontrol.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.CategoriesAdapter;
import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.model.Category;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

/**
 * Фрагмент со списком категорий
 */
public class CategoriesDialogFragment extends DialogFragment
        implements CategoriesAdapter.OnListItemClickListener {
    private static final String TAG_DIALOG_CREATE = "dialog_create";
    private static final String TAG_DIALOG_EDIT = "dialog_edit";
    @BindView(R.id.rv_list)
    RecyclerView rvList;
    @BindView(R.id.la_dialog_header)
    FrameLayout laDialogHeader;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    private CategoriesAdapter mAdapter;
    private CategoryEditDialogFragment mFragment;
    private ArrayList<Category> mValues;
    private CompositeSubscription mSubscription = new CompositeSubscription();
    private boolean mAsDialog;

    public CategoriesDialogFragment() {
    }

    public static CategoriesDialogFragment newInstance() {
        CategoriesDialogFragment fragment = new CategoriesDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsDialog = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAsDialog = true;
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories_list, container, false);
        ButterKnife.bind(this, view);

        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvList.setItemAnimator(new DefaultItemAnimator());
        mValues = new ArrayList<>();
        mAdapter = new CategoriesAdapter(getContext(), this, mValues);
        rvList.setAdapter(mAdapter);
        mSubscription.add(CategoryDAO.getDAO().getAll(false, false).subscribe(categories -> {
            mValues.addAll(categories);
            mAdapter.notifyDataSetChanged();
        }));

        if (mAsDialog) {
            btnOk.setVisibility(View.INVISIBLE);
            tvDialogTitle.setText(R.string.drawer_item_main_categories);
            btnCancel.setOnClickListener(v -> dismiss());
        } else {
            laDialogHeader.setVisibility(View.GONE);
        }

        return view;
    }

    //Отобразить диалог добавления категории
    public void createItem() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        mFragment = CategoryEditDialogFragment.newInstance(new Category(), true, this::addItem);
        mFragment.show(ft, TAG_DIALOG_CREATE);
    }

    //Отобразить диалог редактирования категории
    public void editItem(final int position) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        mFragment = CategoryEditDialogFragment.newInstance(mValues.get(position), false, category -> {
            if (category == null) {
                deleteItem(position);
            } else {
                updateItem(position);
            }
        });
        mFragment.show(ft, TAG_DIALOG_EDIT);
    }

    //Добавить категорию в БД
    private void addItem(final Category category) {
        mSubscription.add(CategoryDAO.getDAO().add(category).subscribe(aLong -> {
            mValues.add(category);
            category.setId(aLong);
            mAdapter.notifyItemInserted(mValues.size() - 1);
        }));
    }

    //Обновить категорию
    private void updateItem(final int position) {
        mSubscription.add(CategoryDAO.getDAO().update(mValues.get(position)).subscribe(integer ->
                mAdapter.notifyItemChanged(position)));
    }

    //Удалить категорию
    private void deleteItem(final int position) {
        Category category = mValues.get(position);
        mSubscription.add(CategoryDAO.getDAO().markAsRemoved(category.getId()).subscribe(integer -> {
            mValues.remove(position);
            mAdapter.notifyItemRemoved(position);
        }));
    }


    @Override
    public void onListItemClick(int position) {
        if (mAsDialog) {
            if (getActivity() instanceof OnCategorySelectedListener) {
                ((OnCategorySelectedListener) getActivity())
                        .onCategorySelected(mValues.get(position));
                dismiss();
            }
        } else {
            editItem(position);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscription.unsubscribe();
    }

    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category);
    }
}
