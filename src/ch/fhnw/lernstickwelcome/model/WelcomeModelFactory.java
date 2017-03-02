/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;
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
    
    public static ApplicationGroupTask getRecommendedApplicationTask(ProxyTask proxy) {
        ArrayList<ApplicationTask> list = new ArrayList<ApplicationTask>();
        // TODO create and add recommended applications
        ApplicationGroupTask task = new ApplicationGroupTask(
                "RecommendedSoftware", 
                proxy,
                list
        );
        return task;
    }
    
    public static ApplicationGroupTask getTeachingApplicationTask(ProxyTask proxy) {
        ArrayList<ApplicationTask> list = new ArrayList<ApplicationTask>();
        // TODO create and add teaching applications
        ApplicationGroupTask task = new ApplicationGroupTask(
                "TeachingSoftware", 
                proxy,
                list
        );
        return task;
    }
    
    public static ProxyTask getProxyTask() {
        return new ProxyTask();
    }
    // TODO add more categories which should be initialized
}
