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
 * Binder class to init binings between view components and backend (model) properties
 * 
 * @author sschw, Line Stettler
 */
public class BackupBinder {

    private final WelcomeController controller;
    private final BackupController backup;
    
    /**
     * Constructor of ExamBackupBinder class
     * 
     * @param controller        is needed to provide access to the backend properties
     * @param backup            FXML controller which prviedes the view properties
     */
    public BackupBinder(WelcomeController controller, BackupController backup){
        this.controller = controller;
        this.backup = backup;
    }
    
    /**
     * Method to initialize the bidirectional bindings between the view and packend properties
     */
    public void initBindings(){
        // Create bindings
        backup.getTsBackup().selectedProperty().bindBidirectional(controller.getBackup().activeProperty());
        backup.getTsScreenshot().selectedProperty().bindBidirectional(controller.getBackup().screenshotProperty());
        backup.getTsUseLocal().selectedProperty().bindBidirectional(controller.getBackup().localProperty());
        backup.getTsUseRemote().selectedProperty().bindBidirectional(controller.getBackup().partitionProperty());      
        backup.getCbMinutes().valueProperty().bindBidirectional(controller.getBackup().frequencyProperty());
        //backup.getChoice_bu_medium().getSelectionModel().selectedItemProperty().bindBidirectional(controller.getBackup().getM);
        backup.getTfDestPath().textProperty().bindBidirectional(controller.getBackup().destinationPathProperty());
        backup.getTfRemotePath().textProperty().bindBidirectional(controller.getBackup().partitionPathProperty());
        backup.getTfSrcPath().textProperty().bindBidirectional(controller.getBackup().sourcePathProperty());
    }

    /**
     * Open other view by clicking on help button
     * @param helpStage     additional window showing help
     * @param help          links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder help) {
        backup.getBtnBuHelp().setOnAction(evt -> {
            help.setHelpEntryByChapter("2");
            helpStage.show();
        });
    }
    
        
    
}
