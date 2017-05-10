/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationPackages;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.model.application.InstallPostprocessingTask;
import ch.fhnw.lernstickwelcome.model.application.InstallPreparationTask;
import ch.fhnw.lernstickwelcome.model.application.AptGetPackages;
import ch.fhnw.lernstickwelcome.model.application.CombinedPackages;
import ch.fhnw.lernstickwelcome.model.application.WgetPackages;
import ch.fhnw.lernstickwelcome.model.backup.BackupTask;
import ch.fhnw.lernstickwelcome.model.firewall.FirewallTask;
import ch.fhnw.lernstickwelcome.model.partition.PartitionTask;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import ch.fhnw.lernstickwelcome.model.systemconfig.SystemconfigTask;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import ch.fhnw.util.StorageTools;
import java.util.logging.Level;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * The WelcomeModelFactory creates the Model of the Welcome Application and
 * Objects which are used by different Model Classes.
 *
 * @author sschw
 */
public class WelcomeModelFactory {

    private final static ProcessExecutor PROCESS_EXECUTOR = new ProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(WelcomeModelFactory.class.getName());
    private static StorageDevice SYSTEM_STORAGE_DEVICE;

    // used to store ApplicationTasks, so there is only 1 instance of each task
    private static HashMap<String, ApplicationTask> applicationTasks = new HashMap<>();

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
     * @param tag tag to be searched for
     * @param title a title for the group task, can be anything
     * @param proxy
     * @return ApplicationGroupTask
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ApplicationGroupTask getApplicationGroupTask(String tag, String title, ProxyTask proxy) throws ParserConfigurationException, SAXException, IOException {
        List<ApplicationTask> apps = getApplicationTasks(tag);
        ApplicationGroupTask task = new ApplicationGroupTask(
                title,
                proxy,
                apps
        );
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
    public static BackupTask getBackupTask(PropertiesTask properties, String backupDirectoryName) {
        return new BackupTask(properties.getProperties(), backupDirectoryName);
    }

    /**
     * Returns a new instance of this class.
     *
     * @param isExam Some functions won't be load in Std. Version
     * @param properties Property File of the Welcome Application
     * @return {@link SystemconfigTask}
     */
    public static SystemconfigTask getSystemTask(boolean isExam, PropertiesTask properties) {
        return new SystemconfigTask(isExam, properties.getProperties());

    }

    /**
     * Returns a new instance of this class.
     *
     * @param properties Property File of the Welcome Application
     * @return {@link PartitionTask}
     */
    public static PartitionTask getPartitionTask(PropertiesTask properties) {
        return new PartitionTask(properties.getProperties());
    }

    /**
     * Returns a new instance of this class.
     *
     * @param proxy The proxy task which calculates the proxy for the commands
     * @return {@link InstallPreparationTask}
     */
    public static InstallPreparationTask getInstallPreparationTask(ProxyTask proxy) {
        return new InstallPreparationTask(proxy);
    }

    /**
     * Returns a new instance of this class.
     *
     * @param proxy The proxy task which calculates the proxy for the commands
     * @return {@link InstallPostprocessingTask}
     */
    public static InstallPostprocessingTask getInstallPostprocessingTask(ProxyTask proxy) {
        return new InstallPostprocessingTask(proxy);
    }

    /**
     * Searches the application.xml for applications with the given tag.
     *
     * @param tag
     * @return List of ApplicationTasks
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static List<ApplicationTask> getApplicationTasks(String tag) throws ParserConfigurationException, SAXException, IOException {
        ArrayList<ApplicationTask> apps = new ArrayList<>();
        File xmlFile = new File("applications.xml");
        Document xmlDoc = WelcomeUtil.parseXmlFile(xmlFile);
        NodeList applications = xmlDoc.getElementsByTagName("application");
        for (int i = 0; i < applications.getLength(); i++) {
            Node application = applications.item(i);
            if (application.getNodeType() == Node.ELEMENT_NODE) {
                Element app = (Element) application;
                NodeList tags = app.getElementsByTagName("tag");
                for (int j = 0; j < tags.getLength(); j++) {
                    Element t = ((Element) tags.item(j));
                    if (t.getTextContent().equals(tag)) {
                        apps.add(getApplicationTask(app));
                    }
                }
            }
        }
        return apps;
    }

    /**
     * Searches the application.xml for an application with the given name.
     *
     * @param name
     * @return a Task for this specific application or null if no application
     * was found.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ApplicationTask getApplicationTask(String name) throws ParserConfigurationException, SAXException, IOException {
        File xmlFile = new File("applications.xml");
        Document xmlDoc = WelcomeUtil.parseXmlFile(xmlFile);
        /*DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	Document xmlDoc = builder.parse(xmlFile);*/
        NodeList applications = xmlDoc.getElementsByTagName("application");
        for (int i = 0; i < applications.getLength(); i++) {
            Node application = applications.item(i);
            if (application.getNodeType() == Node.ELEMENT_NODE) {
                Element app = (Element) application;
                if (app.getAttribute("name").equals(name)) { // found application
                    return getApplicationTask(app);
                }
            }
        }
        return null;
    }

    /**
     * Helper function to create an ApplicationTask out of xml data.
     *
     * @param app
     * @return ApplicationTask TODO: at the moment, only 1 single fetchUrl and
     * savedir is considered and used for all wgetpackages. this should be
     * adjusted. Possible solution: make it possible in xml to add multiple
     * packages to a single fetchurl and saveDir and adjust the code below
     * accordingly.
     */
    private static ApplicationTask getApplicationTask(Element app) {
        String name = app.getAttribute("name");
        if (applicationTasks.containsKey(name)) {
            return applicationTasks.get(name);
        }
        Node l = app.getElementsByTagName("description").item(0);
        String description = app.getElementsByTagName("description").item(0).getTextContent();
        String icon = app.getElementsByTagName("icon").item(0).getTextContent();
        String helpPath = app.getElementsByTagName("help-path").item(0).getTextContent();

        NodeList installedNamesNode = app.getElementsByTagName("installed-name");
        String[] installedNames = new String[installedNamesNode.getLength()];
        for (int i = 0; i < installedNames.length; i++) {
            installedNames[i] = installedNamesNode.item(i).getTextContent();
        }
        List<String> aptgetPackages = new ArrayList<>();
        List<String> wgetPackages = new ArrayList<>();
        String wgetFetchUrl = null;
        String wgetSaveDir = null;
        NodeList packages = app.getElementsByTagName("package");
        for (int j = 0; j < packages.getLength(); j++) {
            Element pkg = ((Element) packages.item(j));
            String type = pkg.getAttribute("type");
            String pkgName = pkg.getTextContent();
            switch (type) {
                case "aptget":
                    aptgetPackages.add(pkgName);
                    break;
                case "wget":
                    wgetPackages.add(pkgName);
                    wgetFetchUrl = pkg.getAttribute("fetchUrl");
                    wgetSaveDir = pkg.getAttribute("saveDir");
                    break;
                default:
                    break;
            }
        }
        List<ApplicationPackages> params = new ArrayList<>();
        if (aptgetPackages.size() > 0) {
            params.add(new AptGetPackages(aptgetPackages.toArray(new String[aptgetPackages.size()])));
        }
        if (wgetPackages.size() > 0) {
            params.add(new WgetPackages(wgetPackages.toArray(new String[wgetPackages.size()]), wgetFetchUrl, wgetSaveDir));
        }
        CombinedPackages pkgs = new CombinedPackages(
                params.toArray(new ApplicationPackages[params.size()])
        );
        ApplicationTask task = new ApplicationTask(name, description, icon, helpPath, pkgs, installedNames);
        applicationTasks.put(name, task);
        return task;
    }
}
