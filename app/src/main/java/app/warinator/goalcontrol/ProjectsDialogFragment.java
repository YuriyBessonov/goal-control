package app.warinator.goalcontrol;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.List;

import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.fragment.ProjectEditDialogFragment;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.Project;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;
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

    private boolean mAsDialog;
    private AndroidTreeView mTreeView;
    private ProjectEditDialogFragment mFragment;
    private CompositeSubscription mSub = new CompositeSubscription();
    private OnParentPickedListener mListener;
    private TreeNode.TreeNodeClickListener onTreeNodeSelected = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            ProjectTreeItemHolder.ProjectTreeItem item = (ProjectTreeItemHolder.ProjectTreeItem) value;
            mListener.onParentPicked(item.project);
            dismiss();
        }
    };
    private Func1<List<Project>, TreeNode> buildProjectsTree = new Func1<List<Project>, TreeNode>() {
        @Override
        public TreeNode call(List<Project> projects) {
            //Ассоциативный массив: ключ - id родителя, значение - список дочерних id
            LongSparseArray<ArrayList<Long>> graph = new LongSparseArray<>();
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
                if (graph.indexOfKey(parent) < 0) {
                    graph.put(parent, new ArrayList<Long>());
                }
                graph.get(parent).add(id);
            }

            for (int i = 0; i < nodes.size(); i++) {
                long id = nodes.keyAt(i);
                TreeNode node = nodes.get(id);
                ArrayList<Long> children = graph.get(id);
                if (children != null) {
                    for (long childId : children) {
                        node.addChild(nodes.get(childId));
                    }
                }
            }
            return root;
        }
    };

    public ProjectsDialogFragment() {
    }

    public static ProjectsDialogFragment newInstance() {
        return new ProjectsDialogFragment();
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

        mSub.add(ProjectDAO.getDAO().getAll(true).map(buildProjectsTree).subscribe(new Action1<TreeNode>() {
            @Override
            public void call(TreeNode root) {
                ViewGroup treeContainer = ButterKnife.findById(v, R.id.la_tree_container);
                mTreeView = new AndroidTreeView(getActivity(), root);
                mTreeView.setDefaultAnimation(true);
                mTreeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
                treeContainer.addView(mTreeView.getView());
                if (mAsDialog) {
                    mTreeView.setDefaultNodeClickListener(onTreeNodeSelected);
                    mTreeView.setUseAutoToggle(false);
                }
                mTreeView.expandAll();
            }
        }));

        return v;
    }

    public void createItem() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        mFragment = ProjectEditDialogFragment.newInstance(new Project());
        mFragment.show(ft, TAG_DIALOG_CREATE);
    }

    public void editItem(Project project) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        mFragment = ProjectEditDialogFragment.newInstance(project);
        mFragment.show(ft, TAG_DIALOG_EDIT);
    }

    private void addItem(final Category category) {
        /*
        mSubscription.add(CategoryDAO.getDAO().add(category).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                mValues.add(category);
                mAdapter.notifyItemInserted(mValues.size() - 1);
            }
        }));
        */
    }

    private void updateItem(final int position) {
        /*
        mSubscription.add(CategoryDAO.getDAO().update(mValues.get(position)).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                mAdapter.notifyItemChanged(position);
            }
        }));
        */
    }

    private void deleteItem(final int position) {
        /*
        Category category = mValues.get(position);
        mSubscription.add(CategoryDAO.getDAO().delete(category).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                mValues.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }));
        */
    }

    public void onProjectEdited(Project project) {
        Toast.makeText(getContext(), "Ага, кто-то что-то сделал с проектом!", Toast.LENGTH_SHORT).show();
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
        if (getActivity() instanceof OnParentPickedListener) {
            mListener = (OnParentPickedListener) getActivity();
        } else {
            throw new RuntimeException("Родитель должен реализовывать" +
                    OnParentPickedListener.class.getSimpleName());
        }
        return super.onCreateDialog(savedInstanceState);
    }

    private TreeNode makeTreeNode(Project project) {
        TreeNode node = new TreeNode(new ProjectTreeItemHolder.ProjectTreeItem(project));
        node.setViewHolder(new ProjectTreeItemHolder(getActivity()));
        return node;
    }

    public interface OnParentPickedListener {
        void onParentPicked(Project parent);
    }

}
