/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author sschw
 */
public class WelcomeApplication extends Application {
    WelcomeController controller;

    @Override
    public void start(Stage stage) throws Exception {
        controller = new WelcomeController();
        
        if(isExamEnvironment())
            controller.loadExamEnvironment();
        else
            controller.loadStandardEnvironment();
        
        
    }

    private boolean isExamEnvironment() {
        return getParameters().getRaw().contains("examEnvironment");
    }
    
    public static void main(String[] args) {
        WelcomeApplication.launch(args);
    }
}
