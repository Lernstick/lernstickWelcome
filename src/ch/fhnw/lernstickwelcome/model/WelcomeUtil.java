/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import ch.fhnw.util.LernstickFileTools;
import ch.fhnw.util.ProcessExecutor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.HyperlinkEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author sschw
 */
public class WelcomeUtil {
    private static final Logger LOGGER = Logger.getLogger(WelcomeUtil.class.getName());
    private static final ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private static final Path PKLA_PATH = Paths.get(
            "/etc/polkit-1/localauthority/50-local.d/10-udisks2.pkla");
    private static final String IMAGE_DIRECTORY = "/lib/live/mount/medium";
    
    private WelcomeUtil() {}
    
    public static Document parseXmlFile(File file)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // External DTD loading will most probably fail because of the local
        // firewall rules. Therefore we must disable this feature, otherwise
        // the call to parse() below will just throw an IOException.
        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }
    
    public static List<String> readFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            for (String line = reader.readLine(); line != null;
                    line = reader.readLine()) {
                lines.add(line);
            }
        }
        return lines;
    }
    
    private boolean isFileSystemMountAllowed() {
        try {
            List<String> pklaRules
                    = LernstickFileTools.readFile(PKLA_PATH.toFile());
            for (String pklaRule : pklaRules) {
                if (pklaRule.equals("ResultAny=yes")) {
                    return true;
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }
        return false;
    }
    
    public static boolean isImageWritable() {
        PROCESS_EXECUTOR.executeProcess(
                "mount", "-o", "remount,rw", IMAGE_DIRECTORY);
        String testPath = IMAGE_DIRECTORY + "/lernstickWelcome.tmp";
        PROCESS_EXECUTOR.executeProcess("touch", testPath);
        File testFile = new File(testPath);
        try {
            if (testFile.exists()) {
                LOGGER.info("image is writable");
                return true;
            } else {
                LOGGER.info("image is not writable");
                return false;
            }
        } finally {
            PROCESS_EXECUTOR.executeProcess("rm", testPath);
            PROCESS_EXECUTOR.executeProcess(
                    "mount", "-o", "remount,ro", IMAGE_DIRECTORY);
        }
    }
    
    
    /**
     * opens a clicked link in a browser
     *
     * @param evt the corresponding HyperlinkEvent
     */
    public static void openLinkInBrowser(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//            try {
//                Desktop.getDesktop().browse(evt.getURL().toURI());
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, "could not open URL", ex);
//            } catch (URISyntaxException ex) {
//                logger.log(Level.SEVERE, "could not open URL", ex);
//            }

            // as long as Konqueror sucks so bad, we enforce firefox
            // (this is a quick and dirty solution, if konqueror starts to be
            // usable, switch back to the code above)
            final HyperlinkEvent finalEvent = evt;
            Thread browserThread = new Thread() {
                @Override
                public void run() {
                    PROCESS_EXECUTOR.executeProcess(new String[]{
                        "firefox", finalEvent.getURL().toString()});
                }
            };
            browserThread.start();
        }
    }
}
