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
public class ExamInformationBinder {
    private final WelcomeController controller;
    private final WelcomeApplicationInformationController information;

    
    public ExamInformationBinder(WelcomeController controller, WelcomeApplicationInformationController information){
        this.controller = controller;
        this.information = information;
    }
    
    public void initBindings(){
        information.getLabel_info_os().textProperty().bind(controller.getSysconf().getSystemname());
        information.getLabel_info_version().textProperty().bind(controller.getSysconf().getSystemversion());
    }
}
