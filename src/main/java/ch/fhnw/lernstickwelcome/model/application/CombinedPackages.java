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
import java.util.Arrays;

/**
 * If a package is installed by multiple aptGet and Wget packages, use this
 * Composer class to combine the commands.
 * @author sschw
 */
public class CombinedPackages extends ApplicationPackages {
    private ApplicationPackages[] packageList;
    
    public CombinedPackages(ApplicationPackages... packages) {
        super(Arrays.stream(packages).flatMap(p -> Arrays.stream(p.getPackageNames())).toArray(i -> new String[i]));
    	this.packageList = packages;
    }

    @Override
    public String getInstallCommand(ProxyTask proxy) {
        StringBuilder sb = new StringBuilder();
        for(ApplicationPackages packages : packageList) {
            sb.append(packages.getInstallCommand(proxy)).append("\n");
        }
        return sb.toString();
    }
    
}
