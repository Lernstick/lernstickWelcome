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
            // We check if it is installed (wget exit code is inconsistent)
            if (exitValue != 0 || !initIsInstalled()) {
                String errorMessage = "apt-get or wget failed with the following "
                        + "output:\n" + PROCESS_EXECUTOR.getOutput();
                LOGGER.severe(errorMessage);
                throw new ProcessingException("ApplicationTask.installationFailed", getName());
            }
            // exit code = 0 && installed = true
            Platform.runLater(() -> {
                installed.set(true);
                installing.set(false);
            });
            updateProgress(packages.getNumberOfPackages(), packages.getNumberOfPackages());
            return null;
        }
    }

}
