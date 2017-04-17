/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.model.backup.BackupTask;
import ch.fhnw.lernstickwelcome.model.firewall.FirewallTask;
import ch.fhnw.lernstickwelcome.model.partition.PartitionTask;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;
import ch.fhnw.lernstickwelcome.model.systemconfig.SystemconfigTask;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import ch.fhnw.util.StorageTools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * XXX Change this class if applications should be load from file
 * 
 * @author sschw
 */
public class WelcomeModelFactory {
    private final static ProcessExecutor PROCESS_EXECUTOR = new ProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(WelcomeModelFactory.class.getName());
    private static StorageDevice SYSTEM_STORAGE_DEVICE;
    
    public static ProcessExecutor getProcessExecutor() {
        return PROCESS_EXECUTOR;
    }
    
    public static StorageDevice getSystemStorageDevice() {
        if(SYSTEM_STORAGE_DEVICE == null) {
            try {
                SYSTEM_STORAGE_DEVICE = StorageTools.getSystemStorageDevice();
            } catch (DBusException | IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return SYSTEM_STORAGE_DEVICE;
    }
    
    public static ApplicationGroupTask getRecommendedApplicationTask(ProxyTask proxy) {
        ArrayList<ApplicationTask> list = new ArrayList<ApplicationTask>();
        // TODO create and add recommended applications
        ApplicationGroupTask task = new ApplicationGroupTask(
                "RecommendedSoftware", 
                proxy,
                list
        );
        return task;
    }
    
    public static ApplicationGroupTask getTeachingApplicationTask(ProxyTask proxy) {
        ArrayList<ApplicationTask> list = new ArrayList<ApplicationTask>();
        // TODO create and add teaching applications
        ApplicationGroupTask task = new ApplicationGroupTask(
                "TeachingSoftware", 
                proxy,
                list
        );
        return task;
    }
    
    public static ProxyTask getProxyTask() {
        return new ProxyTask();
    }
    // TODO add more categories which should be initialized
    
    /**
     * Searches the application.xml for an application with the given name.
     * @param name
     * @return a Task for this specific application or null if no application was found.
     */
    public static ApplicationTask getApplicationTask(String name) {/*
    	File xmlFile = new File("applications.xml");
    	Document xmlDoc = WelcomeUtil.parseXmlFile(xmlFile);
    	NodeList applications = xmlDoc.getElementsByTagName("application");
    	for (int i = 0; i < applications.getLength(); i++) {
    		Node application = applications.item(i);
    		if (application.getNodeType() == Node.ELEMENT_NODE) {
    			Element app = (Element) application;
    			if (app.getAttribute("name") == name) { // found application
        			String description = app.getElementsByTagName("description").item(0).getNodeValue();
        			String icon = app.getElementsByTagName("icon").item(0).getNodeValue();
        			String helpPath = app.getElementsByTagName("help-path").item(0).getNodeValue();
        			List<String> aptgetPackages = new ArrayList<>();
        			List<String> wgetPackages = new ArrayList<>();
        			// XXX does really every wget package have the same fetchUrl and SaveDir?
        			// In application.xml I defined it so every package can have different ones.
        			// In class WgetPackages however, there is only one property for all packages.
        			// So for now this function uses just the last ones of the properties.
        			String wgetFetchUrl; 
        			String wgetSaveDir;
        			NodeList packages = app.getElementsByTagName("package");
        			for (int j = 0; j < packages.getLength(); j++) {
        				Element pkg = ((Element)packages.item(j));
        				String type = pkg.getAttribute("type");
        				String pkgName = pkg.getNodeValue();
        				switch (type) {
						case "aptget":
							aptgetPackages.add(pkgName);
							break;
						case "wget":
							wgetPackages.add(pkgName);
	        				wgetFetchUrl = pkg.getAttribute("fetchUrl");
	        				wgetSaveDir = pkg.getAttribute("saveDir");
							break;
						default: break;
						}
        			}
        			CombinedPackages pkgs = new CombinedPackages(
    					new AptGetPackages(aptgetPackages.toArray(new String[aptgetPackages.size()])), 
    					new WgetPackages(wgetPackages.toArray(new String[wgetPackages.size()]), wgetFetchUrl, wgetSaveDir)
        			);
        			ApplicationTask task = new ApplicationTask(name, description, icon, helpPath, pkgs);
        			return task;
    			}
    		}
    	}*/
    	return null;
    }

    public static PropertiesTask getPropertiesTask() {
        return new PropertiesTask();
    }

    public static FirewallTask getFirewallTask() {
        return new FirewallTask();
    }

    public static BackupTask getBackupTask(PropertiesTask properties, String backupDirectoryName) {
        return new BackupTask(properties.getProperties(), backupDirectoryName);
    }

    public static SystemconfigTask getSystemTask(boolean isExam, PropertiesTask properties) {
        return new SystemconfigTask(isExam, properties.getProperties());

    }

    public static PartitionTask getPartitionTask(PropertiesTask properties) {
        return new PartitionTask(properties.getProperties());
    }
}
