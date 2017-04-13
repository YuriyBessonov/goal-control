package app.warinator.goalcontrol.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import app.warinator.goalcontrol.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Фрагмент редактирования списка пунктов
 */
public class ListEditDialogFragment extends DialogFragment {
    private static final String ARG_LIST = "list";
    @BindView(R.id.lv_items)
    ListView lvItems;
    @BindView(R.id.btn_add_element)
    ImageButton btnAddElement;
    @BindView(R.id.et_new_item)
    EditText etNewItem;
    @BindView(R.id.btn_ok)
    Button btnOk;


    private ArrayList<String> mItemsList;
    private ArrayAdapter<String> mAdapter;

    //Удаление при удержании
    AdapterView.OnItemLongClickListener onItemLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mItemsList.remove(position);
            mAdapter.notifyDataSetChanged();
            return false;
        }
    };

    //Добавление пункта
    private View.OnClickListener onAddElementBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etNewItem.getText().toString().trim().length() > 0) {
                mItemsList.add(etNewItem.getText().toString());
                mAdapter.notifyDataSetChanged();
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

    public ListEditDialogFragment() {}

    public static ListEditDialogFragment getInstance(ArrayList<String> list) {
        ListEditDialogFragment fragment = new ListEditDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_LIST, list);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_edit_dialog, container, false);
        ButterKnife.bind(this, v);

        if (savedInstanceState != null){
            mItemsList = savedInstanceState.getStringArrayList(ARG_LIST);
        }
        else if (getArguments() != null){
            mItemsList = getArguments().getStringArrayList(ARG_LIST);
        }

        mAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, mItemsList);
        lvItems.setAdapter(mAdapter);
        lvItems.setOnItemLongClickListener(onItemLongClick);
        btnAddElement.setOnClickListener(onAddElementBtnClick);
        btnOk.setOnClickListener(onBtnOkClick);
        return v;
    }

    public int getItemsCount() {
        return mItemsList.size();
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
        mListener.onListChanged(mItemsList);
        super.onDismiss(dialog);
    }

    public interface OnListChangedListener {
        void onListChanged(ArrayList<String> list);
    }
}
