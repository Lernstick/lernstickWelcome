/*
 * Copyright (C) 2020 Ronny Standtke <ronny.standtke@gmx.net>
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
package ch.fhnw.lernstickwelcome.model.firewall;

import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter.SearchPattern;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 * Watches the squid log file and adds denied entries to the filter list
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class SquidAccessLogWatcher implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(
            SquidAccessLogWatcher.class.getName());

    private static final String SQUID_FILE
            = WelcomeConstants.SQUID_ACCESS_LOG_FILE_PATH;

    private final ListProperty<WebsiteFilter> websiteList
            = new SimpleListProperty<>(FXCollections.observableArrayList());

    private boolean running = false;

    public ListProperty<WebsiteFilter> getWebsiteList() {
        return websiteList;
    }

    @Override
    public void run() {

        if (running == true) {
            throw new IllegalStateException();
        }
        running = true;

        File file = new File(SQUID_FILE);
        long lastLength = file.length();

        try {
            while (running) {

                TimeUnit.MILLISECONDS.sleep(500);
                long length = file.length();

                if (length < lastLength) {
                    LOGGER.info("Squid log file was reset, restarting.");
                    lastLength = length;

                } else if (length > lastLength) {
                    try (RandomAccessFile randomAccessFile
                            = new RandomAccessFile(file, "r")) {

                        randomAccessFile.seek(lastLength);
                        for (String line = randomAccessFile.readLine();
                                line != null;
                                line = randomAccessFile.readLine()) {
                            parseLine(line);
                        }
                        lastLength = randomAccessFile.getFilePointer();
                    }
                }
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Watcher crashed", ex);
        }
    }

    public void stop() {
        if (running == false) {
            throw new IllegalStateException();
        }
        running = false;
    }

    private void parseLine(String line) {
        // replace multiple spaces with one
        line = line.replaceAll(" +", " ");
        // split line into parameters
        String[] params = line.split(" ");
        // check for 403 Forbidden
        if (params[3].matches("TCP_DENIED/403")) {
            // add exact pattern to list
            WebsiteFilter newElement
                    = new WebsiteFilter(SearchPattern.Exact, params[6]);

            for (WebsiteFilter currElement : websiteList) {
                if (currElement.getSearchPattern().equals(
                        newElement.getSearchPattern())) {
                    return;
                }
            }
            Platform.runLater(() -> websiteList.add(newElement));
        }
    }
}
