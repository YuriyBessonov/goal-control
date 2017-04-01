package app.warinator.goalcontrol;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.Toolbar;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

/**
 * Created by Warinator on 24.01.2017.
 */

public class MaterialDrawer {
    private MaterialDrawer(){}

    public static Drawer build(Activity activity, Toolbar toolbar){
        Resources res = activity.getResources();
        String tasks[] = res.getStringArray(R.array.drawer_items_tasks);
        String main[] = res.getStringArray(R.array.drawer_items_main);
        String aux[] = res.getStringArray(R.array.drawer_items_aux);

        String tasksIcons[] = res.getStringArray(R.array.drawer_icons_tasks);
        String mainIcons[] = res.getStringArray(R.array.drawer_icons_main);
        String auxIcons[] = res.getStringArray(R.array.drawer_icons_aux);

        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withHeader(R.layout.header_drawer)
                .build();

        for (int i=0; i<tasks.length; i++){
            result.addItem(new PrimaryDrawerItem().withName(tasks[i])
                    .withIcon(CommunityMaterial.Icon.valueOf(tasksIcons[i])));
        }
        result.addItem(new DividerDrawerItem());
        for (int i=0; i<main.length; i++){
            result.addItem(new PrimaryDrawerItem().withName(main[i])
                    .withIcon(CommunityMaterial.Icon.valueOf(mainIcons[i])));
        }
        result.addItem(new DividerDrawerItem());
        for (int i=0; i<aux.length; i++){
            result.addItem(new PrimaryDrawerItem().withName(aux[i])
                    .withIcon(CommunityMaterial.Icon.valueOf(auxIcons[i])));
        }

        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        return result;
    }

}
