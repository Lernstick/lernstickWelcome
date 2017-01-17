/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyCategoryTask;
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
public class ApplicationTask extends Task<Boolean> {
    private final static Logger LOGGER = Logger.getLogger(ApplicationTask.class.getName());
    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    
    private String name;
    private String description;
    private Image icon;
    private ApplicationCommand applicationCommand;
    private String[] packages;
    private String helpPath;
    private BooleanProperty installing = new SimpleBooleanProperty();
    private boolean installed;
    private ProxyCategoryTask proxy;
    
    public ApplicationTask(String name, String description, String icon, ApplicationCommand aC, String... packages) {
        this.name = name;
        this.description = description;
        this.icon = new Image(icon);
        this.applicationCommand = aC;
        this.packages = packages;
        this.installed = initIsInstalled();
    }
    
    public ApplicationTask(String name, String description, String icon, String helpPath, ApplicationCommand aC, String... installCommands) {
        this(name, description, icon, aC, installCommands);
        this.helpPath = helpPath;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Image getIcon() {
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

    public void setProxy(ProxyCategoryTask proxy) {
        this.proxy = proxy;
    }

    // abstract or strategy pattern
    @Override
    protected Boolean call() throws Exception {
        String[] commands = applicationCommand.getInstallCommands(proxy, packages);
        //XXX CHECK IF CORRECT
        PROCESS_EXECUTOR.executeProcess(commands);
        return true;
    }

    private boolean initIsInstalled() {
        int length = packages.length;
        String[] commandArray = new String[length + 2];
        commandArray[0] = "dpkg";
        commandArray[1] = "-l";
        System.arraycopy(packages, 0, commandArray, 2, length);
        PROCESS_EXECUTOR.executeProcess(true, true, commandArray);
        List<String> stdOut = PROCESS_EXECUTOR.getStdOutList();
        for (String packageName : packages) {
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
    
}
