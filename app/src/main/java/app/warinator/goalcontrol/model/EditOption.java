package app.warinator.goalcontrol.model;

/**
 * Created by Warinator on 16.03.2017.
 */

public class EditOption {
    private int id;
    private String name;
    private String info;
    private String icon;
    private boolean isSwitcheable = false;
    private boolean isActive = false;


    public EditOption(int id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isSwitcheable() {
        return isSwitcheable;
    }

    public void setSwitcheable(boolean switcheable) {
        isSwitcheable = switcheable;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}
