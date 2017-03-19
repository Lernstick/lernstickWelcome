/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationInformationController;

/**
 *
 * @author user
 */
public class ExamInformationController {

    
    public ExamInformationController(WelcomeController controller, WelcomeApplicationInformationController information){
        getSystem(controller, information);
    }
    
    private void getSystem(WelcomeController controller, WelcomeApplicationInformationController information){
        information.getLabel_info_os().setText(controller.getSystem().getSystemname().getValue());
        information.getLabel_info_version().setText(controller.getSystem().getSystemversion().getValue());
    }
}
