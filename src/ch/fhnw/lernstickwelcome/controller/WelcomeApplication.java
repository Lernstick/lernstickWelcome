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
    ExamInformationController examInformationController;
    ExamBackupController examBackupController;
    ExamSystemController examSystemController;
    public FXMLGuiLoader guiLoader;
    

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new WelcomeController();
        
        if(isExamEnvironment())
            controller.loadExamEnvironment();
        else
            controller.loadStandardEnvironment();    
        
        guiLoader = new FXMLGuiLoader(controller, isExamEnvironment());
        Scene scene = guiLoader.getMainStage();
        primaryStage.setScene(scene);
        primaryStage.show();
        
        
        examInformationController = new ExamInformationController(controller,  guiLoader.getInformation());
        examBackupController = new ExamBackupController(controller, guiLoader.getBackup());
        examSystemController = new ExamSystemController(controller, guiLoader.getSystem());
    
    }
    

    public boolean isExamEnvironment() {
        return getParameters().getRaw().contains("examEnvironment");
    }
    
    public static void main(String[] args) {
        WelcomeApplication.launch(args);
    }
}
