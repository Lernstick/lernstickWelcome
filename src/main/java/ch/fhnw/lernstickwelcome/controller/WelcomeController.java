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
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.PropertiesTask;
import ch.fhnw.lernstickwelcome.model.TaskProcessor;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.InstallPostProcessingTask;
import ch.fhnw.lernstickwelcome.model.application.InstallPreparationTask;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import ch.fhnw.lernstickwelcome.model.backup.BackupTask;
import ch.fhnw.lernstickwelcome.model.firewall.FirewallTask;
import ch.fhnw.lernstickwelcome.model.help.HelpLoader;
import ch.fhnw.lernstickwelcome.model.partition.PartitionTask;
import ch.fhnw.lernstickwelcome.model.systemconfig.SystemConfigTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.application.Application;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * The controller of the Welcome Application.
 *
 * @author sschw
 */
public class WelcomeController {

    private static final Logger LOGGER
            = Logger.getLogger(WelcomeApplication.class.getName());
    private static final ResourceBundle BUNDLE
            = ResourceBundle.getBundle("ch.fhnw.lernstickwelcome.Bundle");

    private TaskProcessor taskProcessor;
    // Backend Tasks
    private PropertiesTask propertiesTask;
    // Standard Environment
    private ProxyTask proxyTask;
    private InstallPreparationTask prepareTask;
    private ApplicationGroupTask recommendedAppsTask;
    private ApplicationGroupTask utilityAppsTask;
    private ApplicationGroupTask teachingAppsTask;
    private ApplicationGroupTask miscAppsTask;
    private ApplicationGroupTask gamesAppsTask;
    private InstallPostProcessingTask postProcessingTask;
    // Exam Environment
    private FirewallTask firewallTask;
    private BackupTask backupTask;
    // General
    private SystemConfigTask systemConfigTask;
    private PartitionTask partitionTask;

    private HelpLoader helpLoader;

    private boolean isExamEnvironment;

    /**
     * Loads the data for the exam environment
     */
    public void loadExamEnvironment() {

        isExamEnvironment = true;

        configureLogger();

        helpLoader = new HelpLoader(
                Locale.getDefault().getLanguage().split("[_-]+")[0],
                isExamEnvironment);

        // init model
        // load propertiesTask at first because other tasks depend on it
        propertiesTask = WelcomeModelFactory.getPropertiesTask();

        firewallTask = WelcomeModelFactory.getFirewallTask();

        backupTask = WelcomeModelFactory.getBackupTask(propertiesTask,
                BUNDLE.getString("BackupTask.Backup_Directory"));

        systemConfigTask = WelcomeModelFactory.getSystemTask(
                true, propertiesTask);

        partitionTask = WelcomeModelFactory.getPartitionTask(propertiesTask);

        // init TaskProcessor
        List<Processable<String>> processingList = new ArrayList<>();
        processingList.add(firewallTask);
        processingList.add(backupTask);
        processingList.add(systemConfigTask);
        processingList.add(partitionTask);
        // propertiesTask must be the last task
        // (otherwise properties saving is broken)
        processingList.add(propertiesTask);
        taskProcessor = new TaskProcessor(processingList);
    }

    /**
     * Loads the data for the standard environment
     *
     * @param application the main JavaFX application
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public void loadStandardEnvironment(Application application)
            throws ParserConfigurationException, SAXException, IOException {

        isExamEnvironment = false;

        configureLogger();

        helpLoader = new HelpLoader(
                Locale.getDefault().getLanguage().split("[_-]+")[0],
                isExamEnvironment);

        // init model
        // load propertiesTask at first because other tasks depend on it
        propertiesTask = WelcomeModelFactory.getPropertiesTask();

        proxyTask = WelcomeModelFactory.getProxyTask();

        recommendedAppsTask = WelcomeModelFactory.getApplicationGroupTask(
                application, "recommended", "RecommendedApplication.title",
                proxyTask);

        utilityAppsTask = WelcomeModelFactory.getApplicationGroupTask(
                application, "utility", "UtiltyApplication.title", proxyTask);

        teachingAppsTask = WelcomeModelFactory.getApplicationGroupTask(
                application, "teaching", "TeachingApplication.title",
                proxyTask);

        miscAppsTask = WelcomeModelFactory.getApplicationGroupTask(application,
                "misc", "MiscApplication.title", proxyTask);

        gamesAppsTask = WelcomeModelFactory.getApplicationGroupTask(application,
                "game", "GameApplication.title", proxyTask);

        // load prepareTask so late because it depends on proxy and apps tasks
        prepareTask = WelcomeModelFactory.getInstallPreparationTask(
                proxyTask, recommendedAppsTask, utilityAppsTask,
                teachingAppsTask, miscAppsTask, gamesAppsTask);
        
        postProcessingTask = WelcomeModelFactory.getInstallPostprocessingTask(
                proxyTask, recommendedAppsTask, utilityAppsTask,
                teachingAppsTask, miscAppsTask, gamesAppsTask);

        partitionTask = WelcomeModelFactory.getPartitionTask(propertiesTask);

        systemConfigTask = WelcomeModelFactory.getSystemTask(
                false, propertiesTask);

        // init task processor
        List<Processable<String>> processingList = new ArrayList<>();
        processingList.add(proxyTask);
        processingList.add(prepareTask);
        processingList.add(recommendedAppsTask);
        processingList.add(utilityAppsTask);
        processingList.add(teachingAppsTask);
        processingList.add(miscAppsTask);
        processingList.add(gamesAppsTask);
        processingList.add(postProcessingTask);

        processingList.add(partitionTask);
        processingList.add(systemConfigTask);

        // propertiesTask must be the last task
        // (otherwise properties saving is broken)
        processingList.add(propertiesTask);

        taskProcessor = new TaskProcessor(processingList);
    }

    /**
     * Starts the TaskProcessor.
     */
    public void startProcessingTasks() {
        if (taskProcessor != null) {
            taskProcessor.run();
        }
    }

    /**
     * Stops backend tasks when the application should be closed.
     *
     * @throws java.io.IOException if an I/O exception occurs
     */
    public void closeApplication() throws IOException {
        if (firewallTask != null) {
            firewallTask.stopFirewallStateChecking();
        }
        if (systemConfigTask != null) {
            systemConfigTask.umountBootConfig();
        }
    }

    /**
     * Configures the Logger.
     */
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

    public ResourceBundle getBundle() {
        return BUNDLE;
    }

    public TaskProcessor getTaskProcessor() {
        return taskProcessor;
    }

    public HelpLoader getHelpLoader() {
        return helpLoader;
    }

    public ProxyTask getProxyTask() {
        return proxyTask;
    }

    public ApplicationGroupTask getRecommendedAppsTask() {
        return recommendedAppsTask;
    }

    public ApplicationGroupTask getUtilityAppsTask() {
        return utilityAppsTask;
    }

    public ApplicationGroupTask getTeachingAppsTask() {
        return teachingAppsTask;
    }

    public ApplicationGroupTask getMiscAppsTask() {
        return miscAppsTask;
    }

    public ApplicationGroupTask getGamesAppsTask() {
        return gamesAppsTask;
    }

    public PartitionTask getPartitionTask() {
        return partitionTask;
    }

    public FirewallTask getFirewallTask() {
        return firewallTask;
    }

    public BackupTask getBackupTask() {
        return backupTask;
    }

    public SystemConfigTask getSystemConfigTask() {
        return systemConfigTask;
    }

    public PropertiesTask getPropertiesTask() {
        return propertiesTask;
    }
}
