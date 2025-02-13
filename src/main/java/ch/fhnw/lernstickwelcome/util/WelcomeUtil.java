/*
 * Copyright (C) 2017 FHNW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.controller.exception.TableCellValidationException;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.util.ProcessExecutor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class contains help functions for the Lernstick Welcome Application
 *
 * @author sschw
 */
public class WelcomeUtil {

    private static final Logger LOGGER
            = Logger.getLogger(WelcomeUtil.class.getName());
    private static final ProcessExecutor PROCESS_EXECUTOR
            = WelcomeModelFactory.getProcessExecutor();
    /**
     * see {@link #isImageWritable()} *
     */
    private static volatile Boolean isImageWritable;
    
    public static String ARCHITECTURE = detectArchitecture();

    private WelcomeUtil() {
    }
    
    /**
    * returns the current architecture
    * @return the architecture the program is currently running on
    */
    private static String detectArchitecture() {
        return System.getProperty("os.arch");
    }

    /**
     * Parses a file as a xml file
     *
     * @param file The file that should be converted to a xml document
     * @return the xml document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
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

    /**
     * Parses a inputStream as a xml file
     *
     * @param is The inputStream that should be converted to a xml document
     * @return the xml document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document parseXmlFile(InputStream is)
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
        return builder.parse(is);
    }

    /**
     * Help function which reads a whole file content into the memory.
     *
     * @param file the file which content should be loaded into the memory
     * @return
     * @throws IOException
     */
    public static List<String> readFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file), Charset.defaultCharset())
        )) {
            for (String line = reader.readLine(); line != null;
                    line = reader.readLine()) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Checks if mounting internal filesystems is allowed.
     * <br>
     * Reads out the {@link WelcomeConstants#PKLA_PATH} File to check if a rule
     * is defined.
     *
     * @return
     */
    public static boolean isInternalFileSystemMountAllowed() {
        return !Files.exists(Paths.get(WelcomeConstants.EXAM_POLKIT_PATH,
                "10-udisks2-mount-system_strict.pkla"));
    }

    /**
     * Checks if mounting external filesystems is allowed.
     * <br>
     * Reads out the {@link WelcomeConstants#PKLA_PATH} File to check if a rule
     * is defined.
     *
     * @return
     */
    public static boolean isExternalFileSystemMountAllowed() {
        return !Files.exists(Paths.get(WelcomeConstants.EXAM_POLKIT_PATH,
                "10-udisks2-mount_strict.pkla"));
    }

    /**
     * Checks if the partition has rw rights.
     * <br>
     * Calls the following commands to check if the partition has rw rights:
     * <ul>
     * <li> "{@code mount -o remount,rw}
     * {@link WelcomeConstants#IMAGE_DIRECTORY}"
     * </li>
     * <li> "{@code touch}
     * {@link WelcomeConstants#IMAGE_DIRECTORY}{@code /lernstickWelcome.tmp}"
     * </li>
     * <li> "{@code rm}
     * {@link WelcomeConstants#IMAGE_DIRECTORY}{@code /lernstickWelcome.tmp}"
     * </li>
     * <li> "{@code mount -o remount,ro}
     * {@link WelcomeConstants#IMAGE_DIRECTORY}"
     * </li>
     * </ul>
     * This ensures that the image is mounted with ro rights but still can be
     * checked for its rw rights.
     * <br>
     * Because it needs much time to calculate the result will be saved locally
     * in the {@link #isImageWritable} variable.
     *
     * @return
     */
    public static boolean isImageWritable() {
        if (isImageWritable == null) {
            PROCESS_EXECUTOR.executeProcess("mount", "-o", "remount,rw",
                    WelcomeConstants.IMAGE_DIRECTORY);
            String testPath = WelcomeConstants.IMAGE_DIRECTORY
                    + "/lernstickWelcome.tmp";
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
                PROCESS_EXECUTOR.executeProcess("mount", "-o", "remount,ro",
                        WelcomeConstants.IMAGE_DIRECTORY);
            }
        }
        return isImageWritable;
    }

    /**
     * Help function to check if the port range is formatted correctly.
     *
     * @param portRange port formatted as range XXXX:XXXX or as single port XXXX
     * @param index if the calculation is inside a table, also give the
     * TableCell index
     * @throws TableCellValidationException
     */
    public static void checkPortRange(String portRange, int index)
            throws TableCellValidationException {

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
                throw new TableCellValidationException(
                        "WelcomeUtil.Error_PortRange", index, 2);
        }
    }

    /**
     * Help function to check if the port is formatted correctly.
     * <br>
     * Called by {@link #checkPortRange(java.lang.String, int) }
     *
     * @param portString the port
     * @param index if the calculation is inside a table, also give the
     * TableCell index
     * @throws TableCellValidationException
     */
    private static void checkPortString(String portString, int index)
            throws TableCellValidationException {
        try {
            int portNumber = Integer.parseInt(portString);
            if ((portNumber < 0) || (portNumber > 65535)) {
                throw new TableCellValidationException(
                        "WelcomeUtil.Error_PortRange", index, 2);
            }
        } catch (NumberFormatException ex) {
            throw new TableCellValidationException(
                    "WelcomeUtil.Error_PortRange", index, 2);
        }
    }

    /**
     * Help function to check if the host/ip is formatted correctly.
     *
     * @param target connection target formatted as host address or ip address
     * @param index if the calculation is inside a table, also give the
     * TableCell index
     * @throws TableCellValidationException
     */
    public static void checkTarget(String target, int index)
            throws TableCellValidationException {

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
                    throw new TableCellValidationException(
                            "WelcomeUtil.Error_PrefixLength",
                            index, 1, prefixLengthString);
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

    /**
     * Help function to check if the hostname is formatted correctly.
     * <br>
     * Called by {@link #checkTarget(java.lang.String, int) }
     *
     * @param string connection target formatted as host address
     * @param index if the calculation is inside a table, also give the
     * TableCell index
     * @throws TableCellValidationException
     */
    private static void checkHostName(String string, int index)
            throws TableCellValidationException {

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
            throw new TableCellValidationException(
                    "WelcomeUtil.Error_No_Hostname", index, 1);
        }

        if (string.length() > 255) {
            throw new TableCellValidationException(
                    "WelcomeUtil.Error_HostnameLength", index, 1, string);
        }

        String[] labels = string.split("\\.");
        for (String label : labels) {
            if (label.length() > 63) {
                throw new TableCellValidationException(
                        "WelcomeUtil.Error_LabelLength", index, 1, label);
            }
            for (int i = 0, length = label.length(); i < length; i++) {
                char c = label.charAt(i);
                if ((c != '-')
                        && ((c < '0') || (c > '9'))
                        && ((c < 'A') || (c > 'Z'))
                        && ((c < 'a') || (c > 'z'))) {
                    throw new TableCellValidationException(
                            "WelcomeUtil.Error_Invalid_Hostname_Character",
                            index, 1, c);
                }
            }
        }
    }

    /**
     * Help function to check if the ip is formatted correctly.
     * <br>
     * Called by {@link #checkTarget(java.lang.String, int) }
     *
     * @param string connection target formatted as ip address
     * @param index if the calculation is inside a table, also give the
     * TableCell index
     * @throws TableCellValidationException
     */
    private static void checkIPv4Address(String string, int index)
            throws TableCellValidationException {

        String[] octetStrings = string.split("\\.");
        for (String octetString : octetStrings) {
            try {
                int octet = Integer.parseInt(octetString);
                if (octet < 0 || octet > 255) {
                    throw new TableCellValidationException(
                            "WelcomeUtil.Error_Octet",
                            index, 1, string, octetString);
                }
            } catch (NumberFormatException ex) {
                throw new TableCellValidationException(
                        "WelcomeUtil.Error_Octet",
                        index, 1, string, octetString);
            }
        }
    }

    /**
     * Opens a clicked link in the browser of the Lernstick (Firefox)
     *
     * @param url the corresponding url
     */
    public static void openLinkInBrowser(String url) {
        // hostServices.showDocument() doesn't work with OpenJDK/OpenJFX
        // getHostServices() just throws the following exception:
        // java.lang.ClassNotFoundException:
        //   com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory

        // as long as Konqueror sucks so bad, we enforce firefox
        // (this is a quick and dirty solution, if konqueror starts to be
        // usable, switch back to the code above)
        try {
            final URL finalUrl = (new URI(url)).toURL();
            Thread browserThread = new Thread() {
                @Override
                public void run() {
                    PROCESS_EXECUTOR.executeProcess(new String[]{
                        "sudo", "-u", "user", "firefox", finalUrl.toString()});
                }
            };
            browserThread.start();
        } catch (MalformedURLException | URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
