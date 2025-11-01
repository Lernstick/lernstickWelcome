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
package ch.fhnw.lernstickwelcome.model;

import ch.fhnw.lernstickwelcome.SplashScreenNotification;
import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationPackages;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.model.application.AptPackages;
import ch.fhnw.lernstickwelcome.model.application.CombinedPackages;
import ch.fhnw.lernstickwelcome.model.application.DpkgApplicationTask;
import ch.fhnw.lernstickwelcome.model.application.DpkgReconfigurePackages;
import ch.fhnw.lernstickwelcome.model.application.FlatpakApplicationTask;
import ch.fhnw.lernstickwelcome.model.application.FlatpakPackages;
import ch.fhnw.lernstickwelcome.model.application.InstallPostProcessingTask;
import ch.fhnw.lernstickwelcome.model.application.InstallPreparationTask;
import ch.fhnw.lernstickwelcome.model.application.PipxApplicationTask;
import ch.fhnw.lernstickwelcome.model.application.PipxPackages;
import ch.fhnw.lernstickwelcome.model.application.WgetPackages;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import ch.fhnw.lernstickwelcome.model.backup.BackupTask;
import ch.fhnw.lernstickwelcome.model.firewall.FirewallTask;
import ch.fhnw.lernstickwelcome.model.partition.PartitionTask;
import ch.fhnw.lernstickwelcome.model.systemconfig.SystemConfigTask;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import ch.fhnw.util.StorageTools;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javax.xml.parsers.ParserConfigurationException;
import org.freedesktop.dbus.exceptions.DBusException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The WelcomeModelFactory creates the Model of the Welcome Application and
 * Objects which are used by different Model Classes.
 *
 * @author sschw
 */
public class WelcomeModelFactory {

    private static final ProcessExecutor PROCESS_EXECUTOR
            = new ProcessExecutor();
    private static final Logger LOGGER
            = Logger.getLogger(WelcomeModelFactory.class.getName());
    private static volatile StorageDevice SYSTEM_STORAGE_DEVICE;

    // used to store ApplicationTasks, so there is only 1 instance of each task
    private static final HashMap<String, ApplicationTask> applicationTasks
            = new HashMap<>();

    private static final boolean IS_FLATPAK_SUPPORTED;

    static {
        IS_FLATPAK_SUPPORTED = PROCESS_EXECUTOR.executeProcess(
                "flatpak", "--version") == 0;
    }

    /**
     * Returns the general {@link ProcessExecutor} which is used to run
     * processes.
     *
     * @return singleton instance of {@link ProcessExecutor}
     */
    public static ProcessExecutor getProcessExecutor() {
        return PROCESS_EXECUTOR;
    }

    /**
     * Returns the general {@link StorageDevice} which is used to read and write
     * data on other partitions.
     *
     * @return
     */
    public static StorageDevice getSystemStorageDevice() {
        if (SYSTEM_STORAGE_DEVICE == null) {
            try {
                SYSTEM_STORAGE_DEVICE = StorageTools.getSystemStorageDevice();
            } catch (DBusException | IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return SYSTEM_STORAGE_DEVICE;
    }

    /**
     * Creates an ApplicationGroupTask containing all the application with given
     * tag.
     *
     * @param application the main JavaFX application
     * @param tag tag to be searched for
     * @param title a title for the group task, can be anything
     * @param proxy
     * @return ApplicationGroupTask
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ApplicationGroupTask getApplicationGroupTask(
            Application application, String tag, String title, ProxyTask proxy)
            throws ParserConfigurationException, SAXException, IOException {

        List<ApplicationTask> apps = getApplicationTasks(application, tag);
        ApplicationGroupTask task
                = new ApplicationGroupTask(title, proxy, apps);
        return task;
    }

    /**
     * Returns a new instance of this class.
     *
     * @return {@link ProxyTask}
     */
    public static ProxyTask getProxyTask() {
        return new ProxyTask();
    }

    /**
     * Returns a new instance of this class.
     *
     * @return {@link PropertiesTask}
     */
    public static PropertiesTask getPropertiesTask() {
        return new PropertiesTask();
    }

    /**
     * Returns a new instance of this class.
     *
     * @return {@link FirewallTask}
     */
    public static FirewallTask getFirewallTask() {
        return new FirewallTask();
    }

    /**
     * Returns a new instance of this class.
     *
     * @param properties Property File of the Welcome Application
     * @param backupDirectoryName the name for the backup folder
     * @return {@link BackupTask}
     */
    public static BackupTask getBackupTask(
            PropertiesTask properties, String backupDirectoryName) {

        return new BackupTask(properties.getProperties(), backupDirectoryName);
    }

    /**
     * Returns a new instance of this class.
     *
     * @param isExam Some functions won't be load in Std. Version
     * @param properties Property File of the Welcome Application
     * @return {@link SystemConfigTask}
     */
    public static SystemConfigTask getSystemTask(
            boolean isExam, PropertiesTask properties) {

        return new SystemConfigTask(isExam, properties.getProperties());
    }

    /**
     * Returns a new instance of this class.
     *
     * @param propertiesTask the task that loads and saves our properties
     * @return {@link PartitionTask}
     */
    public static PartitionTask getPartitionTask(
            PropertiesTask propertiesTask) {

        return new PartitionTask(propertiesTask.getProperties());
    }

    /**
     * Returns a new instance of this class.
     *
     * @param proxy The proxy task which calculates the proxy for the commands
     * @param groups The application groups that will be installed.
     * @return {@link InstallPreparationTask}
     */
    public static InstallPreparationTask getInstallPreparationTask(
            ProxyTask proxy, ApplicationGroupTask... groups) {

        return new InstallPreparationTask(proxy, groups);
    }

    /**
     * Returns a new instance of this class.
     *
     * @param proxy The proxy task which calculates the proxy for the commands
     * @param groups The application groups that will be installed.
     * @return {@link InstallPostProcessingTask}
     */
    public static InstallPostProcessingTask getInstallPostprocessingTask(
            ProxyTask proxy, ApplicationGroupTask... groups) {

        return new InstallPostProcessingTask(proxy, groups);
    }

    /**
     * Searches the application.xml for applications with the given tag.
     *
     * @param application the main JavaFX application
     * @param tag the tag to search for
     * @return List of ApplicationTasks
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static List<ApplicationTask> getApplicationTasks(
            Application application, String tag)
            throws ParserConfigurationException, SAXException, IOException {

        ArrayList<ApplicationTask> apps = new ArrayList<>();
        InputStream is = WelcomeModelFactory.class.getResourceAsStream(
                "/applications.xml");
        Document xmlDoc = WelcomeUtil.parseXmlFile(is);

        NodeList applications = xmlDoc.getElementsByTagName("application");
        int length = applications.getLength();
        for (int i = 0; i < length; i++) {
            Node applicationNode = applications.item(i);
            if (applicationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element app = (Element) applicationNode;
                NodeList tags = app.getElementsByTagName("tag");
                for (int j = 0; j < tags.getLength(); j++) {
                    Element t = ((Element) tags.item(j));
                    if (t.getTextContent().equals(tag)) {
                        ApplicationTask task
                                = getApplicationTask(application, app, length);
                        if (task != null) {
                            apps.add(task);
                        }
                    }
                }
            }
        }
        return apps;
    }

    /**
     * Searches the application.xml for an application with the given name.
     *
     * @param application the main JavaFX application
     * @param name the name of the application to search for
     * @return a Task for this specific application or null if no application
     * was found.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ApplicationTask getApplicationTask(
            Application application, String name)
            throws ParserConfigurationException, SAXException, IOException {

        InputStream is = WelcomeModelFactory.class.getResourceAsStream(
                "/applications.xml");
        Document xmlDoc = WelcomeUtil.parseXmlFile(is);
        NodeList applications = xmlDoc.getElementsByTagName("application");
        int length = applications.getLength();
        for (int i = 0; i < length; i++) {
            Node applicationNode = applications.item(i);
            if (applicationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element app = (Element) applicationNode;
                if (app.getAttribute("name").equals(name)) {
                    // application found
                    return getApplicationTask(application, app, length);
                }
            }
        }
        return null;
    }

    /**
     * Helper function to create an ApplicationTask out of xml data.
     *
     * @param applicationElement the DOM element of the application
     * @return ApplicationTask the task that installs the application
     */
    private static ApplicationTask getApplicationTask(Application fxApplication,
            Element applicationElement, int length) {

        String applicationName = applicationElement.getAttribute("name");
        if (applicationTasks.containsKey(applicationName)) {
            return applicationTasks.get(applicationName);
        }
        String description = applicationElement.
                getElementsByTagName("description").item(0).getTextContent();
        String icon = applicationElement.
                getElementsByTagName("icon").item(0).getTextContent();

        if (fxApplication != null) {
            fxApplication.notifyPreloader(
                    new SplashScreenNotification(
                            applicationName, icon, length));
        }

        String helpPath = applicationElement.
                getElementsByTagName("help-path").item(0).getTextContent();

        List<String> installedDpkgNames = new ArrayList<>();
        NodeList installedDpkgNamesNode
                = applicationElement.getElementsByTagName(
                        "installed-dpkg-name");
        for (int i = 0; i < installedDpkgNamesNode.getLength(); i++) {
            installedDpkgNames.add(
                    installedDpkgNamesNode.item(i).getTextContent());
        }

        List<String> installedFlatpakNames = new ArrayList<>();
        NodeList installedFlatpakNamesNode
                = applicationElement.getElementsByTagName(
                        "installed-flatpak-name");
        for (int i = 0; i < installedFlatpakNamesNode.getLength(); i++) {
            installedFlatpakNames.add(
                    installedFlatpakNamesNode.item(i).getTextContent());
        }

        // We need to keep the order of installations as given in the
        // applications.xml file.
        // This is important for e.g. Adobe Acrobat Reader:
        // 1) Install its dependencies (e.g. libgtk2.0-0:i386), otherwise
        //    installing the downloaded deb will fail
        // 2) Download and install the deb.
        // 3) Install package "lernstick-adobereader-enu" (which needs Adobe
        //    Acrobat Reader to be fully installed)
        List<ApplicationPackages> packages = new ArrayList<>();
        NodeList packageNodeList
                = applicationElement.getElementsByTagName("package");
        String type = "";
        for (int i = 0; i < packageNodeList.getLength(); i++) {
            Element element = ((Element) packageNodeList.item(i));
            type = element.getAttribute("type");
            String packageNames = element.getTextContent();
            // clean up content
            packageNames = packageNames.trim();
            packageNames = packageNames.replaceAll("\n", " ");
            packageNames = packageNames.replaceAll(" +", " ");
            switch (type) {
                case "apt" ->
                    packages.add(new AptPackages(
                            Arrays.asList(packageNames.split(" "))));

                case "dpkg-reconfigure" ->
                    packages.add(new DpkgReconfigurePackages(
                            Arrays.asList(packageNames.split(" "))));

                case "wget" -> {
                    String wgetFetchUrl = element.getAttribute("fetchUrl");
                    String wgetSaveDir = element.getAttribute("saveDir");
                    packages.add(new WgetPackages(Arrays.asList(packageNames),
                            wgetFetchUrl, wgetSaveDir));
                }

                case "flatpak" ->
                    packages.add(new FlatpakPackages(
                            Arrays.asList(packageNames)));

                case "pipx" ->
                    packages.add(
                            new PipxPackages(
                                    element.getAttribute("name"),
                                    Arrays.asList(packageNames))
                    );

                default ->
                    LOGGER.log(Level.WARNING, "Unsupported type \"{0}\"", type);
            }
        }

        CombinedPackages combinedPackages = new CombinedPackages(packages);

        ApplicationTask task = null;
        switch (type) {
            case "flatpak" -> {
                // flatpak applications get ignored when on system without
                // flatpak support (e.g. the Lernstick Mini Version)
                if (IS_FLATPAK_SUPPORTED) {
                    task = new FlatpakApplicationTask(applicationName,
                            description, icon, helpPath, combinedPackages,
                            installedFlatpakNames);
                }
            }

            case "pipx" ->
                task = new PipxApplicationTask(applicationName,
                        description, icon, helpPath, combinedPackages,
                        installedFlatpakNames);

            default ->
                task = new DpkgApplicationTask(applicationName, description,
                        icon, helpPath, combinedPackages, installedDpkgNames);
        }

        if (task != null) {
            applicationTasks.put(applicationName, task);
        }

        return task;
    }
}
