/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.firewall;

import ch.fhnw.lernstickwelcome.controller.ProcessingException;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.util.ProcessExecutor;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(FirewallTask.class.getName());
    private List<IpFilter> ipList = new ArrayList<>();
    private List<WebsiteFilter> websiteList = new ArrayList<>();
    private BooleanProperty firewallRunning = new SimpleBooleanProperty();
    
    public FirewallTask() {
        try {
            parseNetWhiteList();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }

        try {
            parseURLWhiteList();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }
        
        // start periodic firewall status check
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateFirewallState();
            }
            
        }, 0, 3000);
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
            stringBuilder.append(ip.getPortRange());
            stringBuilder.append('\n');
        }
        String ipTables = stringBuilder.toString();
        try (FileOutputStream fileOutputStream
                = new FileOutputStream(WelcomeConstants.IP_TABLES_FILENAME)) {
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
                = new FileOutputStream(WelcomeConstants.URL_WHITELIST_FILENAME)) {
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
    
    private void updateFirewallState() {
        firewallRunning.set(PROCESS_EXECUTOR.executeProcess("lernstick-firewall", "status") == 0);
    }
    
    private void parseURLWhiteList() throws IOException {
        try (FileReader fileReader = new FileReader(WelcomeConstants.URL_WHITELIST_FILENAME);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            for (String line = bufferedReader.readLine(); line != null;) {
                websiteList.add(new WebsiteFilter(line));
                line = bufferedReader.readLine();
            }
        }
    }

    private void parseNetWhiteList() throws IOException {
        FileReader fileReader = new FileReader(WelcomeConstants.IP_TABLES_FILENAME);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String lastComment = "";
        for (String line = bufferedReader.readLine(); line != null;) {
            if (line.startsWith("#")) {
                lastComment = line.substring(1).trim();
            } else {
                // try parsing "protocol target port"
                String[] tokens = line.split(" ");
                if (tokens.length == 3) {
                    IpFilter.Protocol protocol;
                    if (tokens[0].equalsIgnoreCase("TCP")) {
                        protocol = IpFilter.Protocol.TCP;
                    } else if (tokens[0].equalsIgnoreCase("UDP")) {
                        protocol = IpFilter.Protocol.UDP;
                    } else {
                        LOGGER.log(Level.WARNING,
                                "could not parse protocol \"{0}\"", tokens[0]);
                        continue;
                    }
                    String target = tokens[1];
                    String portRange = tokens[2];
                    ipList.add(new IpFilter(protocol, target, portRange, lastComment));
                } else {
                    LOGGER.log(Level.WARNING,
                            "unsupported net whitelist:\n{0}", line);
                }
            }

            line = bufferedReader.readLine();
        }
    }
}
