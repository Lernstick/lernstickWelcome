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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class creates an apt command out of packages which should be installed.
 *
 * @see ApplicationPackages
 * @author sschw
 */
public class AptPackages extends ApplicationPackages {

    private final static Logger LOGGER
            = Logger.getLogger(AptPackages.class.getName());

    public AptPackages(List<String> packageNames) {
        super(packageNames);
    }

    @Override
    public String getInstallCommand(ProxyTask proxy) {
        getPackageNames().forEach(packageName -> {
            LOGGER.log(Level.INFO, "installing package \"{0}\"", packageName);
        });
        StringBuilder builder = new StringBuilder();
        builder.append("#!/bin/sh\n"
                + "export DEBIAN_FRONTEND=noninteractive\n");
        builder.append("apt");
        builder.append(proxy.getAptProxy());
        builder.append("-y install ");
        getPackageNames().forEach(packageName -> {
            builder.append(packageName);
            builder.append(' ');
        });
        return builder.toString();
    }
}
