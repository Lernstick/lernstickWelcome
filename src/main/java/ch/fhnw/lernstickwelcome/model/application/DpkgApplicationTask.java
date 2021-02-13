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

import ch.fhnw.util.ProcessExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This class handles changes to the installation of a dpkg application.
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class DpkgApplicationTask extends ApplicationTask {

    private static final Logger LOGGER
            = Logger.getLogger(DpkgApplicationTask.class.getName());

    /**
     * Creates a DpkgApplicationTask
     *
     * @param name the name of the application or a key for a resource bundle
     * @param description the description or a key for a resource bundle
     * @param icon the name of the icon (without extension and folder)
     * @param helpPath the url or local help chapter.
     * @param packages the packages that the application needs to be installed
     * @param installedNames if not null, these names will be checked to ensure
     * installation.
     */
    public DpkgApplicationTask(String name, String description, String icon,
            String helpPath, ApplicationPackages packages,
            List<String> installedNames) {
        super(name, description, icon, helpPath, packages, installedNames);
    }

    @Override
    protected boolean initIsInstalled() {

        List<String> dpkgListCommand = new ArrayList<>();
        dpkgListCommand.add("dpkg");
        dpkgListCommand.add("-l");
        dpkgListCommand.addAll(installedNames);

        ProcessExecutor processExecutor = new ProcessExecutor(false);
        processExecutor.executeProcess(true, true,
                dpkgListCommand.toArray(new String[dpkgListCommand.size()]));
        LOGGER.log(Level.INFO, "stdout:\n{0}", processExecutor.getStdOut());

        List<String> stdOut = processExecutor.getStdOutList();
        for (String packageName : installedNames) {
            LOGGER.log(Level.INFO, "checking package {0}", packageName);
            Pattern pattern = Pattern.compile("^ii  " + packageName + ".*");
            // all packages in installedNames need to be found
            boolean found = false;
            for (String line : stdOut) {
                if (pattern.matcher(line).matches()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                LOGGER.log(Level.INFO,
                        "package {0} not installed", packageName);
                return false;
            }
        }
        return true;
    }
}
