package app.warinator.goalcontrol.fragment;


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


public class ListEditDialogFragment extends DialogFragment {
    private ArrayList<String> mItemsList;
    private ArrayAdapter<String> mAdapter;
    @BindView(R.id.lv_items)
    ListView lvItems;
    @BindView(R.id.btn_add_element)
    ImageButton btnAddElement;
    @BindView(R.id.et_new_item)
    EditText etNewItem;
    @BindView(R.id.btn_ok)
    Button btnOk;
    ListChangedCallback caller;

    public ListEditDialogFragment() {
    }

    public static ListEditDialogFragment getInstance(ListChangedCallback caller){
        ListEditDialogFragment fragment = new ListEditDialogFragment();
        fragment.caller = caller;
        fragment.mItemsList = new ArrayList<>();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_edit_dialog, container, false);
        ButterKnife.bind(this,v);

        mAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, mItemsList);
        lvItems.setAdapter(mAdapter);
        lvItems.setOnItemLongClickListener(onItemLongClick);
        btnAddElement.setOnClickListener(onAddElementBtnClick);
        btnOk.setOnClickListener(onBtnOkClick);
        return v;
    }

    AdapterView.OnItemLongClickListener onItemLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mItemsList.remove(position);
            mAdapter.notifyDataSetChanged();
            notifyItemsCountChanged();
            return false;
        }
    };

    private View.OnClickListener onAddElementBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etNewItem.getText().toString().trim().length() > 0){
                mItemsList.add(etNewItem.getText().toString());
                mAdapter.notifyDataSetChanged();
                notifyItemsCountChanged();
            }
        }
    };

    private View.OnClickListener onBtnOkClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    public int getItemsCount(){
        return mItemsList.size();
    }

    public interface ListChangedCallback {
        void updateItemsCount(int newCount);
    }

    private void notifyItemsCountChanged(){
        caller.updateItemsCount(getItemsCount());
    }
}
