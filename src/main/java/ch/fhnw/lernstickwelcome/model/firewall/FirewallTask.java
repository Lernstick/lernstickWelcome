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

import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.util.ProcessExecutor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;

/**
 * This class handles changes to the internet access.
 * <br>
 * In order to process a backend task multiple times it extends Processable
 *
 * @see Processable
 * @author sschw
 */
public class FirewallTask implements Processable<String> {

    private static final ProcessExecutor PROCESS_EXECUTOR
            = WelcomeModelFactory.getProcessExecutor();
    private static final Logger LOGGER
            = Logger.getLogger(FirewallTask.class.getName());
    private ListProperty<HostFilter> hostFilterList
            = new SimpleListProperty<>(FXCollections.observableArrayList());
    private ListProperty<WebsiteFilter> websiteList
            = new SimpleListProperty<>(FXCollections.observableArrayList());
    private List<WebsiteFilter> savedWebsiteList = new ArrayList<>();
    private BooleanProperty firewallRunning = new SimpleBooleanProperty();
    private final Timer timer;

    /**
     * Creates a FirewallTask by loading the {@link #parseNetWhiteList()
     * Server Whitelist, {@link #parseURLWhiteList()  Website Whitelist and
     * starting a {@link Timer} to load the firewall state every 3 seconds.
     */
    public FirewallTask() {
        try {
            parseNetWhiteList();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }

        try {
            parseURLWhiteList();
            savedWebsiteList.addAll(websiteList);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }

        // start periodic firewall status check
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateFirewallState();
            }

        }, 0, 3000);
    }

    /**
     * Stops the timer in order to stop the background thread.
     */
    public void stopFirewallStateChecking() {
        timer.cancel();
    }

    /**
     * Toggles the firewall on/off.
     *
     * @throws ProcessingException thrown when the state can't be changed.
     */
    public void toggleFirewallState() throws ProcessingException {
        String action = firewallRunning.get() ? "stop" : "start";
        int ret = PROCESS_EXECUTOR.executeProcess(
                true, true, "systemctl", action, "lernstick-firewall");

        if (ret == 0) {
            firewallRunning.set(!firewallRunning.get());
            // check firewall state
            firewallRunning.set(PROCESS_EXECUTOR.executeProcess(
                    "lernstick-firewall", "status") == 0);
        } else {
            LOGGER.log(Level.WARNING,
                    action + "ing lernstick-firewall failed, return code {0} "
                    + "stdout: '{1}', stderr: '{2}'",
                    new Object[]{
                        ret,
                        PROCESS_EXECUTOR.getStdOut(),
                        PROCESS_EXECUTOR.getStdErr()
                    });
            String messageId = firewallRunning.get()
                    ? "FirewallTask.Stop_firewall_error"
                    : "FirewallTask.Start_firewall_error";
            throw new ProcessingException("Error_Title_Firewall", messageId);
        }
    }

    /**
     * This function is called by the timer to update the current state of the
     * firewall.
     */
    private void updateFirewallState() {
        boolean running = PROCESS_EXECUTOR.executeProcess(
                "lernstick-firewall", "status") == 0;
        Platform.runLater(() -> firewallRunning.set(running));
    }

    /**
     * Loads the url whitelist from the config file.
     *
     * @throws IOException
     */
    private void parseURLWhiteList() throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(
                                WelcomeConstants.URL_WHITELIST_FILENAME
                        ), Charset.defaultCharset()
                )
        )) {
            String line = bufferedReader.readLine();
            while (line != null) {
                websiteList.add(new WebsiteFilter(line));
                line = bufferedReader.readLine();
            }
        }
    }

    /**
     * Loads the ip whitelist from the config file.
     *
     * @throws IOException
     */
    private void parseNetWhiteList() throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(
                                WelcomeConstants.IP_TABLES_FILENAME
                        ), Charset.defaultCharset()
                )
        )) {
            String lastComment = "";
            for (String line = bufferedReader.readLine(); line != null;) {
                if (line.startsWith("#")) {
                    lastComment = line.substring(1).trim();
                } else {
                    // try parsing "protocol target port"
                    String[] tokens = line.split(" ");
                    if (tokens.length == 3) {
                        HostFilter.Protocol protocol;
                        if (tokens[0].equalsIgnoreCase("TCP")) {
                            protocol = HostFilter.Protocol.TCP;
                        } else if (tokens[0].equalsIgnoreCase("UDP")) {
                            protocol = HostFilter.Protocol.UDP;
                        } else {
                            LOGGER.log(Level.WARNING,
                                    "could not parse protocol \"{0}\"",
                                    tokens[0]);
                            continue;
                        }
                        String target = tokens[1];
                        String portRange = tokens[2];
                        hostFilterList.add(new HostFilter(
                                protocol, target, portRange, lastComment));
                    } else {
                        LOGGER.log(Level.WARNING,
                                "unsupported net whitelist:\n{0}", line);
                    }
                }

                line = bufferedReader.readLine();
            }
        }
    }

    /**
     * Saves the ip whitelist entries into the config file.
     */
    private void saveIpTables() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(
                        WelcomeConstants.IP_TABLES_FILENAME),
                        Charset.defaultCharset()))) {

            for (HostFilter hostFilter : hostFilterList) {

                StringBuilder stringBuilder = new StringBuilder();

                // comment
                stringBuilder.append("# ");
                stringBuilder.append(hostFilter.getDescription());
                stringBuilder.append('\n');
                // protocol
                stringBuilder.append(hostFilter.getProtocol().toString());
                stringBuilder.append(' ');
                // target
                stringBuilder.append(hostFilter.getHost());
                stringBuilder.append(' ');
                // port
                stringBuilder.append(hostFilter.getPortRange());
                stringBuilder.append('\n');

                // write line to file
                bufferedWriter.write(stringBuilder.toString());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
    }

    /**
     * Saves the url whitelist entries into the config file.
     */
    public void saveUrlWhitelist() {
        // save URL whitelist
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(
                                WelcomeConstants.URL_WHITELIST_FILENAME
                        ), Charset.defaultCharset()
                )
        )) {
            for (WebsiteFilter website : websiteList) {
                StringBuilder sb = new StringBuilder();
                sb.append(website.getSearchPattern());
                sb.append('\n');
                bw.write(sb.toString());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
    }

    public ListProperty<WebsiteFilter> getWebsiteListProperty() {
        return websiteList;
    }

    public ListProperty<HostFilter> getHostFilterListProperty() {
        return hostFilterList;
    }

    public BooleanProperty firewallRunningProperty() {
        return firewallRunning;
    }

    public boolean hasUnsavedUrls() {
        return savedWebsiteList.size() != websiteList.size()
                || !savedWebsiteList.containsAll(websiteList);
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }

    /**
     * Task for {@link #newTask() }
     *
     * @see Processable
     */
    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            updateTitle("FirewallTask.title");
            updateProgress(0, 3);
            updateMessage("FirewallTask.saveIps");

            // save IP tables
            saveIpTables();

            updateProgress(1, 3);
            updateMessage("FirewallTask.saveWebsites");

            saveUrlWhitelist();
            savedWebsiteList.clear();
            savedWebsiteList.addAll(websiteList);

            updateProgress(2, 3);
            updateMessage("FirewallTask.restartFirewall");

            if (Files.exists(Paths.get("/lib/systemd/lernstick-firewall"))) {
                PROCESS_EXECUTOR.executeProcess(
                        "/lib/systemd/lernstick-firewall", "reload");
            } else {
                // lernstick-firewall >= 3.0 needs to be managed directly via
                // systemd
                PROCESS_EXECUTOR.executeProcess(
                        "systemctl", "reload", "lernstick-firewall");
            }

            updateProgress(3, 3);
            return null;
        }
    }
}
