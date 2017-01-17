/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.Category;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyCategoryTask;
import java.util.List;
import javafx.concurrent.Task;

/**
 * Representing multiple applications which can be installed.
 *
 * @author sschw
 */
public class ApplicationCategoryTask extends Task<Boolean> implements Category {
    private List<ApplicationTask> apps;
    private String name;
    private ProxyCategoryTask proxy;
    
    public ApplicationCategoryTask(String name, ProxyCategoryTask proxy, List<ApplicationTask> apps) {
        this.name = name;
        this.proxy = proxy;
        this.apps = apps;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    protected Boolean call() throws Exception {
        updateTitle(getTitle());
        if(apps != null) {
            int progressCount = 0;
            int maxCount = apps.size();
            for(ApplicationTask app : apps) {
                updateMessage(app.getName());
                app.setProxy(proxy);
                app.call();
                updateProgress(++progressCount, maxCount);
            }
        }
        return true;
    }
}
