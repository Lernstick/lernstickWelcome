/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import ch.fhnw.util.ProcessExecutor;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 * This class handles changes to the tasks with prepare an installation.
 * <br>
 * In order to process a backend task multiple times it extends Processable
 *
 * @see Processable
 * @author sschw
 */
public class InstallPreparationTask implements Processable<String> {

    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(InstallPreparationTask.class.getName());
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
    public InstallPreparationTask(ProxyTask proxy, ApplicationGroupTask... groups) {
        this.proxy = proxy;
        this.groups = groups;
    }

    /**
     * Stops the update-notifier because this could block the update and install
     * commands.
     */
    private void killBlockingProcesses() {
        String script = "#!/bin/sh\n"
                + "mykill() {\n"
                + "   ID=`ps -u 0 | grep \"${1}\" | awk '{ print $1 }'`\n"
                + "   if [ -n \"${ID}\" ]\n"
                + "   then\n"
                + "       kill -9 ${ID}\n"
                + "   fi\n"
                + "}\n"
                + "mykill /usr/lib/update-notifier/apt-check\n"
                + "mykill update-notifier";

        try {
            int exitValue = PROCESS_EXECUTOR.executeScript(script);
            if (exitValue != 0) {
                LOGGER.log(Level.WARNING, "Could not kill update-notifier: {0}",
                        PROCESS_EXECUTOR.getOutput());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Update the package list by calling {@code apt-get update}.
     * @throws IOException 
     */
    private void updatePackageList() throws IOException {

        String updateScript = "cd " + WelcomeConstants.USER_HOME + '\n'
                + "apt-get" + proxy.getAptGetProxy() + "update";
        int exitValue = PROCESS_EXECUTOR.executeScript(
                true, true, updateScript);
        if (exitValue != 0) {
            String aptGetOutput = PROCESS_EXECUTOR.getOutput();
            String logMessage = "apt-get failed with the following "
                    + "output:\n" + aptGetOutput;
            LOGGER.severe(logMessage);
            // TODO Find good solution for this 
            // We dont want to throw an exception because he couldn't update
            // throw new ProcessingException("InstallPreparationTask.aptGetFailed", aptGetOutput); 
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
            for(ApplicationGroupTask g : groups) 
                appsToInstall += g.getApps().stream().
                        filter(a -> !a.installedProperty().get() && a.installingProperty().get()).count();
            
            if(appsToInstall > 0) {
                updateTitle("InstallPreparationTask.title");
                updateMessage("InstallPreparationTask.prepareUpdate");
                updateProgress(0, 2);
                // make sure that update-notifier does not get into our way
                killBlockingProcesses();

                updateProgress(1, 2);
                updateMessage("InstallPreparationTask.update");

                updatePackageList();
            }
            updateProgress(2, 2);
            return null;
        }

    }

}
