/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.ResetableTask;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;
import java.util.List;
import java.util.stream.Collectors;
import javafx.concurrent.Task;

/**
 * Representing multiple applications which can be installed.
 *
 * @author sschw
 */
public class ApplicationGroupTask extends ResetableTask<Boolean> {

    private List<ApplicationTask> apps;
    private ProxyTask proxy;
    private String title;

    public ApplicationGroupTask(String title, ProxyTask proxy, List<ApplicationTask> apps) {
        this.title = title;
        this.proxy = proxy;
        this.apps = apps;
    }

    @Override
    public Task<Boolean> getTask() {
        return new InternalTask();
    }

    private class InternalTask extends Task<Boolean> {

        @Override
        protected Boolean call() throws Exception {
            updateTitle(title);
            if (apps != null) {
                // Calculate total work
                final int totalWork = apps.stream().mapToInt(a -> a.getNoPackages()).sum();

                for (ApplicationTask app : apps) {
                    updateMessage(app.getName());
                    Task<Boolean> appTask = app.getTask();
                    // update this progress on changes of sub-process
                    final double previouslyDone = getWorkDone();
                    appTask.progressProperty().addListener(cl -> updateProgress(previouslyDone + appTask.getWorkDone(), totalWork));

                    app.setProxy(proxy);
                    appTask.run();
                }
            }
            return true;
        }
    }
}
