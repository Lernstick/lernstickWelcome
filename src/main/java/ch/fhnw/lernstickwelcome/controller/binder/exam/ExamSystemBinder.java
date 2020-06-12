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
package ch.fhnw.lernstickwelcome.controller.binder.exam;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.controller.binder.AbstractSystemBinder;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.ExamSystemController;

/**
 * Binder class to init bindings between view components and backend (model)
 * properties
 *
 * @author Line Stettler
 */
public class ExamSystemBinder extends AbstractSystemBinder {

    private final ExamSystemController examSystemController;

    /**
     * Constructor of ExamSystemBinder class
     *
     * @param welcomeController is needed to provide access to the backend
     * properties
     * @param systemController the exam FXML controller which proviedes the view
     * properties
     */
    public ExamSystemBinder(WelcomeController welcomeController,
            ExamSystemController systemController) {

        super(welcomeController, systemController);
        this.examSystemController = systemController;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * backend properties
     */
    @Override
    public void initBindings() {

        super.initBindings();

        examSystemController.getNewPasswordField().textProperty().bindBidirectional(
                welcomeController.getSystemConfigTask().passwordProperty());

        examSystemController.getRepeatPasswordField().textProperty().bindBidirectional(
                welcomeController.getSystemConfigTask().passwordRepeatProperty());

        examSystemController.getAllowInternalFileSystemsToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getSystemConfigTask().allowAccessToInternalFilesystemsProperty());

        examSystemController.getAllowExternalFileSystemsToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getSystemConfigTask().allowAccessToExternalFilesystemsProperty());

        examSystemController.getUserExchangeAccessToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getPartitionTask().accessExchangePartitionProperty());
    }
}
