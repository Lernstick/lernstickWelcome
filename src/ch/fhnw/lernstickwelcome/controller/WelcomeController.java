/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.model.Installer;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.backup.BackupTask;
import ch.fhnw.lernstickwelcome.model.firewall.FirewallTask;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;
import ch.fhnw.lernstickwelcome.model.systemconfig.SystemconfigTask;
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
    private ProxyTask proxy;
    private ApplicationGroupTask recApps;
    private ApplicationGroupTask teachApps;
    private ApplicationGroupTask softwApps;
    private ApplicationGroupTask gamesApps;
    // Exam Environment
    private FirewallTask firewall;
    private BackupTask backup;
    // General
    private SystemconfigTask sysconf;
    
    private boolean  isExamEnvironment;
    public void loadExamEnvironment() {
        isExamEnvironment = true;
        
        List<Task> installTasks = new ArrayList<Task>();
        
        installer = new Installer(installTasks);
    }
    
    public void loadStandardEnvironment() {
        isExamEnvironment = false;
        // Init Model
        proxy = WelcomeModelFactory.getProxyTask();
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
