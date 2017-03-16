/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.model.TaskProcessor;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.backup.BackupTask;
import ch.fhnw.lernstickwelcome.model.firewall.FirewallTask;
import ch.fhnw.lernstickwelcome.model.partition.PartitionTask;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;
import ch.fhnw.lernstickwelcome.model.systemconfig.SystemconfigTask;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.concurrent.Task;

/**
 *
 * @author sschw
 */
public class WelcomeController {
    private TaskProcessor installer;
    // Standard Environment
    private ProxyTask proxy;
    private ApplicationGroupTask recApps;
    private ApplicationGroupTask teachApps;
    private ApplicationGroupTask softwApps;
    private ApplicationGroupTask gamesApps;
    private SystemconfigTask system;
    private PartitionTask partition;
    // Exam Environment
    private FirewallTask firewall;
    private BackupTask backup;
    // General
    private SystemconfigTask sysconf;
    
    private ResourceBundle rb;
    private boolean  isExamEnvironment;
    
    
    
    public void loadExamEnvironment() {

        List<Task> installTasks = new ArrayList<Task>();
        
        installer = new TaskProcessor(installTasks);
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
        
        installer = new TaskProcessor(installTasks);
    }
    
    public void startInstallation() {
        installer.install();
    }

    public TaskProcessor getInstaller() {
        return installer;
    }

    public ProxyTask getProxy() {
        return proxy;
    }

    public ApplicationGroupTask getRecApps() {
        return recApps;
    }

    public ApplicationGroupTask getTeachApps() {
        return teachApps;
    }

    public ApplicationGroupTask getSoftwApps() {
        return softwApps;
    }

    public ApplicationGroupTask getGamesApps() {
        return gamesApps;
    }

    public SystemconfigTask getSystem() {
        return system;
    }

    public PartitionTask getPartition() {
        return partition;
    }

    public FirewallTask getFirewall() {
        return firewall;
    }

    public BackupTask getBackup() {
        return backup;
    }

    public SystemconfigTask getSysconf() {
        return sysconf;
    }
    
    
}
