package ch.fhnw.lernstickwelcome;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * A Panel with some scrolling specific features
 * @author ronny
 */
public class ScrollableJPanel extends JPanel implements Scrollable {

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        Dimension preferredSize = getPreferredSize();
        preferredSize.height = 200;
        return preferredSize;
    }

    @Override
    public int getScrollableUnitIncrement(
            Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    @Override
    public int getScrollableBlockIncrement(
            Rectangle visibleRect, int orientation, int direction) {
        return 40;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
}
