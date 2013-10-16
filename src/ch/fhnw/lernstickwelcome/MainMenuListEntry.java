package ch.fhnw.lernstickwelcome;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class MainMenuListEntry {
    
    private final Icon icon;
    private final String text;
    private final String panelID;
    
    public MainMenuListEntry(String iconPath, String text, String panelID) {
        icon = new ImageIcon(getClass().getResource(iconPath));
        this.text = text;
        this.panelID = panelID;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public String getPanelID() {
        return panelID;
    }
    
}
