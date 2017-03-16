/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.view.WelcomeApplicationSystemController;

/**
 *
 * @author user
 */
public class ExamSystemController {
    
    public ExamSystemController(WelcomeController controller, WelcomeApplicationSystemController system){
        addBindings(controller, system);
    }
    
    private void addBindings(WelcomeController controller, WelcomeApplicationSystemController system){
        system.getCb_sys_access_user().selectedProperty().bindBidirectional(controller.getSystem().);
        system.getCb_sys_allow_file_systems().selectedProperty().bindBidirectional(controller.getSystem().getAllowAccessToOtherFilesystems());
        system.getCb_sys_block_kde().selectedProperty().bindBidirectional(controller.getSystem().getBlockKdeDesktopApplets());
        system.getCb_sys_direct_sound().selectedProperty().bindBidirectional(controller.getSystem().getDirectSoundOutput());
        system.getCb_sys_show_warning().selectedProperty().bindBidirectional(controller.getSystem().);
        system.getCb_sys_start_wa().selectedProperty().bindBidirectional(controller.getSystem().);
        system.getChoice_sys_visible_for().valueProperty().bindBidirectional(controller.getSystem().);
        system.getTxt_sys_exchange_partition().textProperty().bindBidirectional(controller.getSystem().);
        system.getTxt_sys_password().textProperty().bindBidirectional(controller.getSystem().getPassword());
        system.getTxt_sys_password_repeat().textProperty().bindBidirectional(controller.getSystem().getPasswordRepeat());
        system.getTxt_sys_username().textProperty().bindBidirectional(controller.getSystem().getUsername());
    }
    
}
