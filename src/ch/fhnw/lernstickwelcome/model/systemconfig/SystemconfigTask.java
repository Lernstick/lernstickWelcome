/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.systemconfig;

import ch.fhnw.util.Partition;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.freedesktop.dbus.exceptions.DBusException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author sschw
 */
public class SystemconfigTask extends Task<Boolean> {
    private final static Logger LOGGER = Logger.getLogger(SystemconfigTask.class.getName());
    private static final String IMAGE_DIRECTORY = "/lib/live/mount/medium";
    
    private Partition bootConfigPartition;
    
    private IntegerProperty timeoutSeconds = new SimpleIntegerProperty();
    private StringProperty systemname = new SimpleStringProperty();
    private StringProperty systemversion = new SimpleStringProperty();
    
    private StringProperty username = new SimpleStringProperty();
    private BooleanProperty blockKdeDesktopApplets = new SimpleBooleanProperty();
    private BooleanProperty directSoundOutput = new SimpleBooleanProperty();
    
    public SystemconfigTask() {
        setDefaultValues();
    }

    @Override
    protected Boolean call() throws Exception {
        return true;
    }

    private void setDefaultValues() {
        timeoutSeconds.setValue(10);
        
        // Read XmlBootConfig
        try {
            File xmlBootConfigFile = getXmlBootConfigFile();
            if (xmlBootConfigFile != null) {
                Document xmlBootDocument = parseXmlFile(xmlBootConfigFile);
                xmlBootDocument.getDocumentElement().normalize();
                Node systemNode = xmlBootDocument.getElementsByTagName(
                        "system").item(0);
                Element systemElement = (Element) systemNode;
                Node node = systemElement.getElementsByTagName("text").item(0);
                if (node != null) {
                    systemname.setValue(node.getTextContent());
                }
                node = systemElement.getElementsByTagName("version").item(0);
                if (node != null) {
                    systemversion.setValue(node.getTextContent());
                }
            }
        } catch (ParserConfigurationException | SAXException |
                IOException | DBusException ex) {
            LOGGER.log(Level.WARNING, "could not parse xmlboot config", ex);
        }
    }
    
    
    private File getXmlBootConfigFile() throws DBusException {

        if (bootConfigPartition == null) {
            // legacy system
            File configFile = getXmlBootConfigFile(new File(IMAGE_DIRECTORY));
            if (configFile != null) {
                return configFile;
            }
        } else {
            // system with a separate boot partition
            File configFile = bootConfigPartition.executeMounted(
                    new Partition.Action<File>() {

                @Override
                public File execute(File mountPath) {
                    return getXmlBootConfigFile(mountPath);
                }
            });
            if (configFile != null) {
                return configFile;
            }
        }

        return null;
    }

    private File getXmlBootConfigFile(File directory) {
        // search through all known variants
        String[] dirs = new String[]{"isolinux", "syslinux"};
        String[] subdirs = new String[]{"/", "/bootlogo.dir/"};
        for (String dir : dirs) {
            for (String subdir : subdirs) {
                File configFile = new File(
                        directory, dir + subdir + "xmlboot.config");
                if (configFile.exists()) {
                    LOGGER.log(Level.INFO,
                            "xmlboot config file: {0}", configFile);
                    return configFile;
                }
            }
        }
        return null;
    }

    private Document parseXmlFile(File file)
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

}
