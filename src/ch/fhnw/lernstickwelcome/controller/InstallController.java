/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationInstallController;

/**
 *
 * @author user
 */
public class InstallController {
    public InstallController(WelcomeController controller, WelcomeApplicationInstallController install){
        addBindings(controller, install);
    }
    
    private void addBindings(WelcomeController controller, WelcomeApplicationInstallController install){
        // FIXME Isnt initialized in FXMLController
        /*install.getProg_inst_bar().progressProperty().bind(controller.getInstaller().progressProperty());
        install.getTxt_inst_installtitle().textProperty().bind(controller.getInstaller().titleProperty());
        install.getTxt_inst_prc().textProperty().bind(controller.getInstaller().progressProperty().asString());
        install.getTxt_inst_mesage().textProperty().bind(controller.getInstaller().messageProperty());*/
    }
            
    
}
