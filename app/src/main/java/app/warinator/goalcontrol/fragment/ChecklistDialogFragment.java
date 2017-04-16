package app.warinator.goalcontrol.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.adapter.CheckItemsAdapter;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.model.main.CheckListItem;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Фрагмент редактирования списка пунктов
 */
public class ChecklistDialogFragment extends DialogFragment implements CheckItemsAdapter.OnItemRemovedListener {
    private static final String ARG_TASK = "task";
    private static final String ARG_TODO_LIST = "todo_list";
    @BindView(R.id.rv_items)
    RecyclerView rvItems;
    @BindView(R.id.btn_add_element)
    ImageButton btnAddElement;
    @BindView(R.id.et_new_item)
    EditText etNewItem;
    @BindView(R.id.btn_ok)
    Button btnOk;

    private ArrayList<CheckListItem> mTodoList;
    private CheckItemsAdapter mAdapter;
    private Long mTaskId;


    //Добавление пункта
    private View.OnClickListener onAddElementBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etNewItem.getText().toString().trim().length() > 0) {
                mTodoList.add(new CheckListItem(0, mTaskId, 0, etNewItem.getText().toString(), false));
                mAdapter.notifyItemInserted(mAdapter.getItemCount()-1);
                etNewItem.setText("");
            }
        }
    };

    private View.OnClickListener onBtnOkClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };
    private OnListChangedListener mListener;

    public ChecklistDialogFragment() {}

    public static ChecklistDialogFragment getInstance(Long taskId, ArrayList<CheckListItem> todoList) {
        ChecklistDialogFragment fragment = new ChecklistDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TASK, taskId);
        if (todoList != null){
            args.putParcelableArrayList(ARG_TODO_LIST,todoList);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_edit_dialog, container, false);
        ButterKnife.bind(this, v);

        if (savedInstanceState != null){
            mTaskId = savedInstanceState.getLong(ARG_TASK);
            if (savedInstanceState.getParcelableArrayList(ARG_TODO_LIST) != null){
                mTodoList = savedInstanceState.getParcelableArrayList(ARG_TODO_LIST);
            }
        }
        else {
            mTaskId = getArguments().getLong(ARG_TASK);
            if (getArguments().getParcelableArrayList(ARG_TODO_LIST) != null){
                mTodoList = getArguments().getParcelableArrayList(ARG_TODO_LIST);
            }
        }

        if (mTodoList == null){
            mTodoList = new ArrayList<>();
            if (mTaskId > 0 ){
                CheckListItemDAO.getDAO().getAllForTask(mTaskId, false).subscribe(checkListItems -> {
                    mTodoList.addAll(checkListItems);
                    mAdapter.notifyDataSetChanged();
                    mListener.onListChanged(mTodoList);
                });
            }
        }

        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvItems.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new CheckItemsAdapter(getContext(), this, mTodoList);
        rvItems.setAdapter(mAdapter);

        btnAddElement.setOnClickListener(onAddElementBtnClick);
        btnOk.setOnClickListener(onBtnOkClick);
        return v;
    }

    public int getItemsCount() {
        return mTodoList.size();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListChangedListener) {
            mListener = (OnListChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " должен реализовывать " + OnListChangedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mListener.onListChanged(mTodoList);
        super.onDismiss(dialog);
    }

    @Override
    public void onItemRemoved(int position) {
        mListener.onListChanged(mTodoList);
    }

    public interface OnListChangedListener {
        void onListChanged(ArrayList<CheckListItem> list);
    }
}
