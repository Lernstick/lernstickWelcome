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
import javafx.concurrent.Task;

/**
 * After Installation of application with {@link ApplicationTask}, this Task is
 * called to fix dependencies using {@code apt-get -f -y --force-yes install}.
 * 
 * @author sschw
 */
public class InstallPostprocessingTask implements Processable<String> {
    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private final ProxyTask proxy;
    
    public InstallPostprocessingTask(ProxyTask proxy) {
        this.proxy = proxy;
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }

    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            updateTitle("InstallPostprocessingTask.title");
            updateMessage("InstallPostprocessingTask.message");
            updateProgress(0, 1);
            String script = "apt-get" + proxy.getAptGetProxy() + "-f -y --force-yes install";
            PROCESS_EXECUTOR.executeScript(script);
            updateProgress(1, 1);
            return null;
        }

    }

}