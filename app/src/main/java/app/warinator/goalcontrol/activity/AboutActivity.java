package app.warinator.goalcontrol.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import app.warinator.goalcontrol.BuildConfig;
import app.warinator.goalcontrol.R;

/**
 * Активность сведений о программе
 */
public class AboutActivity extends MaterialAboutActivity {

    private static final int colorIcon = R.color.colorPrimaryDark;
    private static final int dpSizeIcon = 18;

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        MaterialAboutCard.Builder cardBuilder = new MaterialAboutCard.Builder();

        cardBuilder.addItem(new MaterialAboutTitleItem.Builder()
                .text(R.string.app_name)
                .icon(R.drawable.app_icon)
                .build());

        cardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.version)
                .subText(BuildConfig.VERSION_NAME)
                .icon(new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_information_outline)
                        .color(ContextCompat.getColor(context, colorIcon)
                        ).sizeDp(dpSizeIcon))
                .build());

        MaterialAboutCard.Builder authorCardBuilder = new MaterialAboutCard.Builder();
        authorCardBuilder.title(R.string.developer);

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.developer_name)
                .subText(R.string.developing_details)
                .icon(new IconicsDrawable(context)
                        .icon(GoogleMaterial.Icon.gmd_person)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(dpSizeIcon))
                .build());

        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.contact_with_developer)
                .icon(new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_email)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(dpSizeIcon))
                .setOnClickAction(ConvenienceBuilder.createEmailOnClickAction(context,
                        context.getString(R.string.support_mail),
                        context.getString(R.string.email_subject)))
                .build());

        return new MaterialAboutList.Builder()
                .addCard(cardBuilder.build())
                .addCard(authorCardBuilder.build())
                .build();
    }

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.drawer_item_aux_about);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
