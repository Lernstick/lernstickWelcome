/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationBackupController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationHelpController;
import javafx.stage.Stage;

/**
 *
 * @author user
 */
public class ExamBackupBinder {

    private final WelcomeController controller;
    private final WelcomeApplicationBackupController backup;
    
    public ExamBackupBinder(WelcomeController controller, WelcomeApplicationBackupController backup){
        this.controller = controller;
        this.backup = backup;
    }
    
    public void initBindings(){
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

    public void initHelp(Stage helpStage, HelpBinder help) {
        backup.getBtnBuHelp().setOnAction(evt -> {
            help.setHelpEntry("Backup");
            helpStage.show();
        });
    }
    
        
    
}
