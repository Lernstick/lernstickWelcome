package ch.fhnw.lernstickwelcome;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * a renderer for offline applications
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class OfflineApplicationRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        if (value instanceof OfflineApplication) {
            OfflineApplication application = (OfflineApplication) value;
            setText(application.getName());
            setIcon(application.getIcon());
        } else {
            setText(value.toString());
            setIcon(null);
        }
        return this;
    }
}
