package app.warinator.goalcontrol.fragment;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;


public class NotesEditDialogFragment extends DialogFragment {
    private static final String ARG_NOTE = "note";
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.et_note)
    EditText etNote;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;

    private String mNote;
    public NotesEditDialogFragment() {}

    public static NotesEditDialogFragment newInstance(String note) {
        NotesEditDialogFragment fragment = new NotesEditDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NOTE, note);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNote = getArguments().getString(ARG_NOTE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notes_edit_dialog, container, false);
        ButterKnife.bind(this,v);
        tvDialogTitle.setText(R.string.task_option_comment);
        etNote.setText(mNote);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.hideKeyboard(getContext(),getView());
                dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof OnNoteEditedCallback){
                    mNote = etNote.getText().toString();
                    ((OnNoteEditedCallback)getContext()).onNoteEdited(mNote);
                    Util.hideKeyboard(getContext(),getView());
                    dismiss();
                }
            }
        });
        RxTextView.textChanges(etNote).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                    if (Util.editTextIsEmpty(etNote)) {
                        btnOk.setEnabled(false);
                    } else {
                        btnOk.setEnabled(true);
                    }
            }
        });
        return v;
    }

    public interface OnNoteEditedCallback{
        void onNoteEdited(String note);
    }


}
