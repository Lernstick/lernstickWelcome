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

import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;

/**
 * Subclasses of this class defines a strategy how packages should be installed.
 * 
 * @author sschw
 */
public abstract class ApplicationPackages {
    private String[] packageNames;
    
    public ApplicationPackages(String[] packageNames) {
        this.packageNames = packageNames;
    }
    
    /**
     * The packages which are installed by this command.
     * @return 
     */
    public String[] getPackageNames() {
        return packageNames;
    }
    
    /**
     * The number of packages which are installed by this command.
     * @return 
     */
    public int getNumberOfPackages() {
        return packageNames.length;
    }
    
    /**
     * Creates the installation command for the installation strategy.
     * @param proxy The proxy task which provides the proxy string for the 
     * command.
     * @return 
     */
    public abstract String getInstallCommand(ProxyTask proxy);
}
