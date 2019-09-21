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
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.BackupController;
import javafx.stage.Stage;

/**
 * Binder class to init binings between view components and backend (model)
 * properties
 *
 * @author sschw, Line Stettler
 */
public class BackupBinder {

    private final WelcomeController welcomeController;
    private final BackupController backupController;

    /**
     * Constructor of ExamBackupBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param backup FXML controller which prviedes the view properties
     */
    public BackupBinder(WelcomeController controller, BackupController backup) {
        this.welcomeController = controller;
        this.backupController = backup;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * packend properties
     */
    public void initBindings() {
        backupController.getBackupToggleSwitch().selectedProperty().
                bindBidirectional(welcomeController.getBackupTask().activeProperty());

        backupController.getScreenshotsToggleSwitch().selectedProperty().
                bindBidirectional(welcomeController.getBackupTask().screenshotProperty());

        backupController.getLocalToggleSwitch().selectedProperty().
                bindBidirectional(welcomeController.getBackupTask().localProperty());

        backupController.getExternalPartitionToggleSwitch().selectedProperty().
                bindBidirectional(welcomeController.getBackupTask().partitionProperty());

        backupController.getFrequencyCombobox().valueProperty().
                bindBidirectional(welcomeController.getBackupTask().frequencyProperty());

        backupController.getDestinationFolderTextField().textProperty().
                bindBidirectional(welcomeController.getBackupTask().destinationPathProperty());

        backupController.getExternalPartitionTextField().textProperty().
                bindBidirectional(welcomeController.getBackupTask().partitionPathProperty());

        backupController.getSourceTextField().textProperty().
                bindBidirectional(welcomeController.getBackupTask().sourcePathProperty());
    }

    /**
     * Open other view by clicking on help button
     *
     * @param helpStage additional window showing help
     * @param help links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder help) {
        backupController.getHelpButton().setOnAction(evt -> {
            help.setHelpEntryByChapter("2");
            helpStage.show();
        });
    }
}
