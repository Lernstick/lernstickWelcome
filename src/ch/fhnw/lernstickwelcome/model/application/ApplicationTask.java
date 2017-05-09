/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import ch.fhnw.util.ProcessExecutor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

/**
 * This class handles changes to the installation of a single application.
 * <br>
 * In order to process a backend task multiple times it extends Processable
 *
 * @see Processable
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
    private BooleanProperty installed = new SimpleBooleanProperty();
    private ProxyTask proxy;
    private String[] installedNames;

    /**
     * Creates a application
     * @param name the name of the application or a key for a resource bundle
     * @param description the description or a key for a resource bundle
     * @param icon the name of the icon (without extension and folder)
     * @param helpPath the url or local help chapter.
     * @param packages the packages that the application needs to be installed
     * @param installedNames if not null, these names will be checked to ensure 
     * installation.
     */
    public ApplicationTask(String name, String description, String icon, String helpPath, ApplicationPackages packages, String[] installedNames) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.packages = packages;
        this.helpPath = helpPath;
        if(installedNames != null && installedNames.length > 0)
            this.installedNames = installedNames;
        else
            this.installedNames = packages.getPackageNames();
        this.installed.set(initIsInstalled());
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

    public BooleanProperty installedProperty() {
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

    /**
     * Checks if the installation is installed.
     * @return true if {@code dpkg -l installedNames}
     */
    private boolean initIsInstalled() {
        int length = installedNames.length;
        String[] commandArray = new String[length + 2];
        commandArray[0] = "dpkg";
        commandArray[1] = "-l";

        System.arraycopy(installedNames, 0, commandArray, 2, length);
        PROCESS_EXECUTOR.executeProcess(true, true, commandArray);
        List<String> stdOut = PROCESS_EXECUTOR.getStdOutList();
        for (String packageName : installedNames) {
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

    /**
     * Task for {@link #newTask() }
     *
     * @see Processable
     */
    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            updateProgress(0, packages.getNumberOfPackages());
            // XXX May nice if there would update the percentage while execute
            int exitValue = PROCESS_EXECUTOR.executeScript(true, true, packages.getInstallCommand(proxy));
            if (exitValue != 0) {
                String errorMessage = "apt-get failed with the following "
                        + "output:\n" + PROCESS_EXECUTOR.getOutput();
                LOGGER.severe(errorMessage);
                throw new ProcessingException(errorMessage);
            }
            Platform.runLater(() -> {
                installed.set(initIsInstalled());
                installing.set(false);
            });
            updateProgress(packages.getNumberOfPackages(), packages.getNumberOfPackages());
            return null;
        }
    }

}
