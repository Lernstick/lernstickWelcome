/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.model.Installer;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.application.ApplicationCategoryTask;
import ch.fhnw.lernstickwelcome.model.backup.BackupCategoryTask;
import ch.fhnw.lernstickwelcome.model.firewall.FirewallCategoryTask;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyCategoryTask;
import ch.fhnw.lernstickwelcome.model.systemconfig.SystemconfigCategoryTask;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;

/**
 *
 * @author sschw
 */
public class WelcomeController {
    private Installer installer;
    // Standard Environment
    private ProxyCategoryTask proxy;
    private ApplicationCategoryTask recApps;
    private ApplicationCategoryTask teachApps;
    private ApplicationCategoryTask softwApps;
    private ApplicationCategoryTask gamesApps;
    // Exam Environment
    private FirewallCategoryTask firewall;
    private BackupCategoryTask backup;
    // General
    private SystemconfigCategoryTask sysconf;
    
    public void loadExamEnvironment() {
        
        
        List<Task> installTasks = new ArrayList<Task>();
        
        installer = new Installer(installTasks);
    }
    
    public void loadStandardEnvironment() {
        // Init Model
        proxy = WelcomeModelFactory.getProxyCategoryTask();
        recApps = WelcomeModelFactory.getRecommendedApplicationTask(proxy);
        
        // Init Installer
        List<Task> installTasks = new ArrayList<Task>();
        installTasks.add(proxy);
        installTasks.add(recApps);
        
        installer = new Installer(installTasks);
    }
    
    public void startInstallation() {
        installer.install();
    }
}
