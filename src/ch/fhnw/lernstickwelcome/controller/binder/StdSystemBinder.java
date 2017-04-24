package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationHelpController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemStdController;
import javafx.stage.Stage;

/**
 *Binder class to init binings between view components and backend (model) properties
 * 
 * @author user
 */
public class StdSystemBinder {

    private WelcomeController controller;
    private WelcomeApplicationSystemStdController system;
    
   /**
     * Constructor of ExamInformationBinder class
     * 
     * @param controller        is needed to provide access to the backend properties
     * @param system            FXML controller which prviedes the view properties
     */
    public StdSystemBinder(WelcomeController controller, WelcomeApplicationSystemStdController system){
        this.controller = controller;
        this.system = system;
    }
    
    /**
     * Method to initialize the bidirectional bindings between the view and packend properties
     */
    public void initBindings(){
        system.getCb_sysStd_block_kde().selectedProperty().bindBidirectional(controller.getSysconf().blockKdeDesktopAppletsProperty());
        system.getCb_sysStd_direct_sound().selectedProperty().bindBidirectional(controller.getSysconf().directSoundOutputProperty());
        system.getCb_sysStd_show_warning().selectedProperty().bindBidirectional(controller.getPartition().showReadWriteWelcomeProperty());
        system.getCb_sysStd_start_wa().selectedProperty().bindBidirectional(controller.getPartition().showReadOnlyInfoProperty());
        system.getChoice_sysStd_visible_for().valueProperty().bindBidirectional(controller.getSysconf().timeoutSecondsProperty());
        system.getTxt_sys_exchange_partition().textProperty().bindBidirectional(controller.getPartition().exchangePartitionLabelProperty());
        system.getTxt_sys_username().textProperty().bindBidirectional(controller.getSysconf().usernameProperty());
        system.getTxt_sys_systemname().textProperty().bindBidirectional(controller.getSysconf().systemnameProperty());
        system.getTxt_sys_systemversion().textProperty().bindBidirectional(controller.getSysconf().systemversionProperty());
        system.getCb_sysStd_start_wa().selectedProperty().bindBidirectional(controller.getPartition().showReadOnlyInfoProperty());
        system.getCb_sysStd_proxy().selectedProperty().bindBidirectional(controller.getProxy().proxyActiveProperty());
        system.getTxt_sysStd_host().textProperty().bindBidirectional(controller.getProxy().hostnameProperty());
        system.getTxt_sysStd_port().textProperty().bindBidirectional(controller.getProxy().portProperty());
        system.getTxt_sysStd_pwd().textProperty().bindBidirectional(controller.getProxy().passwordProperty());
        system.getTxt_sysStd_user().textProperty().bindBidirectional(controller.getProxy().usernameProperty());
       
    }

    public void initHelp(Stage helpStage, HelpBinder help) {
        system.getBtn_sys_help().setOnAction(evt -> {
            help.setHelpEntry("4");
            helpStage.show();
        });
    }
    
}

