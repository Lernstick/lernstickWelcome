/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application.proxy;

import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.util.ProcessExecutor;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author sschw
 */
public class InstallPreparationTask implements Processable<String> {
    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(InstallPreparationTask.class.getName());
    private final ProxyTask proxy;
    
    public InstallPreparationTask(ProxyTask proxy) {
        this.proxy = proxy;
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }

    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            updateTitle("ApplicationInstallPreparationTask.title");
            updateMessage("ApplicationInstallPreparationTask.prepareUpdate");
            updateProgress(0, 2);
            // make sure that update-notifier does not get into our way
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
            updateProgress(1, 2);
            updateMessage("ApplicationInstallPreparationTask.update");
            String updateScript = "cd " + WelcomeConstants.USER_HOME + '\n'
                    + "apt-get" + proxy.getAptGetProxy() + "update";
            int exitValue = PROCESS_EXECUTOR.executeScript(
                    true, true, updateScript);
            if (exitValue != 0) {
                String aptGetOutput = PROCESS_EXECUTOR.getOutput();
                String logMessage = "apt-get failed with the following "
                        + "output:\n" + aptGetOutput;
                LOGGER.severe(logMessage);
//                throw new ProcessingException("InstallPreparationTask.aptGetFailed", aptGetOutput); // TODO Find good solution for this
            }
            updateProgress(2, 2);
            return null;
        }

    }

}
