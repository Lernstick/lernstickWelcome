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
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * This class creates an apt-get command out of packages which should be 
 * installed.
 * <br>
 * Requires the packages which should be get from the fetchUrl. The packages 
 * temporarely saved to the saveDir.
 * @see ApplicationPackages
 * @author sschw
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
        // XXX The XML provides variable USER_HOME as string.
        // XXX We replace it here
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
            builder.append("-O " + packageName + " ");
            builder.append(MessageFormat.format(fetchUrl, packageName));
            builder.append('\n');
            builder.append("dpkg -i " + packageName + '\n');
            builder.append("rm " + packageName);
        }
        return builder.toString();
    }
}
