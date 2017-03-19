/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.model.application.AptGetPackages;
import ch.fhnw.lernstickwelcome.model.application.CombinedPackages;
import ch.fhnw.lernstickwelcome.model.application.WgetPackages;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;
import ch.fhnw.lernstickwelcome.model.WelcomeUtil;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import ch.fhnw.util.StorageTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.freedesktop.dbus.exceptions.DBusException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    
    public static ApplicationTask getApplicationTask(String name) {
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
    	}
    }
}
