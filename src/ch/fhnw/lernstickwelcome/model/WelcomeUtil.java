/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import ch.fhnw.lernstickwelcome.controller.TableCellValidationException;
import ch.fhnw.util.LernstickFileTools;
import ch.fhnw.util.ProcessExecutor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static Boolean isImageWritable;
    
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
    
    public static boolean isFileSystemMountAllowed() {
        try {
            List<String> pklaRules
                    = LernstickFileTools.readFile(WelcomeConstants.PKLA_PATH.toFile());
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
        if(isImageWritable == null) {
            PROCESS_EXECUTOR.executeProcess(
                    "mount", "-o", "remount,rw", WelcomeConstants.IMAGE_DIRECTORY);
            String testPath = WelcomeConstants.IMAGE_DIRECTORY + "/lernstickWelcome.tmp";
            PROCESS_EXECUTOR.executeProcess("touch", testPath);
            File testFile = new File(testPath);
            try {
                if (testFile.exists()) {
                    LOGGER.info("image is writable");
                    isImageWritable = true;
                } else {
                    LOGGER.info("image is not writable");
                    isImageWritable = false;
                }
            } finally {
                PROCESS_EXECUTOR.executeProcess("rm", testPath);
                PROCESS_EXECUTOR.executeProcess(
                        "mount", "-o", "remount,ro", WelcomeConstants.IMAGE_DIRECTORY);
            }
        }
        return isImageWritable;
    }
    
    public static void checkPortRange(String portRange, int index) throws TableCellValidationException {
        String[] tokens = portRange.split(":");
        switch (tokens.length) {
            case 1:
                // simple port
                checkPortString(tokens[0], index);
                return;
            case 2:
                // port range
                checkPortString(tokens[0], index);
                checkPortString(tokens[1], index);
                return;

            default:
                // invalid syntax
                throw new TableCellValidationException("Error_PortRange", index, 2);
        }
    }

    public static void checkPortString(String portString, int index) throws TableCellValidationException {
        try {
            int portNumber = Integer.parseInt(portString);
            if ((portNumber < 0) || (portNumber > 65535)) {
                throw new TableCellValidationException("Error_PortRange", index, 2);
            }
        } catch (NumberFormatException ex) {
            throw new TableCellValidationException("Error_PortRange", index, 2);
        }
    }

    public static void checkTarget(String target, int index) throws TableCellValidationException {
        // a CIDR block has the syntax: <IP address>\<prefix length>
        String octetP = "\\p{Digit}{1,3}";
        String ipv4P = "(?:" + octetP + "\\.){3}" + octetP;
        Pattern cidrPattern = Pattern.compile("(" + ipv4P + ")/(\\p{Digit}*)");
        Matcher matcher = cidrPattern.matcher(target);
        if (matcher.matches()) {
            // check CIDR block syntax
            checkIPv4Address(matcher.group(1), index);

            String prefixLengthString = matcher.group(2);
            try {
                int prefixLength = Integer.parseInt(prefixLengthString);
                if (prefixLength < 0 || prefixLength > 32) {
                    throw new TableCellValidationException("Error_PrefixLength", index, 1, prefixLengthString);
                }
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING,
                        "could not parse " + prefixLengthString, ex);
            }
        } else {
            // check validity of plain IPv4 address or hostname
            Pattern ipv4Pattern = Pattern.compile(ipv4P);
            matcher = ipv4Pattern.matcher(target);
            if (matcher.matches()) {
                checkIPv4Address(target, index);
            } else { 
                checkHostName(target, index);
            }
        }
    }

    public static void checkHostName(String string, int index) throws TableCellValidationException {
        // Hostnames are composed of series of labels concatenated with dots, as
        // are all domain names. For example, "en.wikipedia.org" is a hostname.
        // Each label must be between 1 and 63 characters long, and the entire
        // hostname (including the delimiting dots) has a maximum of 255
        // characters.
        // The Internet standards (Request for Comments) for protocols mandate
        // that component hostname labels may contain only the ASCII letters
        // 'a' through 'z' (in a case-insensitive manner), the digits '0'
        // through '9', and the hyphen ('-').

        if (string.isEmpty()) {
            throw new TableCellValidationException("Error_No_Hostname", index, 1);
        }

        if (string.length() > 255) {
            throw new TableCellValidationException("Error_HostnameLength", index, 1, string);
        }

        String[] labels = string.split("\\.");
        for (String label : labels) {
            if (label.length() > 63) {
                throw new TableCellValidationException("Error_LabelLength", index, 1, label);
            }
            for (int i = 0, length = label.length(); i < length; i++) {
                char c = label.charAt(i);
                if ((c != '-')
                        && ((c < '0') || (c > '9'))
                        && ((c < 'A') || (c > 'Z'))
                        && ((c < 'a') || (c > 'z'))) {
                    throw new TableCellValidationException("Error_Invalid_Hostname_Character", index, 1, c);
                }
            }
        }
    }

    public static void checkIPv4Address(String string, int index) throws TableCellValidationException {
        String[] octetStrings = string.split("\\.");
        for (String octetString : octetStrings) {
            int octet = Integer.parseInt(octetString);
            if (octet < 0 || octet > 255) {
                throw new TableCellValidationException("Error_Octet", index, 1, string, octetString);
            }
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
