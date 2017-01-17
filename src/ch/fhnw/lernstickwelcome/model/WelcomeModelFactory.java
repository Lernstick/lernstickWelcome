/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationCategoryTask;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyCategoryTask;
import ch.fhnw.util.ProcessExecutor;
import java.util.ArrayList;

/**
 * XXX Change this class if applications should be load from file
 * 
 * @author sschw
 */
public class WelcomeModelFactory {
    private final static ProcessExecutor PROCESS_EXECUTOR = new ProcessExecutor();
    
    public static ProcessExecutor getProcessExecutor() {
        return PROCESS_EXECUTOR;
    }
    
    public static ApplicationCategoryTask getRecommendedApplicationTask(ProxyCategoryTask proxy) {
        ArrayList<ApplicationTask> list = new ArrayList<ApplicationTask>();
        // TODO create and add recommended applications
        ApplicationCategoryTask task = new ApplicationCategoryTask(
                "RecommendedSoftware", 
                proxy,
                list
        );
        return task;
    }
    
    public static ApplicationCategoryTask getTeachingApplicationTask(ProxyCategoryTask proxy) {
        ArrayList<ApplicationTask> list = new ArrayList<ApplicationTask>();
        // TODO create and add teaching applications
        ApplicationCategoryTask task = new ApplicationCategoryTask(
                "TeachingSoftware", 
                proxy,
                list
        );
        return task;
    }
    
    public static ProxyCategoryTask getProxyCategoryTask() {
        return new ProxyCategoryTask("ProxySettings");
    }
    // TODO add more categories which should be initialized
}
