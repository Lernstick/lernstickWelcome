/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;
import java.util.List;
import javafx.concurrent.Task;

/**
 * Representing multiple applications which can be installed.
 *
 * @author sschw
 */
public class ApplicationGroupTask extends Task<Boolean> {
    private List<ApplicationTask> apps;
    private ProxyTask proxy;
    
    public ApplicationGroupTask(String title, ProxyTask proxy, List<ApplicationTask> apps) {
        updateTitle(title);
        this.proxy = proxy;
        this.apps = apps;
    }

    @Override
    protected Boolean call() throws Exception {
        if(apps != null) {
            // Calculate total work
            final int totalWork = apps.stream().mapToInt(a -> a.getNoPackages()).sum();
            
            for(ApplicationTask app : apps) {
                updateMessage(app.getName());
                // update this progress on changes of sub-process
                final double previouslyDone = getWorkDone();
                app.progressProperty().addListener(cl -> updateProgress(previouslyDone + app.getWorkDone(), totalWork));
                
                app.setProxy(proxy);
                app.call();
            }
        }
        return true;
    }
}
