/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 * This class loads and saves the property file.
 * @author sschw
 */
public class PropertiesTask implements Processable<String> {
    private static final Logger LOGGER = Logger.getLogger(PropertiesTask.class.getName());
    private Properties properties;
    private File propertiesFile;
    
    /**
     * Loads the property file on creation.
     */
    public PropertiesTask() {
        properties = new Properties();
        propertiesFile = new File(WelcomeConstants.PROPERTIES_PATH);
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO,
                    "can not load properties from " + propertiesFile, ex);
        }
    }
    
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }
    
    /**
     * Task for {@link #newTask() }
     * <br>
     * Saves the property file.
     * @see Processable
     */
    private class InternalTask extends Task<String> {
        @Override
        protected String call() throws Exception {
            updateProgress(0, 1);
            updateTitle("PropertiesTask.title");
            updateMessage("PropertiesTask.message");
            try {
                properties.store(new FileOutputStream(propertiesFile),
                        "lernstick Welcome properties");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            updateProgress(1, 1);
            return null;
        }
    }
}
