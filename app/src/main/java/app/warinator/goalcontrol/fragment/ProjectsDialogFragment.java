package app.warinator.goalcontrol.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import app.warinator.goalcontrol.ProjectTreeItemHolder;
import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.TaskTreeItemHolder;
import app.warinator.goalcontrol.activity.TaskEditActivity;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.model.main.Category;
import app.warinator.goalcontrol.model.main.Project;
import app.warinator.goalcontrol.model.main.Task;
import app.warinator.goalcontrol.utils.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;


public class ProjectsDialogFragment extends DialogFragment {

    private static final String TAG_DIALOG_CREATE = "dialog_create";
    private static final String TAG_DIALOG_EDIT = "dialog_edit";
    private static final int REQUEST_EDIT_TASK = 1;

    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.la_dialog_header)
    FrameLayout laDialogHeader;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;
    @BindView(R.id.btn_ok)
    ImageButton btnOk;
    @BindView(R.id.la_tree_container)
    RelativeLayout laTreeContainer;
    @BindView(R.id.fab_add_menu)
    FloatingActionMenu fabAddMenu;
    @BindView(R.id.fab_add_task)
    FloatingActionButton fabAddTask;
    @BindView(R.id.fab_add_project)
    FloatingActionButton fabAddProject;


    private boolean mAsDialog;
    private AndroidTreeView mTreeView;
    private LongSparseArray<ArrayList<Long>> mNodesGraph;
    private LongSparseArray<ArrayList<TreeNode>> mTaskNodes;
    LongSparseArray<TreeNode> mProjectNodes;
    private ProjectEditDialogFragment mFragment;
    private CompositeSubscription mSub = new CompositeSubscription();
    private Subscription mTreeSub;
    private OnProjectPickedListener mListener;
    private Project mTargetProject;
    private Task mTargetTask;

    private TreeNode.TreeNodeClickListener onTreeNodeSelected = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            ProjectTreeItemHolder.ProjectTreeItem item = (ProjectTreeItemHolder.ProjectTreeItem) value;
            mListener.onProjectPicked(item.project);
            dismiss();
        }
    };

    private Func2<List<Project>, List<Task>, TreeNode> buildTree = new Func2<List<Project>, List<Task>, TreeNode>() {
        @Override
        public TreeNode call(List<Project> projects, List<Task> tasks) {
            //Ассоциативный массив: ключ - id родителя, значение - список id дочерних проектов
            mNodesGraph = new LongSparseArray<>();
            //Ассоциативный массив: ключ - id проекта, значение - узел в дереве
            mProjectNodes = new LongSparseArray<>();

            TreeNode root = TreeNode.root();
            //формируем узлы и граф проектов
            for (Project p : projects) {
                long id = p.getId();
                //создаем новый узел
                TreeNode node = makeTreeNode(p);
                //добавляем в map узлов
                mProjectNodes.put(id, node);
                long parent = p.getParentId();
                //если родителя нет, добавляем к корню
                if (parent == 0) {
                    root.addChild(node);
                }
                //добавляем ребро в граф
                if (mNodesGraph.indexOfKey(parent) < 0) {
                    mNodesGraph.put(parent, new ArrayList<Long>());
                }
                mNodesGraph.get(parent).add(id);
            }

            //Ассоциативный массив: ключ - id проекта, значение - список узлов задач
            mTaskNodes = new LongSparseArray<>();
            for (Task t : tasks) {
                TreeNode node = makeTreeNode(t);
                if (t.getProject() == null) {
                    if (!mAsDialog) {
                        root.addChild(node);
                    }
                } else {
                    long projectId = t.getProject().getId();
                    if (mTaskNodes.indexOfKey(projectId) < 0) {
                        mTaskNodes.put(projectId, new ArrayList<TreeNode>());
                    }
                    mTaskNodes.get(projectId).add(node);
                }
            }

            for (int i = 0; i < mProjectNodes.size(); i++) {
                long id = mProjectNodes.keyAt(i);
                TreeNode node = mProjectNodes.get(id);
                ArrayList<Long> childrenProjects = mNodesGraph.get(id);
                if (childrenProjects != null) {
                    for (long childId : childrenProjects) {
                        node.addChild(mProjectNodes.get(childId));
                    }
                }
                ArrayList<TreeNode> childrenTasks = mTaskNodes.get(id);
                if (childrenTasks != null && !mAsDialog) {
                    node.addChildren(childrenTasks);
                }
            }
            return root;
        }
    };

    private Observable<TreeNode> mTreeObservable;


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
                    Util.showConfirmationDialog(getString(R.string.delete_project),
                            getContext(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteProject(mTargetProject);
                                }
                            });
                    break;
                case R.id.action_task_edit:
                    editTask(mTargetTask);
                    break;
                case R.id.action_task_info:
                    break;
                case R.id.action_task_delete:
                    Util.showConfirmationDialog(getString(R.string.delete_task),
                            getContext(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteTask(mTargetTask);
                                }
                            });
                    break;
            }
        }

        @Override
        public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @DismissEvent int i) {
        }
    };


    private TreeNode.TreeNodeLongClickListener mOnTreeNodeLongClick = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            int optionsSheet;
            String title;
            if (value instanceof ProjectTreeItemHolder.ProjectTreeItem) {
                //проект
                mTargetProject = ((ProjectTreeItemHolder.ProjectTreeItem) value).project;
                optionsSheet = R.menu.menu_project_options;
                title = mTargetProject.getName();
            } else {
                //задача
                mTargetTask = ((TaskTreeItemHolder.TaskTreeItem) value).mTask;
                optionsSheet = R.menu.menu_task_options;
                title = mTargetTask.getName();
            }

            new BottomSheet.Builder(getActivity(), R.style.MyBottomSheetStyle)
                    .setSheet(optionsSheet)
                    .setListener(mMenuOptionSelected)
                    .setTitle(title)
                    .grid()
                    .show();
            return false;
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
            btnOk.setVisibility(View.INVISIBLE);
            tvDialogTitle.setText(R.string.drawer_item_main_projects_and_tasks);
            btnCancel.setOnClickListener(v1 -> dismiss());
            fabAddMenu.setVisibility(View.INVISIBLE);
        } else {
            laDialogHeader.setVisibility(View.GONE);
            fabAddMenu.setVisibility(View.VISIBLE);
        }

        mTreeObservable = ProjectDAO.getDAO().getAll(false).zipWith(TaskDAO.getDAO().getAll(false), buildTree);
        refreshTree();
        fabAddProject.setOnClickListener(v12 -> {
            fabAddMenu.close(true);
            createProject();
        });

        fabAddTask.setOnClickListener(v13 -> {
            fabAddMenu.close(true);
            createTask();
        });

        return v;
    }

    private void refreshTree() {
        if (mTreeSub != null && !mTreeSub.isUnsubscribed()) {
            mTreeSub.unsubscribe();
        }
        mTreeSub = mTreeObservable.subscribe(new Subscriber<TreeNode>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(TreeNode root) {
                ViewGroup treeContainer = laTreeContainer;
                if (treeContainer.getChildCount() > 0) {
                    treeContainer.removeAllViews();
                }
                mTreeView = new AndroidTreeView(getActivity(), root);
                mTreeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
                treeContainer.addView(mTreeView.getView());
                if (mAsDialog) {
                    mTreeView.setDefaultNodeClickListener(onTreeNodeSelected);
                    mTreeView.setUseAutoToggle(false);
                } else {
                    mTreeView.setDefaultNodeLongClickListener(mOnTreeNodeLongClick);
                }
                mTreeView.expandAll();
                mTreeView.setDefaultAnimation(true);
            }
        });
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
        mSub.add(ProjectDAO.getDAO().add(project).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                refreshTree();
            }
        }));
    }

    public void updateProject(Project project) {
        if (nodeHasChild(project.getId(), project.getParentId())) {
            Toast.makeText(getContext(), R.string.cannot_set_child_element_as_its_parent,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mSub.add(ProjectDAO.getDAO().update(project).subscribe(aInt -> refreshTree()));
    }

    private void deleteProject(final Project project) {
        final long id = project.getId();
        final long newParent = project.getParentId();
        mSub.add(ProjectDAO.getDAO().replaceParent(id, newParent).concatMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer integer) {
                return TaskDAO.getDAO().replaceProject(id, newParent);
            }
        }).concatMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer integer) {
                return ProjectDAO.getDAO().delete(project);
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                refreshTree();
            }
        }));
    }

    private void createTask(){
        Intent intent = TaskEditActivity.getIntent(0L, getActivity());
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    private void editTask(Task task){
        Intent intent = TaskEditActivity.getIntent(task.getId(), getActivity());
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_EDIT_TASK){
            refreshTree();
        }
    }

    private void deleteTask(Task task) {
        mSub.add(TaskDAO.getDAO().delete(task).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                refreshTree();
            }
        }));
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

    private TreeNode makeTreeNode(Task task) {
        TreeNode node = new TreeNode(new TaskTreeItemHolder.TaskTreeItem(task));
        node.setViewHolder(new TaskTreeItemHolder(getActivity()));
        return node;
    }

    public interface OnProjectPickedListener {
        void onProjectPicked(Project project);
    }

    private class ProjectsTasksPair {
        private ArrayList<Project> projects;
        private ArrayList<Task> tasks;
    }
}
