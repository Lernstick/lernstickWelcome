
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationBackupController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationHelpController;
import javafx.stage.Stage;

/**
 * Binder class to init binings between view components and backend (model) properties
 * 
 * @author Sandro Schwager, Line Stettler
 */
public class ExamBackupBinder {

    private final WelcomeController controller;
    private final WelcomeApplicationBackupController backup;
    
    /**
     * Constructor of ExamBackupBinder class
     * 
     * @param controller        is needed to provide access to the backend properties
     * @param backup            FXML controller which prviedes the view properties
     */
    public ExamBackupBinder(WelcomeController controller, WelcomeApplicationBackupController backup){
        this.controller = controller;
        this.backup = backup;
    }
    
    /**
     * Method to initialize the bidirectional bindings between the view and packend properties
     */
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

    /**
     * Open other view by clicking on help button
     * @param helpStage     additional window showing help
     * @param help          links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder help) {
        backup.getBtnBuHelp().setOnAction(evt -> {
            help.setHelpEntry("Backup");
            helpStage.show();
        });
    }
    
        
    
}
