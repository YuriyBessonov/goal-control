package app.warinator.goalcontrol;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import app.warinator.goalcontrol.model.main.Project;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Warinator on 04.04.2017.
 */

public class ProjectTreeItemHolder extends TreeNode.BaseNodeViewHolder<ProjectTreeItemHolder.ProjectTreeItem> {
    @BindView(R.id.tv_project_name)
    TextView tvProjectName;
    @BindView(R.id.iv_arrow)
    ImageView ivArrow;
    @BindView(R.id.iv_folder)
    ImageView ivFolder;

    public ProjectTreeItemHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, ProjectTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.item_project, null, false);
        ButterKnife.bind(this,view);
        tvProjectName.setText(value.project.getName());
        int color = value.project.getColor();
        ivFolder.getDrawable().setColorFilter(
                context.getResources().getIntArray(R.array.palette_projects)[color], PorterDuff.Mode.SRC_ATOP);
        if (node.isLeaf()){
            ivArrow.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    @Override
    public void toggle(boolean active) {
        ivArrow.setImageResource(active ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right);
    }

    public static class ProjectTreeItem {
        public Project project;

        public ProjectTreeItem(Project project) {
            this.project = project;
        }
    }
}