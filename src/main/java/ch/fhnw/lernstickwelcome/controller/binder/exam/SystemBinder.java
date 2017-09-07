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
import ch.fhnw.lernstickwelcome.controller.binder.HelpBinder;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.SystemController;
import javafx.stage.Stage;

/**
 * Binder class to init binings between view components and backend (model)
 * properties
 *
 * @author Line Stettler
 */
public class SystemBinder {

    private final WelcomeController welcomeController;
    private final SystemController systemController;

    /**
     * Constructor of ExamSystemBinder class
     *
     * @param welcomeController is needed to provide access to the backend
     * properties
     * @param systemController FXML controller which proviedes the view
     * properties
     */
    public SystemBinder(WelcomeController welcomeController,
            SystemController systemController) {
        this.welcomeController = welcomeController;
        this.systemController = systemController;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * packend properties
     */
    public void initBindings() {
        systemController.getUserExchangeAccessToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getPartition().accessExchangePartitionProperty());

        systemController.getAllowFileSystemsToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getSysconf().allowAccessToOtherFilesystemsProperty());

        systemController.getBlockKdeToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getSysconf().blockKdeDesktopAppletsProperty());

        systemController.getDirectSoundToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getSysconf().directSoundOutputProperty());

        systemController.getReadOnlyWarningToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getPartition().showReadOnlyInfoProperty());

        systemController.getStartWelcomeApplicationToggleSwitch().selectedProperty().bindBidirectional(
                welcomeController.getPartition().showReadWriteWelcomeProperty());

        systemController.getTimeoutComboBox().valueProperty().bindBidirectional(
                welcomeController.getSysconf().timeoutSecondsProperty());

        systemController.getSystemNameTextField().textProperty().bindBidirectional(
                welcomeController.getSysconf().systemnameProperty());

        systemController.getSystemVersionTextField().textProperty().bindBidirectional(
                welcomeController.getSysconf().systemversionProperty());

        systemController.getExchangePartitionTextField().textProperty().bindBidirectional(
                welcomeController.getPartition().exchangePartitionLabelProperty());

        systemController.getNewPasswordField().textProperty().bindBidirectional(
                welcomeController.getSysconf().passwordProperty());

        systemController.getRepeatPasswordField().textProperty().bindBidirectional(
                welcomeController.getSysconf().passwordRepeatProperty());

        systemController.getUserNameTextField().textProperty().bindBidirectional(
                welcomeController.getSysconf().usernameProperty());

        systemController.getExchangePartitionTextField().setDisable(
                !welcomeController.getPartition().hasExchangePartition());
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
