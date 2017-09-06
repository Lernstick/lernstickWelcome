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

/**
 * This class creates an apt-get command out of packages which should be
 * installed.
 * <br>
 * Requires the packages which should be get from the fetchUrl. The packages
 * temporarely saved to the saveDir.
 *
 * @see ApplicationPackages
 * @author sschw
 */
public class WgetPackages extends ApplicationPackages {

    private String fetchUrl;
    private String saveDir;

    /**
     * creates a new WgetPackages instance
     *
     * @param packageNames the names of the packages to install
     * @param fetchUrl the URL where to fetch the Debian package from
     */
    public WgetPackages(String[] packageNames, String fetchUrl) {

        super(packageNames);
        this.fetchUrl = fetchUrl;
        this.saveDir = WelcomeConstants.USER_HOME;
    }

    /**
     * creates a new WgetPackages instance
     *
     * @param packageNames the names of the packages to install
     * @param fetchUrl the URL where to fetch the Debian package from
     * @param saveDir the directory where to temporarily save the downloaded
     * package
     */
    public WgetPackages(String[] packageNames,
            String fetchUrl, String saveDir) {

        this(packageNames, fetchUrl);
        // The XML provides variable USER_HOME as string.
        // We replace it here.
        if ("USER_HOME".equals(saveDir)) {
            this.saveDir = WelcomeConstants.USER_HOME;
        } else {
            this.saveDir = saveDir;
        }
    }

    @Override
    public String getInstallCommand(ProxyTask proxy) {

        StringBuilder builder = new StringBuilder();

        builder.append("cd ").append(saveDir).append('\n');
        for (String packageName : getPackageNames()) {
            builder.append("wget").append(proxy.getWgetProxy());
            builder.append("-O ").append(packageName).append(' ');
            builder.append(MessageFormat.format(fetchUrl, packageName));
            builder.append('\n');
            builder.append("dpkg -i ").append(packageName).append('\n');
            // the next call is necessary to automatically install all missing
            // dependencies of the downloaded package (needed by e.g. Google
            // Earth Pro that depends on the package "lsb-core")
            builder.append("apt-get -f install\n");
            builder.append("rm ").append(packageName).append('\n');
        }

        return builder.toString();
    }
}
