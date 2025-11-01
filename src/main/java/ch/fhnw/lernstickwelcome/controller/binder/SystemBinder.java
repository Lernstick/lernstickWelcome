/*
 * Copyright (C) 2019 Ronny Standtke <ronny.standtke@gmx.net>
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
package ch.fhnw.lernstickwelcome.controller.binder;

import javafx.stage.Stage;

/**
 * An interface for system binders.
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public interface SystemBinder {

    /**
     * Inits all the necessary bindings.
     */
    public void initBindings();

    /**
     * Inits the help system.
     *
     * @param helpStage additional JavaFX stage (window) showing help
     * @param helpBinder links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder helpBinder);
}
