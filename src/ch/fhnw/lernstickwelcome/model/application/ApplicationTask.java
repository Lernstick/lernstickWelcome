/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import ch.fhnw.util.ProcessExecutor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

/**
 *
 * @author sschw
 */
public class ApplicationTask implements Processable<String> {
    private final static Logger LOGGER = Logger.getLogger(ApplicationTask.class.getName());
    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    
    private String name;
    private String description;
    private String icon;
    private ApplicationPackages packages;
    private String helpPath;
    private BooleanProperty installing = new SimpleBooleanProperty();
    private boolean installed;
    private ProxyTask proxy;
    
    public ApplicationTask(String name, String description, String icon, ApplicationPackages packages) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.packages = packages;
        this.installed = initIsInstalled();
    }
    
    public ApplicationTask(String name, String description, String icon, String helpPath, ApplicationPackages packages) {
        this(name, description, icon, packages);
        this.helpPath = helpPath;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getHelpPath() {
        return helpPath;
    }
    
    public BooleanProperty installingProperty() {
        return installing;
    }
    
    public boolean isInstalled() {
        return installed;
    }
    
    public int getNoPackages() {
        return packages.getNumberOfPackages();
    }
    
    /* only used for testcases */
    public ApplicationPackages getPackages() {
        return packages;
    }

    public void setProxy(ProxyTask proxy) {
        this.proxy = proxy;
    }

    private boolean initIsInstalled() {
        int length = packages.getNumberOfPackages();
        String[] commandArray = new String[length + 2];
        commandArray[0] = "dpkg";
        commandArray[1] = "-l";
        
        System.arraycopy(packages.getPackageNames(), 0, commandArray, 2, length);
        PROCESS_EXECUTOR.executeProcess(true, true, commandArray);
        List<String> stdOut = PROCESS_EXECUTOR.getStdOutList();
        for (String packageName : packages.getPackageNames()) {
            LOGGER.log(Level.INFO, "checking package {0}", packageName);
            Pattern pattern = Pattern.compile("^ii  " + packageName + ".*");
            boolean found = false;
            for (String line : stdOut) {
                if (pattern.matcher(line).matches()) {
                    LOGGER.info("match");
                    found = true;
                    break;
                } else {
                    LOGGER.info("no match");
                }
            }
            if (!found) {
                LOGGER.log(Level.INFO,
                        "package {0} not installed", packageName);
                return false;
            }
        }
        return true;
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }

    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            updateProgress(0, packages.getNumberOfPackages());
            //XXX May nice if there would update the percentage while execute
            PROCESS_EXECUTOR.executeProcess(packages.getInstallCommand(proxy));
            updateProgress(packages.getNumberOfPackages(), packages.getNumberOfPackages());
            return null;
        }
    }

}
