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
package ch.fhnw.lernstickwelcome.controller.binder.standard;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.standard.InformationController;

/**
 * Binder class to init binings between view components and backend (model)
 * properties
 *
 * @author Line Stettler
 */
public class InformationBinder {

    private final WelcomeController welcomeController;
    private final InformationController informationController;

    /**
     * Constructor of InformationBinder class
     *
     * @param welcomeController is needed to provide access to the backend
     * properties
     * @param informationController FXML controller which prviedes the view
     * properties
     */
    public InformationBinder(WelcomeController welcomeController,
            InformationController informationController) {

        this.welcomeController = welcomeController;
        this.informationController = informationController;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * backend properties (gets debian version)
     */
    public void initBindings() {

        informationController.operatingSystemProperty().bind(
                welcomeController.getSystemConfigTask().systemNameProperty());

        informationController.versionProperty().bind(
                welcomeController.getSystemConfigTask().systemVersionProperty());
    }
}
