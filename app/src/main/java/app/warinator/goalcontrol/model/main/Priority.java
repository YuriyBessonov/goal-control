package app.warinator.goalcontrol.model.main;

/**
 * Created by Warinator on 29.03.2017.
 */

public class Priority {
    public enum Level {MINOR, LOW, MEDIUM, HIGH, CRITICAL }
    private int id;
    private int value;
    private String name;
}
