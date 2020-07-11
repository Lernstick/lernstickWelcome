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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles changes to the installation of a flatpak application.
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class FlatpakApplicationTask extends ApplicationTask {

    private final static Logger LOGGER
            = Logger.getLogger(FlatpakApplicationTask.class.getName());

    /**
     * Creates a FlatpakApplicationTask
     *
     * @param name the name of the application or a key for a resource bundle
     * @param description the description or a key for a resource bundle
     * @param icon the name of the icon (without extension and folder)
     * @param helpPath the url or local help chapter.
     * @param packages the packages that the application needs to be installed
     * @param installedNames if not null, these names will be checked to ensure
     * installation.
     */
    public FlatpakApplicationTask(String name, String description, String icon,
            String helpPath, ApplicationPackages packages,
            List<String> installedNames) {
        super(name, description, icon, helpPath, packages, installedNames);
    }

    @Override
    protected boolean initIsInstalled() {

        for (String packageName : installedNames) {
            LOGGER.log(Level.INFO, "checking package {0}", packageName);

            int returnValue = PROCESS_EXECUTOR.executeProcess(
                    true, true, "flatpak", "info", packageName);
            if (returnValue == 0) {
                return true;
            }
        }
        return false;
    }
}
