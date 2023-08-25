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
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles changes to the installation of a pipx application.
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class PipxApplicationTask extends ApplicationTask {

    private static final Logger LOGGER
            = Logger.getLogger(PipxApplicationTask.class.getName());

    /**
     * Creates a PipxApplicationTask
     *
     * @param name the name of the application or a key for a resource bundle
     * @param description the description or a key for a resource bundle
     * @param icon the name of the icon (without extension and folder)
     * @param helpPath the url or local help chapter.
     * @param packages the packages that the application needs to be installed
     * @param installedNames if not null, these names will be checked to ensure
     * installation.
     */
    public PipxApplicationTask(String name, String description, String icon,
            String helpPath, ApplicationPackages packages,
            List<String> installedNames) {
        super(name, description, icon, helpPath, packages, installedNames);
    }

    @Override
    protected boolean initIsInstalled() {

        for (String packageName : installedNames) {
            LOGGER.log(Level.INFO, "checking package {0}", packageName);

            ProcessExecutor processExecutor = new ProcessExecutor(true);

            String command = PipxPackages.PIX_COMMAND
                    + " list | grep -q \"package " + packageName + " \"";

            try {
                int returnValue = processExecutor.executeScript(
                        true, true, command);
                if (returnValue == 0) {
                    return true;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "", ex);
            }
        }
        return false;
    }
}
