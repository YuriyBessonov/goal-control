package app.warinator.goalcontrol;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ProjectsDialogFragment extends DialogFragment {
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.la_dialog_header)
    FrameLayout laDialogHeader;
    private boolean mAsDialog;
    private AndroidTreeView mTreeView;

    public ProjectsDialogFragment() {}

    public static ProjectsDialogFragment newInstance(){
        return new ProjectsDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_projects_dialog, null, false);
        ButterKnife.bind(this,v);

        if (mAsDialog){
            tvDialogTitle.setText(R.string.drawer_item_main_projects);
        }
        else {
            laDialogHeader.setVisibility(View.GONE);
        }

        ViewGroup treeContainer = ButterKnife.findById(v, R.id.la_tree_container);
        final TreeNode root = TreeNode.root();


        TreeNode parent = new TreeNode(new ProjectTreeItemHolder.
                ProjectTreeItem(getResources().getIntArray(R.array.palette_material)[1],"Проектище"))
                .setViewHolder(new ProjectTreeItemHolder(getActivity()));
        TreeNode child0 = new TreeNode(new ProjectTreeItemHolder.
                ProjectTreeItem(getResources().getIntArray(R.array.palette_material)[3],"Подпроект 0"))
                .setViewHolder(new ProjectTreeItemHolder(getActivity()));
        TreeNode child1 = new TreeNode(new ProjectTreeItemHolder.
                ProjectTreeItem(getResources().getIntArray(R.array.palette_material)[5],"Подпроект 1"))
                .setViewHolder(new ProjectTreeItemHolder(getActivity()));
        parent.addChildren(child0, child1);
        for (int i=0; i<20; i++){
            TreeNode node = new TreeNode(new ProjectTreeItemHolder.
                    ProjectTreeItem(getResources().getIntArray(R.array.palette_material)[4],"Хватит\nэто\nтерпеть"))
                    .setViewHolder(new ProjectTreeItemHolder(getActivity()));
            parent.addChild(node);
        }
        root.addChild(parent);

        mTreeView = new AndroidTreeView(getActivity(), root);
        mTreeView.setDefaultAnimation(true);
        mTreeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        //mTreeView.setDefaultViewHolder(ProjectTreeItemHolder.class);
        treeContainer.addView(mTreeView.getView());
        mTreeView.expandAll();
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsDialog = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAsDialog = true;
        return super.onCreateDialog(savedInstanceState);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
