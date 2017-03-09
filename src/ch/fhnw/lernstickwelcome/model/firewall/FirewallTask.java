/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.firewall;

import ch.fhnw.lernstickwelcome.controller.ProcessingException;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.util.ProcessExecutor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

/**
 *
 * @author sschw
 */
public class FirewallTask extends Task<Boolean> {
    private final static String IP_TABLES_FILENAME = ""; // TODO Filepath for config
    private final static String URL_WHITELIST_FILENAME = ""; // TODO Filepath for config
    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(FirewallTask.class.getName());
    private List<IpFilter> ipList;
    private List<WebsiteFilter> websiteList;
    private BooleanProperty firewallRunning = new SimpleBooleanProperty();
    
    public FirewallTask() {
    }

    @Override
    protected Boolean call() throws Exception {
        // TODO Add updateProgress
        // TODO Process
        return true;
    }
    
    private void updateFirewall() {
        // save IP tables
        StringBuilder stringBuilder = new StringBuilder();
        for (IpFilter ip : ipList) {
            // comment
            stringBuilder.append("# ");
            stringBuilder.append(ip.getDescription());
            stringBuilder.append('\n');
            // protocol
            stringBuilder.append(ip.getProtocol().toString());
            stringBuilder.append(' ');
            // target
            stringBuilder.append(ip.getIpAddress());
            stringBuilder.append(' ');
            // port
            stringBuilder.append(ip.getPort());
            stringBuilder.append('\n');
        }
        String ipTables = stringBuilder.toString();
        try (FileOutputStream fileOutputStream
                = new FileOutputStream(IP_TABLES_FILENAME)) {
            fileOutputStream.write(ipTables.getBytes());
            fileOutputStream.flush();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }

        // save URL whitelist
        stringBuilder = new StringBuilder();
        for(WebsiteFilter website : websiteList) {
            stringBuilder.append(website.getSearchPattern() + "\n");
        }
        String urlTables = stringBuilder.toString();
        try (FileOutputStream fileOutputStream
                = new FileOutputStream(URL_WHITELIST_FILENAME)) {
            fileOutputStream.write(urlTables.getBytes());
            fileOutputStream.flush();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
        PROCESS_EXECUTOR.executeProcess(
                "/etc/init.d/lernstick-firewall", "reload");
    }
    
    private void toggleFirewallState() throws ProcessingException {
        String action = firewallRunning.get() ? "stop" : "start";
        int ret = PROCESS_EXECUTOR.executeProcess(true, true, "lernstick-firewall", action);

        if (ret == 0) {
            firewallRunning.set(!firewallRunning.get());
            // check firewall state
            firewallRunning.set(PROCESS_EXECUTOR.executeProcess("lernstick-firewall", "status") == 0);
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
                    ? "Stop_firewall_error"
                    : "Start_firewall_error";
            throw new ProcessingException(messageId);
        }
    }
}
