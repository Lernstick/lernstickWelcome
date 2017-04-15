/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationAdditionalSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemStdController;

/**
 *
 * @author user
 */
public class StdAdditionalSoftwareBinder {
    
    private WelcomeController controller;
    private WelcomeApplicationAdditionalSoftwareController additional_sw;
    
    public StdAdditionalSoftwareBinder(WelcomeController controller, WelcomeApplicationAdditionalSoftwareController additional_sw){
        this.controller = controller;
        this.additional_sw = additional_sw;
    }
    
    public void initBindings(){
       // controller.getTeachApps().getApps().forEach();
        //controller.get
        
       // additional_sw.getTbtn_as_teaching().disableProperty().setValue(controller.getTeachApps().getApps().forEach(Sele()));
                
    }
    
}
