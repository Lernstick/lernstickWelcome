/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationBackupController;

/**
 *
 * @author user
 */
public class ExamBackupController {
    public ExamBackupController(WelcomeController controller, WelcomeApplicationBackupController backup){
        addBindings(controller, backup);
    }
    
    private void addBindings(WelcomeController controller, WelcomeApplicationBackupController backup){
        // Create bindings
        backup.getCb_bu_backup().selectedProperty().bindBidirectional(controller.getBackup().getActive());
        backup.getCb_bu_screenshot().selectedProperty().bindBidirectional(controller.getBackup().getScreenshot());
        backup.getCb_bu_use_local().selectedProperty().bindBidirectional(controller.getBackup().getLocal());
        backup.getCb_bu_use_remote().selectedProperty().bindBidirectional(controller.getBackup().getPartition());      
        backup.getChoice_bu_backup().valueProperty().bindBidirectional(controller.getBackup().getFrequency());
        //backup.getChoice_bu_medium().getSelectionModel().selectedItemProperty().bindBidirectional(controller.getBackup().getM);
        backup.getTxt_bu_dest_path().textProperty().bindBidirectional(controller.getBackup().getDestinationPath());
        backup.getTxt_bu_remote_path().textProperty().bindBidirectional(controller.getBackup().getPartitionPath());
        backup.getTxt_bu_src_path().textProperty().bindBidirectional(controller.getBackup().getSourcePath());
    }
    
        
    
}
