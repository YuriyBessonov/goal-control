package app.warinator.goalcontrol;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;


public class IconPickerDialogFragment extends DialogFragment implements IconPickerAdapter.ItemClickListener{
    @BindView(R.id.rv_grid)
    RecyclerView rvIconsGrid;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;

    private IconPickerAdapter mAdapter;

    public IconPickerDialogFragment() {
    }

    public static IconPickerDialogFragment newInstance(){
        return new IconPickerDialogFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.fragment_icon_picker_dialog, container, false);
        ButterKnife.bind(this,v);
        tvDialogTitle.setText(R.string.task_icon);
        btnOk.setVisibility(View.INVISIBLE);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int numberOfColumns = 4;
        GoogleMaterial.Icon[] ic = GoogleMaterial.Icon.values();
        String[] icons = new String[ic.length];
        for (int i=0; i<ic.length; i++){
            icons[i] = ic[i].getName();
        }
        rvIconsGrid.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        mAdapter = new IconPickerAdapter(getContext(), icons);
        mAdapter.setClickListener(this);
        rvIconsGrid.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (getActivity() instanceof IconPickedCallback){
            ((IconPickedCallback)getActivity()).onIconPicked(mAdapter.getItem(position));
        }
        dismiss();
    }

    public interface IconPickedCallback {
        public void onIconPicked(String icon);
    }
}
