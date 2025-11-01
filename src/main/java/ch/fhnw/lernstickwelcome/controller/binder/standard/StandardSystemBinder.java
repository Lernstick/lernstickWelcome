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
import ch.fhnw.lernstickwelcome.controller.binder.AbstractSystemBinder;
import ch.fhnw.lernstickwelcome.fxmlcontroller.standard.StandardSystemController;

/**
 * Binder class to init bindings between view components and backend (model)
 * properties
 *
 * @author Line Stettler
 */
public class StandardSystemBinder extends AbstractSystemBinder {

    private final StandardSystemController standardSystemController;

    /**
     * Constructor of standard SystemBinder class
     *
     * @param welcomeController is needed to provide access to the backend
     * properties
     * @param systemController FXML controller which prviedes the view
     * properties
     */
    public StandardSystemBinder(WelcomeController welcomeController,
            StandardSystemController systemController) {

        super(welcomeController, systemController);
        this.standardSystemController = systemController;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * packend properties
     */
    @Override
    public void initBindings() {

        super.initBindings();

        standardSystemController.getReadOnlyWarningToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getPartitionTask().showReadOnlyInfoProperty());

        standardSystemController.getProxyToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getProxyTask().proxyActiveProperty());

        standardSystemController.getProxyHostTextField().textProperty().bindBidirectional(
                welcomeController.getProxyTask().hostnameProperty());

        standardSystemController.getProxyPortTextField().textProperty().bindBidirectional(
                welcomeController.getProxyTask().portProperty());

        standardSystemController.getProxyPasswordField().textProperty().bindBidirectional(
                welcomeController.getProxyTask().passwordProperty());

        standardSystemController.getProxyUserTextField().textProperty().bindBidirectional(
                welcomeController.getProxyTask().usernameProperty());
    }
}
