package ch.fhnw.lernstickwelcome;

import javax.swing.Icon;

/**
 * An offline application
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class OfflineApplication {

    private final String name;
    private final Icon icon;
    
    /**
     * creates a new OfflineApplication
     * @param name
     * @param icon
     */
    public OfflineApplication(String name, Icon icon) {
        this.name = name;
        this.icon = icon;
    }

    /**
     * returns the name of the application
     * @return the name of the application
     */
    public String getName() {
        return name;
    }

    /**
     * returns the icon of the application
     * @return the icon of the application
     */
    public Icon getIcon() {
        return icon;
    }
}
