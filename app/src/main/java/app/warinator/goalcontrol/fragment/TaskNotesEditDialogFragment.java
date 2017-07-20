package app.warinator.goalcontrol.fragment;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Фрагмент редактирования примечания
 */
public class TaskNotesEditDialogFragment extends DialogFragment {
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

    public TaskNotesEditDialogFragment() {
    }

    public static TaskNotesEditDialogFragment newInstance(String note) {
        TaskNotesEditDialogFragment fragment = new TaskNotesEditDialogFragment();
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
        ButterKnife.bind(this, v);
        tvDialogTitle.setText(R.string.task_option_comment);
        etNote.setText(mNote);
        btnCancel.setOnClickListener(v1 -> {
            Util.hideKeyboard(getContext(), getView());
            dismiss();
        });
        btnOk.setOnClickListener(v12 -> {
            if (getContext() instanceof OnNoteEditedListener) {
                mNote = etNote.getText().toString();
                ((OnNoteEditedListener) getContext()).onNoteEdited(mNote);
                Util.hideKeyboard(getContext(), getView());
                dismiss();
            }
        });
        return v;
    }

    public interface OnNoteEditedListener {
        void onNoteEdited(String note);
    }


}
