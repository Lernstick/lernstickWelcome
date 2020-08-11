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
 *
 * @author sschw
 */
public class PropertiesTask implements Processable<String> {

    private static final Logger LOGGER
            = Logger.getLogger(PropertiesTask.class.getName());

    private Properties properties;
    private File propertiesFile;

    /**
     * Loads the property file on creation.
     */
    public PropertiesTask() {

        properties = new Properties();
        propertiesFile = new File(WelcomeConstants.PROPERTIES_PATH);

        try (FileInputStream fileInputStream
                = new FileInputStream(propertiesFile)) {

            properties.load(fileInputStream);

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
     *
     * @see Processable
     */
    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {

            updateProgress(0, 1);
            updateTitle("PropertiesTask.title");
            updateMessage("PropertiesTask.message");

            try (FileOutputStream fileOutputStream
                    = new FileOutputStream(propertiesFile)) {

                properties.store(fileOutputStream,
                        "lernstickWelcome properties");

            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "", ex);
            }

            updateProgress(1, 1);

            return null;
        }
    }
}
