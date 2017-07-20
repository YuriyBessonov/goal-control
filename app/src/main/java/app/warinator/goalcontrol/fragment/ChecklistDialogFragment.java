package app.warinator.goalcontrol.fragment;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.CheckItemsAdapter;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.model.CheckListItem;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Фрагмент редактирования списка пунктов
 */
public class ChecklistDialogFragment extends DialogFragment
        implements CheckItemsAdapter.OnItemRemovedListener {
    private static final String ARG_TASK = "task";
    private static final String ARG_TODO_LIST = "todo_list";
    private static final String ARG_EDITABLE = "editable";
    @BindView(R.id.rv_items)
    RecyclerView rvItems;
    @BindView(R.id.btn_add_element)
    ImageButton btnAddElement;
    @BindView(R.id.et_new_item)
    EditText etNewItem;
    @BindView(R.id.btn_ok)
    Button btnOk;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    @BindView(R.id.la_add_element)
    LinearLayout laAddElement;

    private ArrayList<CheckListItem> mTodoList;
    private CheckItemsAdapter mAdapter;
    private Long mTaskId;
    private boolean mIsEditable;
    private int mOldCheckedCount;
    private OnChecklistChangedListener mListener;

    //Добавление пункта
    private View.OnClickListener onAddElementBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etNewItem.getText().toString().trim().length() > 0) {
                mTodoList.add(new CheckListItem(0, mTaskId, 0, etNewItem.getText().toString(), false));
                mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
                etNewItem.setText("");
                mListener.onCheckListChanged(mTodoList);
            }
        }
    };

    public ChecklistDialogFragment() {
    }

    public static ChecklistDialogFragment getInstance(Long taskId, ArrayList<CheckListItem> todoList,
                                                      boolean isEditable) {
        ChecklistDialogFragment fragment = new ChecklistDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TASK, taskId);
        if (todoList != null) {
            args.putParcelableArrayList(ARG_TODO_LIST, todoList);
        }
        args.putBoolean(ARG_EDITABLE, isEditable);
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Util.disableTitle(dialog);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_edit_dialog, container, false);
        ButterKnife.bind(this, v);

        if (savedInstanceState != null) {
            mTaskId = savedInstanceState.getLong(ARG_TASK);
            if (savedInstanceState.getParcelableArrayList(ARG_TODO_LIST) != null) {
                mTodoList = savedInstanceState.getParcelableArrayList(ARG_TODO_LIST);
            }
            mIsEditable = savedInstanceState.getBoolean(ARG_EDITABLE);
        } else {
            mTaskId = getArguments().getLong(ARG_TASK);
            if (getArguments().getParcelableArrayList(ARG_TODO_LIST) != null) {
                mTodoList = getArguments().getParcelableArrayList(ARG_TODO_LIST);
            }
            mIsEditable = getArguments().getBoolean(ARG_EDITABLE);
        }

        if (!mIsEditable) {
            laAddElement.setVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.GONE);
        }

        if (mTodoList == null) {
            mTodoList = new ArrayList<>();
            if (mTaskId > 0) {
                CheckListItemDAO.getDAO().getAllForTask(mTaskId, false).subscribe(checkListItems -> {
                    mTodoList.addAll(checkListItems);
                    mAdapter.notifyDataSetChanged();
                    mListener.onCheckListChanged(mTodoList);
                    for (CheckListItem item : checkListItems) {
                        if (item.isCompleted()) {
                            mOldCheckedCount++;
                        }
                    }
                });
            }
        }

        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvItems.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new CheckItemsAdapter(getContext(), this, mTodoList, mIsEditable);
        rvItems.setAdapter(mAdapter);

        btnAddElement.setOnClickListener(onAddElementBtnClick);
        btnOk.setOnClickListener(v1 -> {
            int checkedCount = 0;
            for (CheckListItem item : mTodoList) {
                if (item.isCompleted()) {
                    checkedCount++;
                }
            }
            mListener.onCheckListEditDone(mTodoList, false,
                    checkedCount - mOldCheckedCount);
            dismiss();
        });
        btnCancel.setOnClickListener(v12 -> {
            mListener.onCheckListEditDone(mTodoList, true, 0);
            dismiss();
        });
        return v;
    }

    public int getItemsCount() {
        return mTodoList.size();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChecklistChangedListener) {
            mListener = (OnChecklistChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + getString(R.string.must_implement)
                    + OnChecklistChangedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemRemoved(int position) {
        mListener.onCheckListChanged(mTodoList);
    }

    public interface OnChecklistChangedListener {
        void onCheckListChanged(ArrayList<CheckListItem> list);

        void onCheckListEditDone(ArrayList<CheckListItem> list, boolean cancelled, int checkedDiff);
    }
}
