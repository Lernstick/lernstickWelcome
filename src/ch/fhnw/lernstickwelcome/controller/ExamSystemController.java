/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemController;

/**
 *
 * @author user
 */
public class ExamSystemController {
    
    public ExamSystemController(WelcomeController controller, WelcomeApplicationSystemController system){
        addBindings(controller, system);
    }
    
    private void addBindings(WelcomeController controller, WelcomeApplicationSystemController system){
        system.getCb_sys_access_user().selectedProperty().bindBidirectional(controller.getPartition().getAccessExchangePartition());
        system.getCb_sys_allow_file_systems().selectedProperty().bindBidirectional(controller.getSysconf().getAllowAccessToOtherFilesystems());
        system.getCb_sys_block_kde().selectedProperty().bindBidirectional(controller.getSysconf().getBlockKdeDesktopApplets());
        system.getCb_sys_direct_sound().selectedProperty().bindBidirectional(controller.getSysconf().getDirectSoundOutput());
        system.getCb_sys_show_warning().selectedProperty().bindBidirectional(controller.getPartition().getShowReadWriteWelcome());
        system.getCb_sys_start_wa().selectedProperty().bindBidirectional(controller.getPartition().getShowReadOnlyInfo());
        system.getChoice_sys_visible_for().valueProperty().bindBidirectional(controller.getSysconf().getTimeoutSeconds());
        system.getTxt_sys_exchange_partition().textProperty().bindBidirectional(controller.getPartition().getExchangePartitionLabel());
        system.getTxt_sys_password().textProperty().bindBidirectional(controller.getSysconf().getPassword());
        system.getTxt_sys_password_repeat().textProperty().bindBidirectional(controller.getSysconf().getPasswordRepeat());
        system.getTxt_sys_username().textProperty().bindBidirectional(controller.getSysconf().getUsername());
    }
    
}
