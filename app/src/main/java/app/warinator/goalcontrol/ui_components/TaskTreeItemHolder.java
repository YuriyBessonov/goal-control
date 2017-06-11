package app.warinator.goalcontrol.ui_components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.unnamed.b.atv.model.TreeNode;

import app.warinator.goalcontrol.R;
import app.warinator.goalcontrol.model.Task;
import app.warinator.goalcontrol.utils.ColorUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Warinator on 04.04.2017.
 */

public class TaskTreeItemHolder extends TreeNode.BaseNodeViewHolder<TaskTreeItemHolder.TaskTreeItem> {
    @BindView(R.id.tv_task_name)
    TextView tvTaskName;
    @BindView(R.id.iiv_task_icon)
    IconicsImageView iivTaskIcon;

    public TaskTreeItemHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, TaskTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.item_task, null, false);
        ButterKnife.bind(this,view);
        tvTaskName.setText(value.mTask.getName());
        iivTaskIcon.setIcon(GoogleMaterial.Icon.values()[value.mTask.getIcon()]);
        int color = ColorUtil.getProjectColor(value.mTask.getProject() == null ? ColorUtil.COLOR_DEFAULT :
                value.mTask.getProject().getColor(), context);
        iivTaskIcon.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return view;
    }

    @Override
    public void toggle(boolean active) {
    }

    public static class TaskTreeItem {
        public Task mTask;

        public TaskTreeItem(Task task) {
            mTask = task;
        }
    }
}