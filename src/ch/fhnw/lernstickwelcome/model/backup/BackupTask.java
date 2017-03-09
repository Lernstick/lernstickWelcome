/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.backup;

import ch.fhnw.lernstickwelcome.controller.ProcessingException;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.WelcomeUtil;
import ch.fhnw.util.Partition;
import ch.fhnw.util.ProcessExecutor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.freedesktop.dbus.exceptions.DBusException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author user
 */
public class BackupTask extends Task<Boolean> {
    private final static Logger LOGGER = Logger.getLogger(BackupTask.class.getName());
    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    
    private BooleanProperty active = new SimpleBooleanProperty();
    private StringProperty sourcePath = new SimpleStringProperty();
    private BooleanProperty local = new SimpleBooleanProperty();
    private StringProperty destinationPath = new SimpleStringProperty();
    private BooleanProperty partition = new SimpleBooleanProperty();
    private StringProperty partitionPath = new SimpleStringProperty();
    private BooleanProperty screenshot = new SimpleBooleanProperty();
    
    public BackupTask() {
    }

    @Override
    protected Boolean call() throws Exception {
        updateProgress(0, 1);
        if(active.get()) {
            
        }
        updateProgress(1, 1);
        return true;
    }
    

    private boolean checkBackupDirectory() throws ProcessingException {

        if ((!active.get()) || (!local.get())) {
            // As long as the directory option is not selected we just don't
            // care what is configured there...
            return true;
        }

        if (destinationPath.get().isEmpty()) {
            throw new ProcessingException("Error_No_Backup_Directory");
        }

        File dirFile = new File(destinationPath.get());
        if (dirFile.exists()) {
            if (!dirFile.isDirectory()) {
                throw new ProcessingException("Error_Backup_Directory_No_Directory", destinationPath.get());
            }

            String[] files = dirFile.list();
            if ((files != null) && (files.length != 0)) {
                int returnValue = PROCESS_EXECUTOR.executeProcess(
                        "rdiff-backup", "-l", dirFile.getAbsolutePath());
                if (returnValue != 0) {
                    throw new ProcessingException("Error_Backup_Directory_Invalid", destinationPath.get());
                }
            }
        }

        // determine device where the directory is located
        // (df takes care for symlinks etc.)
        PROCESS_EXECUTOR.executeProcess(true, true, "df", destinationPath.get());
        List<String> stdOut = PROCESS_EXECUTOR.getStdOutList();
        String device = null;
        for (String line : stdOut) {
            if (line.startsWith("/dev/")) {
                String[] tokens = line.split(" ");
                device = tokens[0];
            }
        }
        if (device == null) {
            LOGGER.log(Level.WARNING,
                    "could not determine device of directory {0}",
                    destinationPath.get());
            return true;
        }

        // check, if device is exFAT
        try {
            Partition partition = Partition.getPartitionFromDeviceAndNumber(
                    device.substring(5));
            String idType = partition.getIdType();
            if (idType.equals("exfat")) {
                // rdiff-backup does not work (yet) on exfat partitions!
                throw new ProcessingException("Error_Backup_on_exFAT", destinationPath.get());
            }
        } catch (DBusException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }

        return true;
    }

    private void updateJBackpackProperties(
            String backupSource, String backupDestination) {
        // update JBackpack preferences of the default user
        File prefsDirectory = new File(
                "/home/user/.java/.userPrefs/ch/fhnw/jbackpack/");
        updateJBackpackProperties(prefsDirectory, true);

        // update JBackpack preferences of the root user
        prefsDirectory = new File(
                "/root/.java/.userPrefs/ch/fhnw/jbackpack/");
        updateJBackpackProperties(prefsDirectory, false);
    }
    
    private void updateJBackpackProperties(File prefsDirectory, boolean chown) {
        File prefsFile = new File(prefsDirectory, "prefs.xml");
        String prefsFilePath = prefsFile.getPath();
        if (prefsFile.exists()) {
            try {
                Document xmlBootDocument = WelcomeUtil.parseXmlFile(prefsFile);
                xmlBootDocument.getDocumentElement().normalize();
                Node mapNode
                        = xmlBootDocument.getElementsByTagName("map").item(0);
                Element mapElement = (Element) mapNode;
                NodeList entries = mapElement.getElementsByTagName("entry");
                for (int i = 0, length = entries.getLength(); i < length; i++) {
                    Element entry = (Element) entries.item(i);
                    String key = entry.getAttribute("key");
                    switch (key) {
                        case "destination":
                            entry.setAttribute("value", "local");
                            break;
                        case "local_destination_directory":
                            entry.setAttribute("value", destinationPath.get());
                            break;
                        case "source":
                            entry.setAttribute("value", sourcePath.get());
                            break;
                    }
                }

                // write changes back to config file
                File tmpFile = File.createTempFile("lernstickWelcome", "tmp");
                TransformerFactory transformerFactory
                        = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                        "http://java.sun.com/dtd/preferences.dtd");
                DOMSource source = new DOMSource(xmlBootDocument);
                StreamResult result = new StreamResult(tmpFile);
                transformer.transform(source, result);
                PROCESS_EXECUTOR.executeProcess(
                        "mv", tmpFile.getPath(), prefsFilePath);
                PROCESS_EXECUTOR.executeProcess(
                        "chown", "user.user", prefsFilePath);

            } catch (ParserConfigurationException | SAXException |
                    IOException | DOMException | TransformerException ex) {
                LOGGER.log(Level.WARNING, "can not update xmlboot config", ex);
            }

        } else {
            if (!prefsDirectory.exists() && !prefsDirectory.mkdirs()) {
                LOGGER.log(Level.WARNING,
                        "could not create directory {0}", prefsDirectory);
                return;
            }
            // create mininal JBackpack preferences
            String preferences
                    = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                    + "<!DOCTYPE map SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n"
                    + "<map MAP_XML_VERSION=\"1.0\">\n"
                    + "  <entry key=\"destination\" value=\"local\"/>\n"
                    + "  <entry key=\"local_destination_directory\" value=\"" + destinationPath.get() + "\"/>\n"
                    + "  <entry key=\"source\" value=\"" + sourcePath.get() + "\"/>\n"
                    + "</map>\n";

            try (FileWriter fileWriter = new FileWriter(prefsFile)) {
                fileWriter.write(preferences);
                fileWriter.flush();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "", ex);
            }
            if (chown) {
                PROCESS_EXECUTOR.executeProcess(
                        "chown", "-R", "user.user", "/home/user/.java/");
            }
        }
    }
    
}
