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

import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import ch.fhnw.util.ProcessExecutor;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

/**
 * This class handles changes to the tasks with prepare an installation.
 * <br>
 * In order to process a backend task multiple times it extends Processable
 *
 * @see Processable
 * @author sschw
 */
public class InstallPreparationTask implements Processable<String> {

    private static final Logger LOGGER = Logger.getLogger(
            InstallPreparationTask.class.getName());
    private static final ResourceBundle BUNDLE
            = ResourceBundle.getBundle("ch.fhnw.lernstickwelcome.Bundle");

    private final ProxyTask proxy;
    private final ApplicationGroupTask[] groups;

    /**
     * Initializes the InstallPreparationTask.<br>
     * Needs ApplicationGroupTasks to ensure that there are installations to
     * install.
     *
     * @param proxy The proxy which should be used to run its tasks.
     * @param groups The application groups that will be installed.
     */
    public InstallPreparationTask(ProxyTask proxy,
            ApplicationGroupTask... groups) {

        this.proxy = proxy;
        this.groups = groups;
    }

    /**
     * Update the package list by calling {@code apt-get update}.
     *
     * @throws IOException
     */
    private void updatePackageList() throws IOException {

        ProcessExecutor processExecutor = new ProcessExecutor(true);
        int exitValue = processExecutor.executeScript(true, true,
                "apt-get" + proxy.getAptProxy() + "update");

        if (exitValue == 0) {
            // check, if there were any warnings
            List<String> stdErrList = processExecutor.getStdErrList();
            if (!stdErrList.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                int index = BUNDLE.getString("Apt_Warning_Prefix").length();
                for (int i = 0, size = stdErrList.size(); i < size; i++) {
                    String warning = stdErrList.get(i);
                    LOGGER.log(Level.INFO, "Warning: \"{0}\"", warning);
                    errorMessage.append(warning.substring(index));
                    if (i < size - 1) {
                        errorMessage.append('\n');
                    }
                }
                LOGGER.log(Level.INFO, "warning dialog message: {0}",
                        errorMessage.toString());
                FutureTask<Void> showWarningTask = new FutureTask<>(() -> {
                    Alert warning = new Alert(Alert.AlertType.WARNING,
                            errorMessage.toString(), ButtonType.CLOSE);
                    warning.setHeaderText(BUNDLE.getString(
                            "Apt_Update_Warning"));
                    warning.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    warning.showAndWait();
                    return null;
                });
                Platform.runLater(showWarningTask);
                try {
                    showWarningTask.get();
                } catch (InterruptedException | ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, "", ex);
                }
            }

        } else {
            String aptOutput = processExecutor.getOutput();
            String logMessage = "apt-get failed with the following "
                    + "output:\n" + aptOutput;
            LOGGER.severe(logMessage);
        }
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
            // Check if there are applications to install.
            int appsToInstall = 0;
            for (ApplicationGroupTask g : groups) {
                appsToInstall += g.getApps().stream().
                        filter(a -> !a.installedProperty().get()
                        && a.installingProperty().get()).count();
            }

            if (appsToInstall > 0) {
                updateTitle("InstallPreparationTask.title");
                updateMessage("InstallPreparationTask.update");
                updateProgress(0, 1);
                updatePackageList();
            }
            updateProgress(1, 1);
            return null;
        }

    }

}
