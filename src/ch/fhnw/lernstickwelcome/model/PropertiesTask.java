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
 *
 * @author sschw
 */
public class PropertiesTask extends Task<Boolean> {
    private static final Logger LOGGER = Logger.getLogger(PropertiesTask.class.getName());
    private Properties properties;
    private File propertiesFile;
    
    public PropertiesTask() {
        properties = new Properties();
        propertiesFile = new File("/etc/lernstickWelcome");
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
    protected Boolean call() throws Exception {
        try {
            properties.store(new FileOutputStream(propertiesFile),
                    "lernstick Welcome properties");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return true;
    }
}
