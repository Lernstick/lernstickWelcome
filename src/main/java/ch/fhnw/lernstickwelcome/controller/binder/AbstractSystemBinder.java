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

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.AbstractSystemController;
import javafx.stage.Stage;

/**
 * An abstract base class for the standard and exam system binders
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class AbstractSystemBinder {

    /**
     * the core controller
     */
    protected final WelcomeController welcomeController;

    /**
     * the standard or exam controller
     */
    protected final AbstractSystemController systemController;

    /**
     * Creates a new AbstractSystemBinder.
     *
     * @param welcomeController the core controller
     * @param systemController the standard or exam controller
     */
    public AbstractSystemBinder(WelcomeController welcomeController,
            AbstractSystemController systemController) {

        this.welcomeController = welcomeController;
        this.systemController = systemController;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * backend properties
     */
    public void initBindings() {
        systemController.getSystemNameTextField().textProperty().bindBidirectional(
                welcomeController.getSystemConfigTask().systemNameProperty());

        systemController.getSystemVersionTextField().textProperty().bindBidirectional(
                welcomeController.getSystemConfigTask().systemVersionProperty());

        systemController.getTimeoutComboBox().valueProperty().bindBidirectional(
                welcomeController.getSystemConfigTask().timeoutSecondsProperty());

        systemController.getUserNameTextField().textProperty().bindBidirectional(
                welcomeController.getSystemConfigTask().usernameProperty());

        systemController.getExchangePartitionLabelTextField().textProperty().bindBidirectional(
                welcomeController.getPartitionTask().exchangePartitionLabelProperty());

        systemController.getExchangePartitionLabelTextField().setDisable(
                !welcomeController.getPartitionTask().hasExchangePartition());

        systemController.getStartWelcomeApplicationToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getPartitionTask().startWelcomeApplicationProperty());

        systemController.getReadOnlyWarningToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getPartitionTask().showReadOnlyInfoProperty());
    }

    /**
     * Open other view by clicking on help button
     *
     * @param helpStage additional window showing help
     * @param help links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder help) {
        systemController.getHelpButton().setOnAction(evt -> {
            help.setHelpEntryByChapter("3");
            helpStage.show();
        });
    }
}
