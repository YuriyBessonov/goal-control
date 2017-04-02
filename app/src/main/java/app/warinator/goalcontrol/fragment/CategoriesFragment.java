package app.warinator.goalcontrol.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.CategoriesRecyclerViewAdapter;
import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.model.main.Category;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
/**
 * Фрагмент со списком категорий
 */
public class CategoriesFragment extends Fragment implements CategoriesRecyclerViewAdapter.OnListItemClickListener {

    private static final String TAG_DIALOG_CREATE = "dialog_create";
    private static final String TAG_DIALOG_EDIT = "dialog_edit";

    private CategoriesRecyclerViewAdapter mAdapter;
    private CategoryEditDialogFragment mFragment;
    private ArrayList<Category> mValues;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    public CategoriesFragment() {}

    public static CategoriesFragment newInstance() {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories_list, container, false);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            mValues = new ArrayList<>();
            mSubscription.add(CategoryDAO.getDAO().getAll().subscribe(new Action1<List<Category>>() {
                @Override
                public void call(List<Category> categories) {
                    mValues.addAll(categories);
                    mAdapter.notifyDataSetChanged();
                }
            }));
            mAdapter = new CategoriesRecyclerViewAdapter(getContext(), this, mValues);
            recyclerView.setAdapter(mAdapter);
        }

        return view;
    }

    public void createItem() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        mFragment = CategoryEditDialogFragment.newInstance(new Category(), true, new Action1<Category>() {
            @Override
            public void call(Category item) {
                addItem(item);
            }
        });
        mFragment.show(ft, TAG_DIALOG_CREATE);
    }

    public void editItem(final int position) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        mFragment = CategoryEditDialogFragment.newInstance(mValues.get(position), false, new Action1<Category>() {
            @Override
            public void call(Category category) {
                if (category == null) {
                    deleteItem(position);
                } else {
                    updateItem(position);
                }
            }
        });
        mFragment.show(ft, TAG_DIALOG_EDIT);
    }

    private void addItem(final Category category) {
        mSubscription.add(CategoryDAO.getDAO().add(category).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                mValues.add(category);
                mAdapter.notifyItemInserted(mValues.size() - 1);
            }
        }));
    }

    private void updateItem(final int position) {
        mSubscription.add(CategoryDAO.getDAO().update(mValues.get(position)).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                mAdapter.notifyItemChanged(position);
            }
        }));
    }

    private void deleteItem(final int position) {
        Category category = mValues.get(position);
        mSubscription.add(CategoryDAO.getDAO().delete(category).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                mValues.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }));
    }


    @Override
    public void onListItemClick(int position) {
        editItem(position);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscription.unsubscribe();
    }
}
