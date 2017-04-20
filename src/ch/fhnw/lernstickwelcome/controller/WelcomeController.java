/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.model.PropertiesTask;
import ch.fhnw.lernstickwelcome.model.ResetableTask;
import ch.fhnw.lernstickwelcome.model.TaskProcessor;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.backup.BackupTask;
import ch.fhnw.lernstickwelcome.model.firewall.FirewallTask;
import ch.fhnw.lernstickwelcome.model.help.HelpLoader;
import ch.fhnw.lernstickwelcome.model.partition.PartitionTask;
import ch.fhnw.lernstickwelcome.model.proxy.ProxyTask;
import ch.fhnw.lernstickwelcome.model.systemconfig.SystemconfigTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author sschw
 */
public class WelcomeController {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("ch/fhnw/lernstickwelcome/Bundle");
    private static final Logger LOGGER = Logger.getLogger(WelcomeApplication.class.getName());
    
    private TaskProcessor taskProcessor;
    // Backend Tasks
    private PropertiesTask properties;
    // Standard Environment
    private ProxyTask proxy;
    private ApplicationGroupTask recApps;
    private ApplicationGroupTask teachApps;
    private ApplicationGroupTask softwApps;
    private ApplicationGroupTask gamesApps;
    private PartitionTask partition;
    // Exam Environment
    private FirewallTask firewall;
    private BackupTask backup;
    // General
    private SystemconfigTask sysconf;
    
    private HelpLoader help;
    
    private boolean  isExamEnvironment;
    
    public void loadExamEnvironment() {
        configureLogger();
        
        isExamEnvironment = true;
        
        help = new HelpLoader(BUNDLE.getLocale().getLanguage().split("[_-]+")[0], isExamEnvironment);
        
        properties = WelcomeModelFactory.getPropertiesTask();
        firewall = WelcomeModelFactory.getFirewallTask();
        backup = WelcomeModelFactory.getBackupTask(properties, BUNDLE.getString("Backup_Directory"));
        sysconf = WelcomeModelFactory.getSystemTask(true, properties);
        partition = WelcomeModelFactory.getPartitionTask(properties);
        
        List<ResetableTask> processingList = new ArrayList<>();
        processingList.add(firewall);
        processingList.add(backup);
        processingList.add(sysconf);
        processingList.add(partition);
        processingList.add(properties);
        taskProcessor = new TaskProcessor(processingList);
    }
    
    public void loadStandardEnvironment() {
        configureLogger();
        
        isExamEnvironment = false;

        help = new HelpLoader(BUNDLE.getLocale().getLanguage().split("[_-]+")[0], isExamEnvironment);
        // Init Model
        properties = WelcomeModelFactory.getPropertiesTask();
        proxy = WelcomeModelFactory.getProxyTask();
        recApps = WelcomeModelFactory.getRecommendedApplicationTask(proxy);
        teachApps = WelcomeModelFactory.getTeachingApplicationTask(proxy);

        sysconf = WelcomeModelFactory.getSystemTask(false, properties);

        // Init Installer
        List<ResetableTask> processingList = new ArrayList<>();
        processingList.add(proxy);
        processingList.add(recApps);

        taskProcessor = new TaskProcessor(processingList);
    }
    
    public void startProcessingTasks() {
        taskProcessor.run();
    }
    
    public void closeApplication() {
        if(isExamEnvironment) {
            firewall.stopFirewallStateChecking();
        }
        sysconf.umountBootConfig();
    }

    public void configureLogger() {
        
        // log everything...
        Logger globalLogger = Logger.getLogger("ch.fhnw");
        globalLogger.setLevel(Level.ALL);
        SimpleFormatter formatter = new SimpleFormatter();

        // log to console
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(Level.ALL);
        globalLogger.addHandler(consoleHandler);

        // log into a rotating temporaty file of max 5 MB
        try {
            FileHandler fileHandler
                    = new FileHandler("%t/lernstickWelcome", 5000000, 2, true);
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(Level.ALL);
            globalLogger.addHandler(fileHandler);
        } catch (IOException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public TaskProcessor getInstaller() {
        return taskProcessor;
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
    
    public PropertiesTask getProperties() {
        return properties;
    }

    public ResourceBundle getBundle() {
        return BUNDLE;
    }

    public HelpLoader getHelpLoader() {
        return help;
    }
    
}
