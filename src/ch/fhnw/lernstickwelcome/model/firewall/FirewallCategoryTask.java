/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.firewall;

import ch.fhnw.lernstickwelcome.model.Category;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.util.ProcessExecutor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author user
 */
public class FirewallCategoryTask extends Task<Boolean> implements Category {
    private final static String IP_TABLES_FILENAME = ""; // TODO Filepath for config
    private final static String URL_WHITELIST_FILENAME = ""; // TODO Filepath for config
    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private final static Logger LOGGER = Logger.getLogger(FirewallCategoryTask.class.getName());
    private String name;
    private List<IpFilter> ipList;
    private List<WebsiteFilter> websiteList;
    
    public FirewallCategoryTask(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected Boolean call() throws Exception {
        // FIXME Currently old process
        // TODO Add updateProgress
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
        return true;
    }
    
}
