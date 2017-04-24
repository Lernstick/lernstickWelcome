/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class WgetPackages extends ApplicationPackages {
    private final static Logger LOGGER = Logger.getLogger(WgetPackages.class.getName());
    private String fetchUrl;
    private String saveDir;
    
    public WgetPackages(String packageName[], String fetchUrl) {
        super(packageName);
        this.fetchUrl = fetchUrl;
        this.saveDir = WelcomeConstants.USER_HOME;
    }
    
    public WgetPackages(String packageName[], String fetchUrl, String saveDir) {
        this(packageName, fetchUrl);
        if("USER_HOME".equals(saveDir))
            this.saveDir = WelcomeConstants.USER_HOME;
        else
            this.saveDir = saveDir;
    }

    @Override
    public String getInstallCommand(ProxyTask proxy) {
        StringBuilder builder = new StringBuilder();
        builder.append("cd " + saveDir);
        for(String packageName : getPackageNames()) {
            builder.append("\nwget" + proxy.getWgetProxy());
//            builder.append(String.format(fetchUrl, packageName));
            builder.append(fetchUrl + "/" + packageName);
            builder.append('\n');
            builder.append("dpkg -i " + packageName + '\n');
            builder.append("rm " + packageName);
        }
        return builder.toString();
    }
}
