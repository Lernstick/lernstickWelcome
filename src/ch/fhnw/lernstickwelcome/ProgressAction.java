package ch.fhnw.lernstickwelcome;

import javax.swing.Icon;

/**
 * some information about a progress
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class ProgressAction {

    private String description;
    private Icon icon;

    /**
     * creates a new ProgressAction
     * @param description a description of the progress action
     * @param icon the icon of the application
     */
    public ProgressAction(String description, Icon icon) {
        super();
        this.description = description;
        this.icon = icon;
    }

    /**
     * returns the description of this action
     * @return the description of this action
     */
    public String getDescription() {
        return description;
    }

    /**
     * returns the icon of the application
     * @return the icon of the application
     */
    public Icon getIcon() {
        return icon;
    }
}
