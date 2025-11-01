/*
 * Copyright (C) 2023 Ronny Standtke <ronny.standtke@gmx.net>
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
import java.util.List;

/**
 * This class creates a pipx command out of packages which should be installed.
 * <br>
 * Requires the packages which should be get from the fetchUrl. The packages
 * temporarely saved to the saveDir.
 *
 * @see ApplicationPackages
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class PipxPackages extends ApplicationPackages {

    /**
     * the default command for running pipx
     */
    public static final String PIX_COMMAND
            = "PIPX_HOME=/opt/pipx PIPX_BIN_DIR=/usr/local/bin pipx";

    private final String name;
    private final List<String> injectedPackages;

    /**
     * creates a new PipxPackages instance
     *
     * @param name the name of the package to install
     * @param injectedPackages the list of packages to inject
     */
    public PipxPackages(String name, List<String> injectedPackages) {
        super(Arrays.asList(name));
        this.name = name;
        this.injectedPackages = injectedPackages;
    }

    @Override
    public String getInstallCommand(ProxyTask proxy) {

        StringBuilder builder = new StringBuilder();

        // install package itself
        builder.append(PIX_COMMAND).append(" install ").append(name)
                .append('\n');

        // install all injected packages
        builder.append(PIX_COMMAND).append(" inject ").append(name);
        for (String injectedPackage : injectedPackages) {
            builder.append(' ').append(injectedPackage);
        }
        builder.append('\n');

        return builder.toString();
    }
}
