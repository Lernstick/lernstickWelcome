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
package ch.fhnw.lernstickwelcome.model.firewall;

import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter.SearchPattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 *
 * @author root
 */
public class SquidAccessLogWatcher implements Runnable {

    private final String filename = WelcomeConstants.SQUID_ACCESS_LOG_FILE_PATH;
    private boolean running = false;
    private final ListProperty<WebsiteFilter> websiteList = new SimpleListProperty<>(FXCollections.observableArrayList());

    public ListProperty<WebsiteFilter> getWebsiteList() {
        return websiteList;
    }

    public void run() {
        if (running == true) {
            throw new IllegalStateException();
        }
        running = true;
        File file = new File(filename);
        long lastModified = file.lastModified();

        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            lnr.skip(Long.MAX_VALUE);
            int lineNumber = lnr.getLineNumber() + 1;
            lnr.close();

            String currentLine;
            while (running) {
                if (lastModified < file.lastModified()) {
                    lastModified = file.lastModified();

                    BufferedReader br = new BufferedReader(new FileReader(file));
                    for (int i = 0; i < lineNumber; i++) {
                        br.readLine();
                    }
                    while ((currentLine = br.readLine()) != null) {
                        parseLine(currentLine);
                        lineNumber++;
                    }
                    br.close();
                }

                Thread.sleep(500);
            }
        } catch (Exception e) {
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
        // splitt line into parameters
        String[] params = line.split(" ");
        // check for 403 Forbidden
        if (params[3].matches("TCP_DENIED/403")) {
            // add exact pattern to list
            WebsiteFilter newElement = new WebsiteFilter(SearchPattern.Exact, params[6]);
            for (WebsiteFilter currElement : websiteList) {
                if (currElement.getSearchPattern().equals(newElement.getSearchPattern())) {
                    return;
                }
            }
            Platform.runLater(() -> websiteList.add(newElement));
        }
    }
}
