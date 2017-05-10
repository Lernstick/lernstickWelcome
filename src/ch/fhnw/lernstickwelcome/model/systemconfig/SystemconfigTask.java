package ch.fhnw.lernstickwelcome.model.systemconfig;

import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.util.LernstickFileTools;
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
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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
 * This class handles system changes for the Lernstick.
 * <br>
 * In order to process a backend task multiple times it extends Processable
 * 
 * @see Processable
 * 
 * @author sschw
 */
public class SystemconfigTask implements Processable<String> {

    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(SystemconfigTask.class.getName());

    // Some functions are only required in exam env.
    private boolean isExamEnv; // TODO Block functions for Std. Version
    private boolean passwordChanged;
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
    private Properties properties;

    /**
     * Creates a SystemconfigTask by loading the values from the properties or bootconfig.
     * 
     * @param isExamEnv Some functions won't be load in Std. Version
     * @param properties Property File of the Welcome Application
     */
    public SystemconfigTask(boolean isExamEnv, Properties properties) {
        this.isExamEnv = isExamEnv;
        this.properties = properties;

        // Load properties
        blockKdeDesktopApplets.set("true".equals(properties.getProperty(WelcomeConstants.KDE_LOCK)));
        passwordChanged = "true".equals(properties.getProperty(WelcomeConstants.PASSWORD_CHANGED));
        allowAccessToOtherFilesystems.set(WelcomeUtil.isFileSystemMountAllowed());
        // Load Sound Output
        directSoundOutput.set(!Files.exists(WelcomeConstants.ALSA_PULSE_CONFIG_FILE));
        // Load partitions
        getPartitions();
        // Load BootConfigInfos from the BootPartition
        getBootConfigInfos();
        // Load the Username
        getFullUserName();
    }

    /**
     * Update the access to other filesystems by editing the PKLA File.
     * <br>
     * Uses the {@link #allowAccessToOtherFilesystems} Property to check how the
     * option has to be adjusted.
     */
    private void updateAllowFilesystemMount() {
        try {
            if (allowAccessToOtherFilesystems.get()) {
                LernstickFileTools.replaceText(WelcomeConstants.UDISKS_PKLA_PATH.toString(), Pattern.compile("=auth_.*"), "=yes");
            } else {
                LernstickFileTools.replaceText(WelcomeConstants.UDISKS_PKLA_PATH.toString(), Pattern.compile("=yes"), "=auth_self_keep");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
    }

    /**
     * Loads the bootconfig info by reading out values from the xmlBootConfig and the syslinuxConfigFile.
     */
    private void getBootConfigInfos() {
        // Read out the bootloader timeout in seconds
        try {
            timeoutSeconds.set(getTimeout());
        } catch (IOException | DBusException ex) {
            LOGGER.log(Level.WARNING, "could not set boot timeout value", ex);
        }

        // Read XmlBootConfig
        try {
            File xmlBootConfigFile = getXmlBootConfigFile();
            if (xmlBootConfigFile != null) {
                // Convert the boot config to a xml
                Document xmlBootDocument = WelcomeUtil.parseXmlFile(xmlBootConfigFile);
                xmlBootDocument.getDocumentElement().normalize();
                // Search for the system tag
                Node systemNode = xmlBootDocument.getElementsByTagName(
                        "system").item(0);
                Element systemElement = (Element) systemNode;
                // Read out the systemnameProperty (text tag)
                Node node = systemElement.getElementsByTagName("text").item(0);
                if (node != null) {
                    systemname.setValue(node.getTextContent());
                }
                // Read out the systemversion (version tag)
                node = systemElement.getElementsByTagName("version").item(0);
                if (node != null) {
                    systemversion.setValue(node.getTextContent());
                }
            }
        } catch (ParserConfigurationException | SAXException
                | IOException | DBusException ex) {
            LOGGER.log(Level.WARNING, "could not parse xmlboot config", ex);
        }
    }

    /**
     * Read out the full username by running {@code getent passwd user} with the {@link ProcessExecutor}
     */
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

    /**
     * Loads the partitions by using {@link WelcomeModelFactory#getSystemStorageDevice() }.
     * <br>
     * Loads the bootConfigParition and the exchangePartition.
     */
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

    /**
     * Updates the BootLoaders of the exchangePartition and the bootConfigPartition.
     * <br>
     * If boot config isn't seperated, it will do the action onto the running system.
     * @throws DBusException 
     */
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
                    "mount", "-o", "remount,rw", WelcomeConstants.IMAGE_DIRECTORY);

            updateBootloaders(new File(WelcomeConstants.IMAGE_DIRECTORY),
                    timeout, systemName, systemVersion);

            // remount image read-only
            PROCESS_EXECUTOR.executeProcess(
                    "mount", "-o", "remount,ro", WelcomeConstants.IMAGE_DIRECTORY);
        } else {
            // system with a separate boot partition
            bootConfigPartition.executeMounted(updateBootloaderAction);
        }
        if (exchangePartition != null) {
            exchangePartition.executeMounted(updateBootloaderAction);
        }
    }

    /**
     * Updates the bootloader of the given partition.
     * 
     * @param directory root directory of the partition
     * @param timeout the timeout that has to be set
     * @param systemName the systemName that has to be set
     * @param systemVersion the systemVersion that has to be set
     * @throws DBusException 
     */
    private void updateBootloaders(File directory, int timeout,
            String systemName, String systemVersion) throws DBusException {

        // syslinux
        for (File syslinuxConfigFile : getSyslinuxConfigFiles(directory)) {
            PROCESS_EXECUTOR.executeProcess("sed", "-i", "-e",
                    "s|timeout .*|timeout " + (timeout * 10) + "|1",
                    syslinuxConfigFile.getPath());
        }

        // xmlboot
        File xmlBootConfigFile = getXmlBootConfigFile(directory);
        if (xmlBootConfigFile != null) {
            try {
                // Convert the boot config to a xml
                Document xmlBootDocument = WelcomeUtil.parseXmlFile(xmlBootConfigFile);
                xmlBootDocument.getDocumentElement().normalize();
                // Search for the system tag
                Node systemNode = xmlBootDocument.
                        getElementsByTagName("system").item(0);
                Element systemElement = (Element) systemNode;
                // Read out the systemnameProperty (text tag)
                Node node = systemElement.getElementsByTagName("text").item(0);
                if (node != null) {
                    node.setTextContent(systemName);
                }
                // Read out the systemversion (version tag)
                node = systemElement.getElementsByTagName("version").item(0);
                if (node != null) {
                    node.setTextContent(systemVersion);
                }

                // write changes back to config file
                
                // Create a temp file.
                File tmpFile = File.createTempFile("lernstickWelcome", "tmp");
                TransformerFactory transformerFactory
                        = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(xmlBootDocument);
                StreamResult result = new StreamResult(tmpFile);
                transformer.transform(source, result);
                // Replace existing bootconfig with the temp file using mv.
                PROCESS_EXECUTOR.executeProcess("mv", tmpFile.getPath(),
                        xmlBootConfigFile.getPath());

                // rebuild bootlogo so that the changes are visible right after
                // reboot
                File bootlogoDir = xmlBootConfigFile.getParentFile();
                File syslinuxDir = bootlogoDir.getParentFile();
                PROCESS_EXECUTOR.executeProcess("gfxboot",
                        "--archive", bootlogoDir.getPath(),
                        "--pack-archive", syslinuxDir.getPath() + "/bootlogo");
            } catch (ParserConfigurationException | SAXException | IOException
                    | DOMException | TransformerException ex) {
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

    /**
     * Load the syslinuxConfigFile of the partition.
     * <br>
     * The following config files can be found:
     * <ul>
     * <li>{@code directory/isolinux/isolinux.cfg}</li>
     * <li>{@code directory/isolinux/syslinux.cfg}</li>
     * <li>{@code directory/isolinux/boot_486.cfg}</li>
     * <li>{@code directory/isolinux/boot_686.cfg}</li>
     * <li>{@code directory/syslinux/isolinux.cfg}</li>
     * <li>{@code directory/syslinux/syslinux.cfg}</li>
     * <li>{@code directory/syslinux/boot_486.cfg}</li>
     * <li>{@code directory/syslinux/boot_686.cfg}</li>
     * </ul>
     * @param directory root dir of the partition
     * @return config files in the syslinux folder
     */
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

    /**
     * Load the xmlBootConfigFile of the bootConfigPartition.
     * <br>
     * If the variable bootConfigPartition is null, the function will use the 
     * running system as bootConfigPartition.
     * @return the bootConfigFile or null if it doesn't exist
     * @throws DBusException 
     */
    private File getXmlBootConfigFile() throws DBusException {

        if (bootConfigPartition == null) {
            // legacy system
            File configFile = getXmlBootConfigFile(new File(WelcomeConstants.IMAGE_DIRECTORY));
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

    /**
     * Load the xmlBootConfigFile of the partition.
     * <br>
     * The File loaded by this function can be under the following paths: <br>
     * <ul>
     * <li>{@code directory/isolinux/xmlboot.config}</li>
     * <li>{@code directory/isolinux/bootlogo.dir/xmlboot.config}</li>
     * <li>{@code directory/syslinux/xmlboot.config}</li>
     * <li>{@code directory/syslinux/bootlogo.dir/xmlboot.config}</li>
     * </ul>
     * @param directory
     * @return the bootConfigFile or null if it doesn't exist
     */
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

    /**
     * Loads the timeout out of the syslinuxConfigFile using a regex to match
     * the timout value in the files.
     * @return first occurence of the timeout or -1 if it couldn't be found.
     * @throws IOException
     * @throws DBusException 
     */
    private int getTimeout() throws IOException, DBusException {
        // use syslinux configuration as reference for the timeout setting
        List<File> syslinuxConfigFiles;
        if (bootConfigPartition == null) {
            // legacy system
            syslinuxConfigFiles = getSyslinuxConfigFiles(new File(WelcomeConstants.IMAGE_DIRECTORY));
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

    /**
     * Changes the password of the user by running {@code chpasswd} with the 
     * {@link ProcessExecutor} and calling {@link #passwordEnabled() }.
     * @throws ProcessingException 
     */
    public void changePassword() throws ProcessingException {
        // Check if password should be changed
        if (password.get() == null || passwordRepeat.get() == null || password.get().isEmpty() || passwordRepeat.get().isEmpty()) {
            return;
        }
        // check, if both passwords are the same
        String password1 = password.get();
        String password2 = passwordRepeat.get();
        if (!password1.equals(password2)) {
            throw new ProcessingException("SystemconfigTask.Password_Mismatch");
        }

        // ok, passwords match, change password
        String passwordChangeScript = "#!/bin/sh\n"
                + "echo \"user:" + password1 + "\""
                + " | /usr/sbin/chpasswd\n";
        try {
            int returnValue = PROCESS_EXECUTOR.executeScript(
                    true, true, passwordChangeScript);
            if (returnValue == 0) {
                passwordEnabled();
            } else {
                throw new ProcessingException("SystemconfigTask.Password_Change_Error");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
    }

    /**
     * Enables the password by updating the empty_passwd_info and
     * @throws ProcessingException 
     */
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
        
        // set password in properties as changed
        passwordChanged = true;
        properties.setProperty(WelcomeConstants.PASSWORD_CHANGED,
                passwordChanged ? "true" : "false");

        // add polkit rules to enforce authentication
        // rules for our own applications
        addStrictPKLA("10-welcome.pkla", "enforce authentication before "
                + "running the Lernstick Welcome application",
                "ch.lernstick.welcome");
        addStrictPKLA("10-dlcopy.pkla", "enforce authentication before "
                + "running the Lernstick storage media management application",
                "ch.lernstick.dlcopy");

        // harden our custom rules for third party applications
        hardenPKLAs("jbackpack", "gnome-system-log", "packagekit", "synaptic", "udisks2");
    }

    /**
     * Adds an action with a description to the given pkla-file.<br>
     * This new rule for the PolicyKit Local Authority results into a password
     * request when trying to run this action.
     * @param fileName the pkla file in which the action should be saved
     * @param description description of the action
     * @param action the action that should be run
     */
    private void addStrictPKLA(
            String fileName, String description, String action) {
        File strictPKLA = new File(WelcomeConstants.LOCAL_POLKIT_PATH, fileName);
        String strictWelcomeRule
                = "[" + description + "]\n"
                + "Identity=unix-user:*\n"
                + "Action=" + action + "\n"
                + "ResultAny=auth_self_keep\n"
                + "ResultInactive=auth_self_keep\n"
                + "ResultActive=auth_self_keep\n";
        try (FileWriter fileWriter = new FileWriter(strictPKLA)) {
            fileWriter.write(strictWelcomeRule);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }
    }

    /**
     * Hardens the rule in an pkla-files to restrict access to the action by the
     * PolicyKit Local Authority.
     * @param pklas The actions that should be restricted.
     * @throws ProcessingException 
     */
    private void hardenPKLAs(String... pklas) throws ProcessingException {
        Pattern yesPattern = Pattern.compile("(.*)=yes");
        Path strictPoliciesDir = Paths.get("/etc/polkit-1/localauthority/55-lernstick-exam.d");
        if (!Files.isDirectory(strictPoliciesDir)) {
            try {
                Files.createDirectories(strictPoliciesDir);
            } catch (IOException ex) {
                Logger.getLogger(SystemconfigTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (String pkla : pklas) {
            try {
                Path lenientPath = Paths.get(
                        WelcomeConstants.LOCAL_POLKIT_PATH, "10-" + pkla + ".pkla");
                List<String> lenientLines = Files.readAllLines(
                		lenientPath, StandardCharsets.UTF_8);
                List<String> strictLines = new ArrayList<>();
                for (String lenientLine : lenientLines) {
                    Matcher matcher = yesPattern.matcher(lenientLine);
                    if (matcher.matches()) {
                        lenientLine = matcher.group(1) + "=auth_self_keep";
                    }
                    strictLines.add(lenientLine);
                }
            Path strictPath = strictPoliciesDir.resolve("10-" + pkla + "_strict.pkla");
           	Files.write(strictPath, strictLines, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "", ex);
                throw new ProcessingException("SystemconfigTask.cantWritePasswordPolicy", pkla);
            }
        }
    }

    /**
     * If the bootConfigPartition was mounted to make configurations on it, this
     * function has to be called to umount it after use.
     */
    public void umountBootConfig() {
        if ((bootConfigMountInfo != null) && (!bootConfigMountInfo.alreadyMounted())) {
            try {
                bootConfigPartition.umount();
            } catch (DBusException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Updates the permission of the File 
     * {@link WelcomeConstants#APPLETS_CONFIG_FILE} according to the value of
     * the {@link #blockKdeDesktopApplets} Property
     */
    public void updateBlockKdeDesktopApplets() {
        if (!blockKdeDesktopApplets.get()) {
            try {
                PosixFileAttributes attributes = Files.readAttributes(
                        WelcomeConstants.APPLETS_CONFIG_FILE, PosixFileAttributes.class
                );
                Set<PosixFilePermission> permissions = attributes.permissions();

                permissions.add(PosixFilePermission.OWNER_WRITE);

                Files.setPosixFilePermissions(WelcomeConstants.APPLETS_CONFIG_FILE, permissions);
            } catch (IOException iOException) {
                LOGGER.log(Level.WARNING, "", iOException);
            }
        } else {
            try {
                PosixFileAttributes attributes = Files.readAttributes(
                        WelcomeConstants.APPLETS_CONFIG_FILE, PosixFileAttributes.class
                );
                Set<PosixFilePermission> permissions = attributes.permissions();

                permissions.remove(PosixFilePermission.OWNER_WRITE);

                Files.setPosixFilePermissions(WelcomeConstants.APPLETS_CONFIG_FILE, permissions);
            } catch (IOException iOException) {
                LOGGER.log(Level.WARNING, "", iOException);
            }
        }
    }

    public StringProperty systemnameProperty() {
        return systemname;
    }

    public StringProperty systemversionProperty() {
        return systemversion;
    }

    public IntegerProperty timeoutSecondsProperty() {
        return timeoutSeconds;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public StringProperty passwordRepeatProperty() {
        return passwordRepeat;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public BooleanProperty blockKdeDesktopAppletsProperty() {
        return blockKdeDesktopApplets;
    }

    public BooleanProperty directSoundOutputProperty() {
        return directSoundOutput;
    }

    public BooleanProperty allowAccessToOtherFilesystemsProperty() {
        return allowAccessToOtherFilesystems;
    }
    
    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }

    /**
     * Task for {@link #newTask() }
     * @see Processable
     */
    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            // Set labels and progress
            updateProgress(0, 6);
            updateTitle("SystemconfigTask.title");
            updateMessage("SystemconfigTask.username");

            // Set Username
            if (!username.get().equals(oldUsername)) {
                LOGGER.log(Level.INFO,
                        "updating full user name to \"{0}\"", username.get());
                PROCESS_EXECUTOR.executeProcess("chfn", "-f", username.get(), "user");
            }

            updateProgress(1, 6);
            updateMessage("SystemconfigTask.bootloader");

            // Update bootloader
            if (WelcomeUtil.isImageWritable()) {
                updateBootloaders();
            }

            updateProgress(2, 6);
            updateMessage("SystemconfigTask.setup");

            // Update allow filesystem mount
            updateAllowFilesystemMount();

            updateProgress(3, 6);

            // Update direct sound
            if (Files.exists(WelcomeConstants.ALSA_PULSE_CONFIG_FILE)) {
                if (directSoundOutput.get()) {
                    // divert alsa pulse config file
                    PROCESS_EXECUTOR.executeProcess("dpkg-divert",
                            "--rename", WelcomeConstants.ALSA_PULSE_CONFIG_FILE.toString());
                }
            } else if (!directSoundOutput.get()) {
                // restore original alsa pulse config file
                PROCESS_EXECUTOR.executeProcess("dpkg-divert", "--remove",
                        "--rename", WelcomeConstants.ALSA_PULSE_CONFIG_FILE.toString());
            }

            updateProgress(4, 6);

            // Update kde applets
            updateBlockKdeDesktopApplets();

            properties.setProperty(WelcomeConstants.KDE_LOCK,
                    blockKdeDesktopApplets.get() ? "true" : "false");

            updateProgress(5, 6);
            updateMessage("SystemconfigTask.password");

            // Update password
            changePassword();

            updateProgress(6, 6);

            return null;
        }
    }

}
