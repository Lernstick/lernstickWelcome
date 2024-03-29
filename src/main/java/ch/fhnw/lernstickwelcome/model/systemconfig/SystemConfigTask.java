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
package ch.fhnw.lernstickwelcome.model.systemconfig;

import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.util.MountInfo;
import ch.fhnw.util.Partition;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
 * This class handles system changes for the Lernstick.
 * <br>
 * In order to process a backend task multiple times it extends Processable
 *
 * @see Processable
 *
 * @author sschw
 */
public class SystemConfigTask implements Processable<String> {

    private static final ProcessExecutor PROCESS_EXECUTOR
            = WelcomeModelFactory.getProcessExecutor();
    private static final Logger LOGGER
            = Logger.getLogger(SystemConfigTask.class.getName());

    // Some functions are only required in the exam environment
    private boolean isExamEnv;

    private boolean showPasswordDialog;
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
    private BooleanProperty allowAccessToInternalFilesystems
            = new SimpleBooleanProperty();
    private BooleanProperty allowAccessToExternalFilesystems
            = new SimpleBooleanProperty();
    private MountInfo bootConfigMountInfo;
    private Properties properties;

    /**
     * Creates a SystemconfigTask by loading the values from the properties or
     * bootconfig.
     *
     * @param isExamEnv Some functions won't be load in Std. Version
     * @param properties Property File of the Welcome Application
     */
    public SystemConfigTask(boolean isExamEnv, Properties properties) {
        this.isExamEnv = isExamEnv;
        this.properties = properties;

        // Load properties
        if (isExamEnv) {
            showPasswordDialog = "true".equals(properties.getProperty(
                    WelcomeConstants.SHOW_PASSWORD_DIALOG, "true"));
        }
        allowAccessToInternalFilesystems.set(
                WelcomeUtil.isInternalFileSystemMountAllowed());
        allowAccessToExternalFilesystems.set(
                WelcomeUtil.isExternalFileSystemMountAllowed());
        // Load partitions
        getPartitions();
        // Load BootConfigInfos from the BootPartition
        getBootConfigInfos();
        // Load the Username
        getFullUserName();
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }

    /**
     * If the bootConfigPartition was mounted to make configurations on it, this
     * function has to be called to umount it after use.
     *
     * @throws java.io.IOException if an I/O exception occurs
     */
    public void umountBootConfig() throws IOException {
        if ((bootConfigMountInfo != null)
                && (!bootConfigMountInfo.alreadyMounted())) {
            try {
                bootConfigPartition.umount();
            } catch (DBusException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    public StringProperty systemNameProperty() {
        return systemname;
    }

    public StringProperty systemVersionProperty() {
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

    public BooleanProperty allowAccessToInternalFilesystemsProperty() {
        return allowAccessToInternalFilesystems;
    }

    public BooleanProperty allowAccessToExternalFilesystemsProperty() {
        return allowAccessToExternalFilesystems;
    }

    public boolean showPasswordDialog() {
        return showPasswordDialog;
    }
    
    /**
     * Update the access to other filesystems by editing the PKLA File.
     * <br>
     * Uses the {@link #allowAccessToOtherFilesystems} Property to check how the
     * option has to be adjusted.
     */
    private void updateAllowFilesystemMount() throws ProcessingException {

        try {
            if (allowAccessToInternalFilesystems.get()) {
                Files.delete(Paths.get(WelcomeConstants.EXAM_POLKIT_PATH,
                        "10-udisks2-mount-system_strict.pkla"));
            } else {
                hardenPKLAs("udisks2-mount-system");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }

        try {
            if (allowAccessToExternalFilesystems.get()) {
                Files.delete(Paths.get(WelcomeConstants.EXAM_POLKIT_PATH,
                        "10-udisks2-mount_strict.pkla"));
            } else {
                hardenPKLAs("udisks2-mount");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
    }

    /**
     * Loads the bootconfig info by reading out values from the xmlBootConfig
     * and the syslinuxConfigFile.
     */
    private void getBootConfigInfos() {
        // Read out the boot loader timeout in seconds
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
                Document xmlBootDocument = WelcomeUtil.parseXmlFile(
                        xmlBootConfigFile);
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
     * Read out the full username by running {@code getent passwd user} with the
     * {@link ProcessExecutor}
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
     * Loads the partitions by using
     * {@link WelcomeModelFactory#getSystemStorageDevice()}.
     * <br>
     * Loads the bootConfigPartition and the exchangePartition.
     */
    private void getPartitions() {
        systemStorageDevice = WelcomeModelFactory.getSystemStorageDevice();
        if (systemStorageDevice != null) {
            exchangePartition = systemStorageDevice.getExchangePartition();

            Partition efiPartition = systemStorageDevice.getEfiPartition();
            bootConfigPartition = efiPartition;
            /*
            if ((efiPartition != null)
                    && efiPartition.getIdLabel().equals(Partition.EFI_LABEL)) {
                // current partitioning scheme, the boot config is on the
                // *system* partition!
                bootConfigPartition = systemStorageDevice.getSystemPartition();
            } else {
                // pre 2016-02 partitioning scheme with boot config files on
                // boot partition
                bootConfigPartition = efiPartition;
            }
             */
            if (bootConfigPartition != null) {
                try {
                    bootConfigMountInfo = bootConfigPartition.mount();
                } catch (DBusException | IOException ex) {
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
     * Updates the boot menus on the EFI and exchange partition.
     * <br>
     * If boot config isn't separated, it will do the action on the running
     * system.
     *
     * @throws DBusException
     */
    private void updateBootMenus() throws DBusException, IOException {
        final int timeout = timeoutSeconds.get();
        final String systemName = systemname.get();
        final String systemVersion = systemversion.get();

        Partition.Action<Void> updateBootLoaderAction
                = new Partition.Action<Void>() {

            @Override
            public Void execute(File mountPath) {
                try {
                    updateBootMenus(mountPath, timeout,
                            systemName, systemVersion);
                } catch (DBusException ex) {
                    LOGGER.log(Level.SEVERE, "", ex);
                }
                return null;
            }
        };

        bootConfigPartition.executeMounted(updateBootLoaderAction);
        if (exchangePartition != null) {
            exchangePartition.executeMounted(updateBootLoaderAction);
        }
    }

    /**
     * Updates the boot menus on the given partition.
     *
     * @param directory root directory of the partition
     * @param timeout the timeout that has to be set
     * @param systemName the systemName that has to be set
     * @param systemVersion the systemVersion that has to be set
     * @throws DBusException
     */
    private void updateBootMenus(File directory, int timeout,
            String systemName, String systemVersion) throws DBusException {

        // syslinux
        getSyslinuxConfigFiles(directory).forEach(syslinuxConfigFile -> {
            PROCESS_EXECUTOR.executeProcess("sed", "-i", "-e",
                    "s|timeout .*|timeout " + (timeout * 10) + "|1",
                    syslinuxConfigFile.getPath());
        });

        // xmlboot
        File xmlBootConfigFile = getXmlBootConfigFile(directory);
        if (xmlBootConfigFile != null) {
            try {
                // Convert the boot config to a xml
                Document xmlBootDocument = WelcomeUtil.parseXmlFile(
                        xmlBootConfigFile);
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
     *
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
     *
     * @return the bootConfigFile or null if it doesn't exist
     * @throws DBusException
     */
    private File getXmlBootConfigFile() throws DBusException, IOException {

        if (bootConfigPartition == null) {
            // legacy system
            File configFile = getXmlBootConfigFile(new File(
                    WelcomeConstants.IMAGE_DIRECTORY));
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
     *
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
     *
     * @return first occurence of the timeout or -1 if it couldn't be found.
     * @throws IOException
     * @throws DBusException
     */
    private int getTimeout() throws IOException, DBusException {
        // use syslinux configuration as reference for the timeout setting
        List<File> syslinuxConfigFiles;
        if (bootConfigPartition == null) {
            // legacy system
            syslinuxConfigFiles = getSyslinuxConfigFiles(
                    new File(WelcomeConstants.IMAGE_DIRECTORY));
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
            List<String> configFileLines
                    = WelcomeUtil.readFile(syslinuxConfigFile);
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
     *
     * @throws ProcessingException
     */
    public void changePassword() throws ProcessingException {
        // Check if password should be changed
        if ((password.get() == null) || (passwordRepeat.get() == null)
                || password.get().isEmpty() || passwordRepeat.get().isEmpty()) {
            return;
        }
        // check, if both passwords are the same
        String password1 = password.get();
        String password2 = passwordRepeat.get();
        if (!password1.equals(password2)) {
            throw new ProcessingException("Error_Title_Password_Change",
                    "SystemconfigTask.Password_Mismatch");
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
                throw new ProcessingException("Error_Title_Password_Change",
                        "SystemconfigTask.Password_Change_Error");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
    }

    /**
     * Enables the password by updating the empty_passwd_info and
     *
     * @throws ProcessingException
     */
    private void passwordEnabled() throws ProcessingException {
        // TODO: the password hint is deprecated
        // remove this code block somewhen in the future...

        // disable password hint
        File configFile = new File(WelcomeConstants.EMPTY_PASSWORD_HINT_FILE);
        if (!configFile.exists()) {
            try (OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(configFile), Charset.defaultCharset()
            )) {
                // write kdialog config file
                osw.write("[Notification Messages]\n"
                        + "show=false");

                // fix ownership of kdialog config file:
                Path path = configFile.toPath();
                UserPrincipalLookupService lookupService
                        = FileSystems.getDefault().getUserPrincipalLookupService();
                // set user
                Files.setOwner(path,
                        lookupService.lookupPrincipalByName("user"));
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
        showPasswordDialog = false;
        properties.setProperty(WelcomeConstants.SHOW_PASSWORD_DIALOG,
                showPasswordDialog ? "true" : "false");

        // add polkit rules to enforce authentication
        // rules for our own applications
        addStrictPKLA("10-welcome_strict.pkla", "enforce authentication before "
                + "running the Lernstick Welcome application",
                "ch.lernstick.welcome");
        addStrictPKLA("10-dlcopy_strict.pkla", "enforce authentication before "
                + "running the Lernstick storage media management application",
                "ch.lernstick.dlcopy");
        addStrictPKLA("10-wrapper-synaptic_strict.pkla", "enforce "
                + "authentication before running the synaptic wrapper script",
                "ch.lernstick.wrapper-synaptic");
        addStrictPKLA("10-wrapper-gdebi-gtk_strict.pkla", "enforce "
                + "authentication before running the gdebi-gtk wrapper script",
                "ch.lernstick.wrapper-gdebi-gtk");

        // harden our custom rules for third party applications
        hardenPKLAs("jbackpack", "gnome-system-log", "packagekit");
    }

    /**
     * Adds an action with a description to the given pkla-file.<br>
     * This new rule for the PolicyKit Local Authority results into a password
     * request when trying to run this action.
     *
     * @param fileName the pkla file in which the action should be saved
     * @param description description of the action
     * @param action the action that should be run
     */
    private void addStrictPKLA(String fileName, String description,
            String action) throws ProcessingException {

        Path strictPoliciesDir = getStrictPoliciesDir();
        Path strictPath = strictPoliciesDir.resolve(fileName);
        String strictWelcomeRule
                = "[" + description + "]\n"
                + "Identity=unix-user:*\n"
                + "Action=" + action + "\n"
                + "ResultAny=auth_self_keep\n"
                + "ResultInactive=auth_self_keep\n"
                + "ResultActive=auth_self_keep\n";
        try {
            Files.write(strictPath, strictWelcomeRule.getBytes());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
            throw new ProcessingException("Error",
                    "SystemconfigTask.cantWritePasswordPolicy", fileName);
        }
    }

    /**
     * Hardens the rule in an pkla-files to restrict access to the action by the
     * PolicyKit Local Authority.
     *
     * @param pklas The actions that should be restricted.
     * @throws ProcessingException
     */
    private void hardenPKLAs(String... pklas) throws ProcessingException {
        Pattern yesPattern = Pattern.compile("(.*)=yes");
        Path strictPoliciesDir = getStrictPoliciesDir();
        for (String pkla : pklas) {
            try {
                Path lenientPath = Paths.get(WelcomeConstants.LOCAL_POLKIT_PATH,
                        "10-" + pkla + ".pkla");
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
                Path strictPath = strictPoliciesDir.resolve(
                        "10-" + pkla + "_strict.pkla");
                Files.write(strictPath, strictLines, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "", ex);
                throw new ProcessingException("Error",
                        "SystemconfigTask.cantWritePasswordPolicy", pkla);
            }
        }
    }

    private Path getStrictPoliciesDir() {
        Path strictPoliciesDir = Paths.get(WelcomeConstants.EXAM_POLKIT_PATH);
        if (!Files.isDirectory(strictPoliciesDir)) {
            try {
                Files.createDirectories(strictPoliciesDir);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return strictPoliciesDir;
    }

    /**
     * Task for {@link #newTask() }
     *
     * @see Processable
     */
    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            // Set labels and progress
            updateTitle("SystemconfigTask.title");

            // update boot menu
            updateProgress(1, 5);
            updateMessage("SystemconfigTask.bootmenu");
            if (WelcomeUtil.isImageWritable()) {
                updateBootMenus();
            }

            // update username
            updateProgress(2, 5);
            updateMessage("SystemconfigTask.username");
            if (!username.get().equals(oldUsername)) {
                LOGGER.log(Level.INFO,
                        "updating full user name to \"{0}\"", username.get());
                PROCESS_EXECUTOR.executeProcess(
                        "chfn", "-f", username.get(), "user");
            }

            // update password
            updateProgress(3, 5);
            if (isExamEnv) {
                // this should only be run in the exam environment
                updateMessage("SystemconfigTask.password");
                changePassword();
            }

            // update allow filesystem mount
            updateProgress(4, 5);
            if (isExamEnv) {
                updateMessage("SystemconfigTask.setup");
                updateAllowFilesystemMount();
            }

            // done
            updateProgress(5, 5);
            return null;
        }
    }
}
