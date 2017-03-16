/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import ch.fhnw.lernstickwelcome.util.FXMLGuiLoader;

/**
 *
 * @author sschw
 */
public class WelcomeApplication extends Application {
    WelcomeController controller;
    public FXMLGuiLoader guiLoader;

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new WelcomeController();
        guiLoader = new FXMLGuiLoader(controller, isExamEnvironment());
        
        Scene scene = guiLoader.getWelcomeApplicationStart();
        primaryStage.setScene(scene);
        primaryStage.show();
        if(isExamEnvironment())
            controller.loadExamEnvironment();
        else
            controller.loadStandardEnvironment();    
    }
    

    public boolean isExamEnvironment() {
        return getParameters().getRaw().contains("examEnvironment");
    }
    
    public static void main(String[] args) {
        WelcomeApplication.launch(args);
    }
}
