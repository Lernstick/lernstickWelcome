package ch.fhnw.lernstickwelcome;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;

/**
 * A DocumentFilter that filters regarding a maximum size
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class DocumentSizeFilter extends DocumentFilter {

    private final static int MAX_CHARS = 11;

    @Override
    public void insertString(FilterBypass fb, int offs,
            String newText, AttributeSet a) throws BadLocationException {
        
        // only allow ASCII input
        if (!isASCII(newText)) {
            return;
        }

        Document document = fb.getDocument();
        int lenght = document.getLength();
        String text = document.getText(0, lenght);
        int specialLenght = getSpecialLength(text);
        int newLength = getSpecialLength(newText);

        if ((specialLenght + newLength) <= MAX_CHARS) {
            super.insertString(fb, offs, newText, a);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public void replace(FilterBypass fb, int offs, int length, String newText,
            AttributeSet a) throws BadLocationException {

        // only allow ASCII input
        if (!isASCII(newText)) {
            return;
        }
        
        // try string replacement
        Document document = fb.getDocument();
        int docLength = document.getLength();
        String testText = document.getText(0, docLength);
        StringBuilder builder = new StringBuilder(testText);
        builder.replace(offs, offs + length, newText);
        String replacedText = builder.toString();
        
        // check, if the replacement still fits in
        int specialLength = getSpecialLength(replacedText);
        if (specialLength <= MAX_CHARS) {
            super.replace(fb, offs, length, newText, a);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private boolean isASCII(String string) {
        for (int i = 0, length = string.length(); i < length; i++) {
            char character = string.charAt(i);
            if ((character < 0) || (character > 127)) {
                return false;
            }
        }
        return true;
    }
    
    private int getSpecialLength(String string) {
        // follow special rules for VFAT labels
        int count = 0;
        for (int i = 0, length = string.length(); i < length; i++) {
            char character = string.charAt(i);
            if ((character >= 0) && (character <= 127)) {
                // ASCII
                if ((character == 39) || (character == 96)) {
                    // I have no idea why those both characters take up 3 bytes
                    // but they really do...
                    count += 3;
                } else {
                    count++;
                }
            } else {
                // non ASCII
                count += 2;
            }
        }
        return count;
    }
}
