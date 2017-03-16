package app.warinator.goalcontrol.model;

/**
 * Created by Warinator on 16.03.2017.
 */

public class EditOption {
    private String name;
    private String info;
    private String icon;

    public EditOption(String name, String info, String icon) {
        this.name = name;
        this.info = info;
        this.icon = icon;
    }

    public EditOption() {
        this.name = "Sample name";
        this.info = "Sample info";
        this.icon = "gmd-settings";
    }


    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public String getIcon() {
        return icon;
    }
}
