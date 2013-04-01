package ch.fhnw.lernstickwelcome;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;

/**
 * A DocumentFilter that enforces the rules for full user names
 * (':', ',' and '=' are not allowed!)
 *
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class FullUserNameFilter extends DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offs,
            String newText, AttributeSet a) throws BadLocationException {
        if (!isAllowed(newText)) {
            return;
        }
        super.insertString(fb, offs, newText, a);
    }

    @Override
    public void replace(FilterBypass fb, int offs, int length, String newText,
            AttributeSet a) throws BadLocationException {
        if (!isAllowed(newText)) {
            return;
        }
        super.replace(fb, offs, length, newText, a);
    }

    private boolean isAllowed(String string) {
        for (int i = 0, length = string.length(); i < length; i++) {
            char character = string.charAt(i);
            if ((character == ':')
                    || (character == ',')
                    || (character == '=')) {
                Toolkit.getDefaultToolkit().beep();
                return false;
            }
        }
        return true;
    }
}
