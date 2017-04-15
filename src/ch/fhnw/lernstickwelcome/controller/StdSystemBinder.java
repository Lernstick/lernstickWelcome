/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationHelpController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemStdController;
import javafx.stage.Stage;

/**
 *
 * @author user
 */
public class StdSystemBinder {

    private WelcomeController controller;
    private WelcomeApplicationSystemStdController system;
    
    public StdSystemBinder(WelcomeController controller, WelcomeApplicationSystemStdController system){
        this.controller = controller;
        this.system = system;
    }
    
    public void initBindings(){
        system.getCb_sysStd_block_kde().selectedProperty().bindBidirectional(controller.getSysconf().getBlockKdeDesktopApplets());
        system.getCb_sysStd_direct_sound().selectedProperty().bindBidirectional(controller.getSysconf().getDirectSoundOutput());
        system.getCb_sysStd_show_warning().selectedProperty().bindBidirectional(controller.getPartition().getShowReadWriteWelcome());
        system.getCb_sysStd_start_wa().selectedProperty().bindBidirectional(controller.getPartition().getShowReadOnlyInfo());
        system.getChoice_sysStd_visible_for().valueProperty().bindBidirectional(controller.getSysconf().getTimeoutSeconds());
        system.getTxt_sys_exchange_partition().textProperty().bindBidirectional(controller.getPartition().getExchangePartitionLabel());
        system.getTxt_sysStd_username().textProperty().bindBidirectional(controller.getSysconf().getUsername());
        system.getCb_sysStd_start_wa().selectedProperty().bindBidirectional(controller.getPartition().getShowReadOnlyInfo());
        system.getCb_sysStd_proxy().selectedProperty().bindBidirectional(controller.getProxy().getProxyActive());
        system.getTxt_sysStd_host().textProperty().bindBidirectional(controller.getProxy().getHostname());
        system.getTxt_sysStd_port().textProperty().bindBidirectional(controller.getProxy().getPort());
        system.getTxt_sysStd_pwd().textProperty().bindBidirectional(controller.getProxy().getPassword());
        system.getTxt_sysStd_user().textProperty().bindBidirectional(controller.getProxy().getUsername());
       
    }

    public void initHelp(Stage helpStage, HelpBinder help) {
        system.getBtn_sys_help().setOnAction(evt -> {
            help.setHelpEntry("System");
            helpStage.show();
        });
    }
    
}

