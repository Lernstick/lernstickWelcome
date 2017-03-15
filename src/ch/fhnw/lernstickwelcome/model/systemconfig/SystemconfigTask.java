/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.systemconfig;

import ch.fhnw.lernstickwelcome.controller.ProcessingException;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.WelcomeUtil;
import ch.fhnw.util.MountInfo;
import ch.fhnw.util.Partition;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import org.xml.sax.SAXException;

/**
 *
 * @author sschw
 */
public class SystemconfigTask extends Task<Boolean> {
    private ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(SystemconfigTask.class.getName());
    private static final String IMAGE_DIRECTORY = "/lib/live/mount/medium";
    private static final String LOCAL_POLKIT_PATH
            = "/etc/polkit-1/localauthority/50-local.d";
    
    // Some functions are only required in exam env.
    private boolean isExamEnv;
    private Partition bootConfigPartition;
    private Partition exchangePartition;
    private StorageDevice systemStorageDevice;
    
    private IntegerProperty timeoutSeconds = new SimpleIntegerProperty();
    private StringProperty systemname = new SimpleStringProperty();
    private StringProperty systemversion = new SimpleStringProperty();
    private StringProperty password = new SimpleStringProperty();
    private StringProperty passwordRepeat = new SimpleStringProperty();
    
    private String oldUsername;
    private StringProperty username = new SimpleStringProperty();
    private BooleanProperty blockKdeDesktopApplets = new SimpleBooleanProperty();
    private BooleanProperty directSoundOutput = new SimpleBooleanProperty();
    private BooleanProperty allowAccessToOtherFilesystems = new SimpleBooleanProperty();
    private MountInfo bootConfigMountInfo;
    
    public SystemconfigTask(boolean isExamEnv, Properties properties) {
        this.isExamEnv = isExamEnv;
        
        blockKdeDesktopApplets.set("true".equals(
                properties.getProperty(WelcomeConstants.KDE_LOCK)));
        allowAccessToOtherFilesystems.set(WelcomeUtil.isFileSystemMountAllowed());
        getPartitions();
        getBootConfigInfos();
        getFullUserName();
    }

    @Override
    protected Boolean call() throws Exception {
        if(username.get().equals(oldUsername))
            LOGGER.log(Level.INFO,
                    "updating full user name to \"{0}\"", username.get());
            PROCESS_EXECUTOR.executeProcess("chfn", "-f", username.get(), "user");
        return true;
    }

    private void getBootConfigInfos() {
        // timeoutSeconds.setValue(10); // One Customer wanted 10sec. by default.
        
        try {
            timeoutSeconds.set(getTimeout());
        } catch (IOException | DBusException ex) {
            LOGGER.log(Level.WARNING, "could not set boot timeout value", ex);
        }
        
        // Read XmlBootConfig
        try {
            File xmlBootConfigFile = getXmlBootConfigFile();
            if (xmlBootConfigFile != null) {
                Document xmlBootDocument = WelcomeUtil.parseXmlFile(xmlBootConfigFile);
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

    private void getFullUserName() {
        PROCESS_EXECUTOR.executeProcess(true, true, "getent", "passwd", "user");
        List<String> stdOut = PROCESS_EXECUTOR.getStdOutList();
        if (stdOut.isEmpty()) {
            LOGGER.warning("getent returned no result!");
        } else {
            // getent passwd returns a line with the following pattern:
            // login:encrypted_password:id:gid:gecos_field:home:shell
            String line = stdOut.get(0);
            String[] tokens = line.split(":");
            if (tokens.length < 5) {
                LOGGER.log(Level.WARNING,
                        "can not parse getent line:\n{0}", line);
            } else {
                String gecosField = line.split(":")[4];
                // the "gecos_field" has the following syntax:
                // full_name,room_nr,phone_work,phone_private,misc
                username.set(gecosField.split(",")[0]);
                oldUsername = username.get();
            }
        }
    }
    
    private void getPartitions() {
        systemStorageDevice = WelcomeModelFactory.getSystemStorageDevice();
        if (systemStorageDevice != null) {
            exchangePartition = systemStorageDevice.getExchangePartition();

            Partition efiPartition = systemStorageDevice.getEfiPartition();
            if (efiPartition != null && efiPartition.getIdLabel().equals(Partition.EFI_LABEL)) {
                // current partitioning scheme, the boot config is on the
                // *system* partition!
                bootConfigPartition = systemStorageDevice.getSystemPartition();
            } else {
                // pre 2016-02 partitioning scheme with boot config files on
                // boot partition
                bootConfigPartition = efiPartition;
            }
            if (bootConfigPartition != null) {
                try {
                    bootConfigMountInfo = bootConfigPartition.mount();
                } catch (DBusException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
        LOGGER.log(Level.INFO, "\nsystemStorageDevice: {0}\n"
                + "exchangePartition: {1}\nbootConfigPartition: {2}",
                new Object[]{systemStorageDevice,
                    exchangePartition, bootConfigPartition});
    }

    private void updateBootloaders() throws DBusException {
        final int timeout = timeoutSeconds.get();
        final String systemName = systemname.get();
        final String systemVersion = systemversion.get();

        Partition.Action<Void> updateBootloaderAction
                = new Partition.Action<Void>() {

            @Override
            public Void execute(File mountPath) {
                try {
                    updateBootloaders(mountPath, timeout,
                            systemName, systemVersion);
                } catch (DBusException ex) {
                    LOGGER.log(Level.SEVERE, "", ex);
                }
                return null;
            }
        };

        if (bootConfigPartition == null || systemStorageDevice.getEfiPartition().getIdLabel().equals(Partition.EFI_LABEL)) {
            // legacy system without separate boot partition or
            // post 2016-02 partition schema where the boot config files are
            // located again on the system partition

            // make image temporarily writable
            PROCESS_EXECUTOR.executeProcess(
                    "mount", "-o", "remount,rw", IMAGE_DIRECTORY);

            updateBootloaders(new File(IMAGE_DIRECTORY),
                    timeout, systemName, systemVersion);

            // remount image read-only
            PROCESS_EXECUTOR.executeProcess(
                    "mount", "-o", "remount,ro", IMAGE_DIRECTORY);
        } else {
            // system with a separate boot partition
            bootConfigPartition.executeMounted(updateBootloaderAction);
        }
        if (exchangePartition != null) {
            exchangePartition.executeMounted(updateBootloaderAction);
        }
    }
    
    private void updateBootloaders(File directory, int timeout,
            String systemName, String systemVersion) throws DBusException {

        // syslinux
        for(File syslinuxConfigFile : getSyslinuxConfigFiles(directory)) {
            PROCESS_EXECUTOR.executeProcess("sed", "-i", "-e",
                    "s|timeout .*|timeout " + (timeout * 10) + "|1",
                    syslinuxConfigFile.getPath());
        }

        // xmlboot
        File xmlBootConfigFile = getXmlBootConfigFile(directory);
        if (xmlBootConfigFile != null) {
            try {
                Document xmlBootDocument = WelcomeUtil.parseXmlFile(xmlBootConfigFile);
                xmlBootDocument.getDocumentElement().normalize();
                Node systemNode = xmlBootDocument.
                        getElementsByTagName("system").item(0);
                Element systemElement = (Element) systemNode;
                Node node = systemElement.getElementsByTagName("text").item(0);
                if (node != null) {
                    node.setTextContent(systemName);
                }
                node = systemElement.getElementsByTagName("version").item(0);
                if (node != null) {
                    node.setTextContent(systemVersion);
                }

                // write changes back to config file
                File tmpFile = File.createTempFile("lernstickWelcome", "tmp");
                TransformerFactory transformerFactory
                        = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(xmlBootDocument);
                StreamResult result = new StreamResult(tmpFile);
                transformer.transform(source, result);
                PROCESS_EXECUTOR.executeProcess("mv", tmpFile.getPath(),
                        xmlBootConfigFile.getPath());

                // rebuild bootlogo so that the changes are visible right after
                // reboot
                File bootlogoDir = xmlBootConfigFile.getParentFile();
                File syslinuxDir = bootlogoDir.getParentFile();
                PROCESS_EXECUTOR.executeProcess("gfxboot",
                        "--archive", bootlogoDir.getPath(),
                        "--pack-archive", syslinuxDir.getPath() + "/bootlogo");
            } catch (ParserConfigurationException | SAXException | IOException |
                    DOMException | TransformerException ex) {
                LOGGER.log(Level.WARNING, "can not update xmlboot config", ex);
            }
        }

        // grub
        String grubMainConfigFilePath = directory + "/boot/grub/grub_main.cfg";
        if (new File(grubMainConfigFilePath).exists()) {
            PROCESS_EXECUTOR.executeProcess("sed", "-i", "-e",
                    "s|set timeout=.*|set timeout=" + timeout + "|1",
                    grubMainConfigFilePath);
        }
        String grubThemeFilePath
                = directory + "/boot/grub/themes/lernstick/theme.txt";
        if (new File(grubThemeFilePath).exists()) {
            PROCESS_EXECUTOR.executeProcess("sed", "-i", "-e",
                    "s|num_ticks = .*|num_ticks = " + timeout + "|1;"
                    + "s|title-text: .*|title-text: \""
                    + systemName + ' ' + systemVersion + "\"|1",
                    grubThemeFilePath);
        }
    }
    
    private List<File> getSyslinuxConfigFiles(File directory) {

        List<File> configFiles = new ArrayList<>();

        // check all known locations of syslinux config files
        String[] syslinuxDirs = new String[]{
            "isolinux",
            "syslinux"
        };
        String[] syslinuxFiles = new String[]{
            "isolinux.cfg",
            "syslinux.cfg",
            "boot_486.cfg",
            "boot_686.cfg"
        };
        for (String syslinuxDir : syslinuxDirs) {
            for (String syslinuxFile : syslinuxFiles) {
                File configDir = new File(directory, syslinuxDir);
                File configFile = new File(configDir, syslinuxFile);
                if (configFile.exists()) {
                    configFiles.add(configFile);
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0, size = configFiles.size(); i < size; i++) {
            stringBuilder.append(configFiles.get(i));
            if (i < size - 1) {
                stringBuilder.append('\n');
            }
        }

        LOGGER.log(Level.INFO, "syslinux config files: \n{0}",
                stringBuilder.toString());

        return configFiles;
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

    private int getTimeout() throws IOException, DBusException {

        // use syslinux configuration as reference for the timeout setting
        List<File> syslinuxConfigFiles;
        if (bootConfigPartition == null) {
            // legacy system
            syslinuxConfigFiles = getSyslinuxConfigFiles(
                    new File(IMAGE_DIRECTORY));
        } else {
            // system with a separate boot partition
            syslinuxConfigFiles = bootConfigPartition.executeMounted(
                    new Partition.Action<List<File>>() {
                @Override
                public List<File> execute(File mountPath) {
                    return getSyslinuxConfigFiles(mountPath);
                }
            });
        }

        Pattern timeoutPattern = Pattern.compile("timeout (.*)");
        for (File syslinuxConfigFile : syslinuxConfigFiles) {
            List<String> configFileLines = WelcomeUtil.readFile(syslinuxConfigFile);
            for (String configFileLine : configFileLines) {
                Matcher matcher = timeoutPattern.matcher(configFileLine);
                if (matcher.matches()) {
                    String timeoutString = matcher.group(1);
                    try {
                        return Integer.parseInt(timeoutString) / 10;
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING,
                                "could not parse timeout value \"{0}\"",
                                timeoutString);
                    }
                }
            }
        }
        return -1;
    }

    private void changePassword() throws ProcessingException {
        // check, if both passwords are the same
        String password1 = password.get();
        String password2 = passwordRepeat.get();
        if (!password1.equals(password2)) {
            throw new ProcessingException("Password_Mismatch");
        }

        // ok, passwords match, change password
        String passwordChangeScript = "#!/bin/sh\n"
                + "echo \"user:" + password1 + "\""
                + " | /usr/sbin/chpasswd\n";
        ProcessExecutor executor = new ProcessExecutor();
        try {
            int returnValue = executor.executeScript(
                    true, true, passwordChangeScript);
            if (returnValue == 0) {
                passwordEnabled();
            } else {
                throw new ProcessingException("Password_Change_Error");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
    }

    private void passwordEnabled() throws ProcessingException {
        // TODO: the password hint is deprecated
        // remove this code block somewhen in the future...

        // disable password hint
        File configFile = new File(
                "/home/user/.kde/share/config/empty_passwd_info");
        if (!configFile.exists()) {
            try (FileWriter fileWriter = new FileWriter(configFile)) {
                // write kdialog config file
                fileWriter.write("[Notification Messages]\n"
                        + "show=false");

                // fix ownership of kdialog config file:
                Path path = configFile.toPath();
                UserPrincipalLookupService lookupService
                        = FileSystems.getDefault().getUserPrincipalLookupService();
                // set user
                Files.setOwner(path, lookupService.lookupPrincipalByName("user"));
                // set group            
                PosixFileAttributeView fileAttributeView
                        = Files.getFileAttributeView(path,
                                PosixFileAttributeView.class);
                fileAttributeView.setGroup(
                        lookupService.lookupPrincipalByGroupName("user"));
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "", ex);
            }
        }

        // add polkit rules to enforce authentication
        // rules for our own applications
        addStrictPKLA("10-welcome.pkla", "enforce authentication before "
                + "running the Lernstick Welcome application",
                "ch.lernstick.welcome");
        addStrictPKLA("10-dlcopy.pkla", "enforce authentication before "
                + "running the Lernstick storage media management application",
                "ch.lernstick.dlcopy");

        // harden our custom rules for third party applications
        hardenPKLAs("gnome-system-log", "packagekit", "synaptic", "udisks2");
    }

    private void addStrictPKLA(
            String fileName, String description, String action) {
        File strictPKLA = new File(LOCAL_POLKIT_PATH, fileName);
        String strictWelcomeRule
                = "[" + description + "]\n"
                + "Identity=unix-user:*\n"
                + "Action=" + action + "\n"
                + "ResultAny=auth_self\n"
                + "ResultInactive=auth_self\n"
                + "ResultActive=auth_self\n";
        try (FileWriter fileWriter = new FileWriter(strictPKLA)) {
            fileWriter.write(strictWelcomeRule);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }
    }

    private void hardenPKLAs(String... pklas) throws ProcessingException {
        Pattern yesPattern = Pattern.compile("(.*)=yes");
        for (String pkla : pklas) {
            try {
                Path path = Paths.get(
                        LOCAL_POLKIT_PATH, "10-" + pkla + ".pkla");
                List<String> lenientLines = Files.readAllLines(
                        path, StandardCharsets.UTF_8);
                List<String> strictLines = new ArrayList<>();
                for (String lenientLine : lenientLines) {
                    Matcher matcher = yesPattern.matcher(lenientLine);
                    if (matcher.matches()) {
                        lenientLine = matcher.group(1) + "=auth_self";
                    }
                    strictLines.add(lenientLine);
                }
                Files.write(path, strictLines, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new ProcessingException(ex.getMessage());
            }
        }
    }
    
    public void umountBootConfig() {
        if ((bootConfigMountInfo != null) && (!bootConfigMountInfo.alreadyMounted())) {
            try {
                bootConfigPartition.umount();
            } catch (DBusException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

}
