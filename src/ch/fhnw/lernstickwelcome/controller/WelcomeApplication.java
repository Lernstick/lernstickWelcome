/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.util.FXMLGuiLoader;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author sschw
 */
public class WelcomeApplication extends Application {
    WelcomeController controller;
    ExamInformationController examInformationController;
    ExamBackupController examBackupController;
    ExamSystemController examSystemController;
    InstallController installController;
    public FXMLGuiLoader guiLoader;
    

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new WelcomeController();
        ResourceBundle rb = ResourceBundle.getBundle("ch/fhnw/lernstickwelcome/Bundle");
        
        guiLoader = new FXMLGuiLoader(isExamEnvironment(), rb);
        Scene scene = guiLoader.getMainStage();
        
        if(isExamEnvironment()){
            controller.loadExamEnvironment(rb);
            
            examInformationController = new ExamInformationController(controller,  guiLoader.getInformation());
            examBackupController = new ExamBackupController(controller, guiLoader.getBackup());
            examSystemController = new ExamSystemController(controller, guiLoader.getSystem());
            installController = new InstallController(controller, guiLoader.getInstaller());
        }else{
            controller.loadStandardEnvironment(rb);  
        }

        primaryStage.setScene(scene);
        primaryStage.show();
        
    }

    @Override
    public void stop() throws Exception {
        controller.closeApplication();
        System.exit(0); // TODO investigate into thread that still runs.
    }

    public boolean isExamEnvironment() {
        return getParameters().getRaw().contains("examEnvironment");
    }
    
    public static void main(String[] args) {
        WelcomeApplication.launch(args);
    }
}
