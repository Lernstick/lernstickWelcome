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
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import ch.fhnw.util.ProcessExecutor;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
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
public abstract class ApplicationTask implements Processable<String> {

    private static final Logger LOGGER
            = Logger.getLogger(ApplicationTask.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "ch.fhnw.lernstickwelcome.Bundle");

    protected final List<String> installedNames;

    private final String name;
    private final String description;
    private final String icon;
    private final ApplicationPackages packages;
    private final String helpPath;
    private final BooleanProperty installing = new SimpleBooleanProperty();
    private final BooleanProperty installed = new SimpleBooleanProperty();
    private ProxyTask proxy;

    /**
     * Creates an ApplicationTask
     *
     * @param name the name of the application or a key for a resource bundle
     * @param description the description or a key for a resource bundle
     * @param icon the name of the icon (without extension and folder)
     * @param helpPath the url or local help chapter.
     * @param packages the packages that the application needs to be installed
     * @param installedNames if not null, these names will be checked to ensure
     * installation.
     */
    public ApplicationTask(String name, String description, String icon,
            String helpPath, ApplicationPackages packages,
            List<String> installedNames) {

        this.name = name;
        this.description = description;
        this.icon = icon;
        this.packages = packages;
        this.helpPath = helpPath;
        if (installedNames == null || installedNames.isEmpty()) {
            this.installedNames = packages.getPackageNames();
        } else {
            this.installedNames = installedNames;
        }
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

    public int getNumberOfPackages() {
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
     *
     * @return true if {@code dpkg -l installedNames}
     */
    protected abstract boolean initIsInstalled();

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

            ProcessExecutor processExecutor = new ProcessExecutor(true);
            int exitValue = processExecutor.executeScript(true, true,
                    packages.getInstallCommand(proxy));
            // We check if it is installed (wget exit code is inconsistent)
            if (exitValue != 0 || !initIsInstalled()) {

                // check if we failed to get the dpkg frontend lock
                boolean failedFrontEndLock = false;
                for (String error : processExecutor.getStdErrList()) {
                    if (error.startsWith("E: Could not get lock")) {
                        failedFrontEndLock = true;
                    }
                }

                if (failedFrontEndLock) {
                    throw new ProcessingException(
                            "Error_Title_Application_Installation",
                            "Error_Getting_Dpkg_Frontend_Lock");

                } else {
                    String errorMessage
                            = "apt or wget failed with the following output:\n"
                            + processExecutor.getOutput();
                    LOGGER.severe(errorMessage);
                    String name = getName();
                    try {
                        name = BUNDLE.getString(name);
                    } catch (MissingResourceException e) {
                        // this is OK, only some applications are localized,
                        // like "Additional multimedia formats" or
                        // "Additional fonts"
                    }
                    throw new ProcessingException(
                            "Error_Title_Application_Installation",
                            "ApplicationTask.installationFailed", name);
                }
            }
            // exit code = 0 && installed = true
            Platform.runLater(() -> {
                installed.set(true);
                installing.set(false);
            });
            updateProgress(packages.getNumberOfPackages(),
                    packages.getNumberOfPackages());
            return null;
        }
    }
}
