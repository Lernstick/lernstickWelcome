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
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javafx.concurrent.Task;

/**
 * Representing multiple applications which can be installed.
 * <br>
 * In order to process a backend task multiple times it extends Processable
 *
 * @see Processable
 * @author sschw
 */
public class ApplicationGroupTask implements Processable<String> {

    private final List<ApplicationTask> apps;
    private final ProxyTask proxy;
    private final String title;

    /**
     * Initializes a group of application.
     *
     * @param title The title of the group.
     * @param proxy The proxy which should be given to the applications.
     * @param apps The applications that are represented by this group.
     */
    public ApplicationGroupTask(String title, ProxyTask proxy,
            List<ApplicationTask> apps) {

        this.title = title;
        this.proxy = proxy;
        this.apps = apps;
    }

    public List<ApplicationTask> getApps() {
        return apps;
    }

    public String getTitle() {
        return title;
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
            updateTitle(title);
            if (apps != null) {
                // Calculate total work
                final List<ApplicationTask> appsToInstall = apps.stream()
                        .filter(a -> {
                            return !a.installedProperty().get()
                                    && a.installingProperty().get();
                        })
                        .collect(Collectors.toList());
                final int totalWork = appsToInstall.stream()
                        .mapToInt(a -> a.getNumberOfPackages()).sum();
                if (totalWork != 0) {
                    updateProgress(0, totalWork);
                    int previouslyDone = 0;
                    for (ApplicationTask app : appsToInstall) {
                        try {
                            updateMessage(app.getName());
                            updateValue(WelcomeConstants.ICON_APPLICATION_FOLDER
                                    + "/" + app.getIcon());
                            Task<String> appTask = app.newTask();
                            // update this progress on changes of sub-process
                            final int fPreviouslyDone = previouslyDone;
                            appTask.progressProperty().addListener(cl -> {
                                updateProgress(fPreviouslyDone + 
                                        appTask.getWorkDone(), totalWork);
                            });
                            app.setProxy(proxy);
                            appTask.run();
                            appTask.get();
                            previouslyDone += app.getNumberOfPackages();
                        } catch (ExecutionException ex) {
                            Throwable t = ex.getCause();
                            if (t instanceof Exception) {
                                throw (Exception) t;
                            } else {
                                throw ex;
                            }
                        }
                    }
                }
            }
            updateProgress(1, 1);
            return null;
        }
    }
}