
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.BackupController;
import javafx.stage.Stage;

/**
 * Binder class to init binings between view components and backend (model) properties
 * 
 * @author sschw, Line Stettler
 */
public class ExamBackupBinder {

    private final WelcomeController controller;
    private final BackupController backup;
    
    /**
     * Constructor of ExamBackupBinder class
     * 
     * @param controller        is needed to provide access to the backend properties
     * @param backup            FXML controller which prviedes the view properties
     */
    public ExamBackupBinder(WelcomeController controller, BackupController backup){
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
