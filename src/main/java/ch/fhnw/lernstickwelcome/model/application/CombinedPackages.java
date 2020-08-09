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
import java.util.stream.Collectors;

/**
 * If a package is installed by multiple apt and wget packages, use this
 * composer class to combine the commands.
 *
 * @author sschw
 */
public class CombinedPackages extends ApplicationPackages {

    private final List<ApplicationPackages> applicationPackages;

    /**
     * creates a new CombinedPackages instance
     *
     * @param applicationPackages the application packages to install
     */
    public CombinedPackages(List<ApplicationPackages> applicationPackages) {
        super(applicationPackages.stream()
                .flatMap(p -> p.getPackageNames().stream())
                .collect(Collectors.toList()));
        this.applicationPackages = applicationPackages;
    }

    @Override
    public String getInstallCommand(ProxyTask proxyTask) {

        StringBuilder stringBuilder = new StringBuilder();
        applicationPackages.forEach(p -> {
            stringBuilder.append(p.getInstallCommand(proxyTask)).append('\n');
        });

        return stringBuilder.toString();
    }
}
