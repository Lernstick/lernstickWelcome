package ch.fhnw.lernstickwelcome.controller.binder.exam;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.controller.binder.HelpBinder;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.SystemController;
import javafx.stage.Stage;

/**
 * Binder class to init binings between view components and backend (model) properties
 * 
 * @author Line Stettler
 */
public class SystemBinder {

    private WelcomeController controller;
    private SystemController system;
    
    /**
     * Constructor of ExamSystemBinder class
     * 
     * @param controller is needed to provide access to the backend properties
     * @param system FXML controller which proviedes the view properties
     */
    public SystemBinder(WelcomeController controller, SystemController system){
        this.controller = controller;
        this.system = system;
    }
    
    /**
     * Method to initialize the bidirectional bindings between the view and packend properties
     */
    public void initBindings(){
        system.getTsAccessUser().selectedProperty().bindBidirectional(controller.getPartition().accessExchangePartitionProperty());
        system.getTsAllowFileSystems().selectedProperty().bindBidirectional(controller.getSysconf().allowAccessToOtherFilesystemsProperty());
        system.getTsBlockKde().selectedProperty().bindBidirectional(controller.getSysconf().blockKdeDesktopAppletsProperty());
        system.getTsDirectSound().selectedProperty().bindBidirectional(controller.getSysconf().directSoundOutputProperty());
        system.getTsShowWarning().selectedProperty().bindBidirectional(controller.getPartition().showReadOnlyInfoProperty());
        system.getTsStartWa().selectedProperty().bindBidirectional(controller.getPartition().showReadWriteWelcomeProperty());
        system.getCbVisibleFor().valueProperty().bindBidirectional(controller.getSysconf().timeoutSecondsProperty());
        system.getTfSystemName().textProperty().bindBidirectional(controller.getSysconf().systemnameProperty());
        system.getTfSystemVersion().textProperty().bindBidirectional(controller.getSysconf().systemversionProperty());
        system.getTfExchangePartition().textProperty().bindBidirectional(controller.getPartition().exchangePartitionLabelProperty());
        system.getTxt_sys_password().textProperty().bindBidirectional(controller.getSysconf().passwordProperty());
        system.getTxt_sys_password_repeat().textProperty().bindBidirectional(controller.getSysconf().passwordRepeatProperty());
        system.getTfUsername().textProperty().bindBidirectional(controller.getSysconf().usernameProperty());
        
        system.getTfExchangePartition().setDisable(!controller.getPartition().hasExchangePartition());
    }

    /**
     * Open other view by clicking on help button
     * @param helpStage     additional window showing help
     * @param help          links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder help) {
        system.getBtnSysHelp().setOnAction(evt -> {
            help.setHelpEntryByChapter("3");
            helpStage.show();
        });
    }
    
}
