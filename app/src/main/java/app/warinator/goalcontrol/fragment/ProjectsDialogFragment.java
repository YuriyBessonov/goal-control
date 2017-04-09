package app.warinator.goalcontrol.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.scalified.fab.ActionButton;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import app.warinator.goalcontrol.ProjectTreeItemHolder;
import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.Project;
import app.warinator.goalcontrol.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;


public class ProjectsDialogFragment extends DialogFragment {

    private static final String TAG_DIALOG_CREATE = "dialog_create";
    private static final String TAG_DIALOG_EDIT = "dialog_edit";

    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.la_dialog_header)
    FrameLayout laDialogHeader;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.fab_add)
    ActionButton btnAdd;

    private boolean mAsDialog;
    private AndroidTreeView mTreeView;
    private LongSparseArray<ArrayList<Long>> mNodesGraph;
    private ProjectEditDialogFragment mFragment;
    private CompositeSubscription mSub = new CompositeSubscription();
    private OnProjectPickedListener mListener;
    private Project mTargetProject;

    private TreeNode.TreeNodeClickListener onTreeNodeSelected = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            ProjectTreeItemHolder.ProjectTreeItem item = (ProjectTreeItemHolder.ProjectTreeItem) value;
            mListener.onProjectPicked(item.project);
            dismiss();
        }
    };
    private Func1<List<Project>, TreeNode> buildProjectsTree = new Func1<List<Project>, TreeNode>() {
        @Override
        public TreeNode call(List<Project> projects) {
            //Ассоциативный массив: ключ - id родителя, значение - список дочерних id
            mNodesGraph = new LongSparseArray<>();
            //Ассоциативный массив: ключ - id проекта, значение - узел в дереве
            LongSparseArray<TreeNode> nodes = new LongSparseArray<>();
            TreeNode root = TreeNode.root();
            for (Project p : projects) {
                long id = p.getId();
                TreeNode node = makeTreeNode(p);
                nodes.put(id, node);
                long parent = p.getParentId();
                if (parent == 0) {
                    root.addChild(node);
                }
                if (mNodesGraph.indexOfKey(parent) < 0) {
                    mNodesGraph.put(parent, new ArrayList<Long>());
                }
                mNodesGraph.get(parent).add(id);
            }

            for (int i = 0; i < nodes.size(); i++) {
                long id = nodes.keyAt(i);
                TreeNode node = nodes.get(id);
                ArrayList<Long> children = mNodesGraph.get(id);
                if (children != null) {
                    for (long childId : children) {
                        node.addChild(nodes.get(childId));
                    }
                }
            }
            return root;
        }
    };
    private TreeNode.TreeNodeLongClickListener mOnTreeNodeLongClick = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            mTargetProject = ((ProjectTreeItemHolder.ProjectTreeItem) value).project;
            new BottomSheet.Builder(getActivity())
                    .setSheet(R.menu.menu_project_options)
                    .setListener(mMenuOptionSelected)
                    .setTitle(mTargetProject.getName())
                    .grid()
                    .show();
            return false;
        }
    };
    private Observer<Long> errorHandlerLong = new Observer<Long>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNext(Long aLong) {
        }
    };
    private Observer<Integer> errorHandlerInt = new Observer<Integer>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        @Override
        public void onNext(Integer aLong) {
        }
    };
    private BottomSheetListener mMenuOptionSelected = new BottomSheetListener() {
        @Override
        public void onSheetShown(@NonNull BottomSheet bottomSheet) {
        }

        @Override
        public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_project_edit:
                    editProject(mTargetProject);
                    break;
                case R.id.action_project_info:
                    break;
                case R.id.action_project_delete:
                    Util.showConfirmationDialog(getString(R.string.delete_the_project_and_all_his_children),
                            getContext(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteProject(mTargetProject);
                                }
                            });
                    break;
            }
        }

        @Override
        public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @DismissEvent int i) {
        }
    };

    public ProjectsDialogFragment() {
    }

    public static ProjectsDialogFragment newInstance() {
        return new ProjectsDialogFragment();
    }

    private boolean nodeHasChild(long nodeId, long childId) {
        LinkedList<Long> q = new LinkedList<>();
        q.add(nodeId);
        while (!q.isEmpty()) {
            long pid = q.poll();
            ArrayList<Long> children = mNodesGraph.get(pid);
            if (children != null) {
                for (long id : children) {
                    if (id == childId) {
                        return true;
                    }
                    q.push(id);
                }
            }
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_projects_dialog, null, false);
        ButterKnife.bind(this, v);

        if (mAsDialog) {
            tvDialogTitle.setText(R.string.drawer_item_main_projects);
            btnOk.setVisibility(View.INVISIBLE);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        } else {
            laDialogHeader.setVisibility(View.GONE);
        }

        mSub.add(ProjectDAO.getDAO().getAll(true).map(buildProjectsTree).subscribe(new Subscriber<TreeNode>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(TreeNode root) {
                ViewGroup treeContainer = ButterKnife.findById(v, R.id.la_tree_container);
                if (treeContainer.getChildCount() > 0) {
                    treeContainer.removeAllViews();
                }
                mTreeView = new AndroidTreeView(getActivity(), root);
                mTreeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
                treeContainer.addView(mTreeView.getView());
                if (mAsDialog) {
                    mTreeView.setDefaultNodeClickListener(onTreeNodeSelected);
                    mTreeView.setUseAutoToggle(false);
                    btnAdd.setVisibility(View.INVISIBLE);
                } else {
                    mTreeView.setDefaultNodeLongClickListener(mOnTreeNodeLongClick);
                }
                mTreeView.expandAll();
                mTreeView.setDefaultAnimation(true);
            }
        }));
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProject();
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!mAsDialog){
            btnAdd.playShowAnimation();
        }
    }

    public void createProject() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        mFragment = ProjectEditDialogFragment.newInstance(new Project());
        mFragment.show(ft, TAG_DIALOG_CREATE);
    }

    public void editProject(Project project) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        mFragment = ProjectEditDialogFragment.newInstance(project);
        mFragment.show(ft, TAG_DIALOG_EDIT);
    }

    public void addProject(Project project) {
        mSub.add(ProjectDAO.getDAO().add(project).subscribe(errorHandlerLong));
    }

    public void updateProject(Project project) {
        if (nodeHasChild(project.getId(), project.getParentId())) {
            Toast.makeText(getContext(), R.string.cannot_set_child_element_as_its_parent,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mSub.add(ProjectDAO.getDAO().update(project).subscribe(errorHandlerInt));
    }

    private void deleteProject(Project project) {
        mSub.add(ProjectDAO.getDAO().delete(project).subscribe(errorHandlerInt));
    }

    public void onProjectEdited(Project project) {
        if (project.getId() == 0) {
            addProject(project);
        } else {
            updateProject(project);
        }
    }

    public void onCategoryPicked(Category category) {
        mFragment.setCategory(category);
    }

    public void onParentPicked(Project parent) {
        mFragment.setParent(parent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsDialog = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAsDialog = true;
        if (getActivity() instanceof OnProjectPickedListener) {
            mListener = (OnProjectPickedListener) getActivity();
        } else {
            throw new RuntimeException(getString(R.string.parent_must_implement) +
                    OnProjectPickedListener.class.getSimpleName());
        }
        return super.onCreateDialog(savedInstanceState);
    }

    private TreeNode makeTreeNode(Project project) {
        TreeNode node = new TreeNode(new ProjectTreeItemHolder.ProjectTreeItem(project));
        node.setViewHolder(new ProjectTreeItemHolder(getActivity()));
        return node;
    }

    public interface OnProjectPickedListener {
        void onProjectPicked(Project project);
    }

}
