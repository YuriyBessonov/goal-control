package app.warinator.goalcontrol.ui_components;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.Toolbar;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

import app.warinator.goalcontrol.R;

/**
 * Настройщик бокового меню
 */
public class MaterialDrawer {
    public final static int[] task_items = {
            R.string.drawer_item_task_current,
            R.string.drawer_item_task_today,
            R.string.drawer_item_task_week,
            R.string.drawer_item_task_date,
            R.string.drawer_item_task_no_date};
    public final static int[] main_items = {
            R.string.drawer_item_main_projects_and_tasks,
            R.string.drawer_item_main_categories,
            R.string.drawer_item_main_statistics
    };
    public final static int[] aux_items = {
            R.string.drawer_item_aux_help,
            R.string.drawer_item_aux_about
    };
    private MaterialDrawer() {
    }

    public static Drawer build(Activity activity, Toolbar toolbar) {
        Resources res = activity.getResources();

        String tasksIcons[] = res.getStringArray(R.array.drawer_icons_tasks);
        String mainIcons[] = res.getStringArray(R.array.drawer_icons_main);
        String auxIcons[] = res.getStringArray(R.array.drawer_icons_aux);

        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withHeader(R.layout.header_drawer)
                .build();

        for (int i = 0; i < task_items.length; i++) {
            result.addItem(new PrimaryDrawerItem().withName(task_items[i]).withTag(task_items[i])
                    .withIcon(CommunityMaterial.Icon.valueOf(tasksIcons[i])));
        }
        result.addItem(new DividerDrawerItem());
        for (int i = 0; i < main_items.length; i++) {
            result.addItem(new PrimaryDrawerItem().withName(main_items[i]).withTag(main_items[i])
                    .withIcon(CommunityMaterial.Icon.valueOf(mainIcons[i])));
        }
        result.addItem(new DividerDrawerItem());
        for (int i = 0; i < aux_items.length; i++) {
            result.addItem(new PrimaryDrawerItem().withName(aux_items[i]).withTag(aux_items[i])
                    .withIcon(CommunityMaterial.Icon.valueOf(auxIcons[i])));
        }

        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        return result;
    }

    public static int getItemPosition(Drawer drawer, int itemId) {
        List<IDrawerItem> items = drawer.getDrawerItems();
        int i = 1;
        for (IDrawerItem item : items) {
            if (item.getTag() != null && (int) item.getTag() == itemId) {
                return i;
            }
            i++;
        }
        return -1;
    }

}
