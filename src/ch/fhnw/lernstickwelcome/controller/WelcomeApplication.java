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
    private WelcomeController controller;
    private ExamInformationController examInformationController;
    private ExamFirewallController examFirewallController;
    private ExamBackupController examBackupController;
    private ExamSystemController examSystemController;
    private ProgressController progressController;
    private FXMLGuiLoader guiLoader;
    private MainBinder mainBinder;

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new WelcomeController();

        guiLoader = new FXMLGuiLoader(isExamEnvironment(), controller.getBundle());
        
        Stage errorStage = FXMLGuiLoader.createDialog(
                primaryStage,
                guiLoader.getErrorScene(),
                controller.getBundle().getString("welcomeApplicationError.title"),
                true
        );

        if (isExamEnvironment()) {
            controller.loadExamEnvironment();

            examInformationController = new ExamInformationController(controller, guiLoader.getInformation());
            examFirewallController = new ExamFirewallController(controller, guiLoader.getFirewall());
            examFirewallController.initBindings();
            examFirewallController.initHandlers(errorStage, guiLoader.getError());
            examBackupController = new ExamBackupController(controller, guiLoader.getBackup());
            examSystemController = new ExamSystemController(controller, guiLoader.getSystem());
        } else {
            controller.loadStandardEnvironment();
        }
        
        progressController = new ProgressController(controller, guiLoader.getProgress());
        progressController.initBindings();
        progressController.initHandlers(errorStage, guiLoader.getError());
        Stage progressStage = FXMLGuiLoader.createDialog(
                primaryStage,
                guiLoader.getProgressScene(),
                controller.getBundle().getString("welcomeApplicationProgress.save"),
                true
        );

        mainBinder = new MainBinder(controller, guiLoader.getWelcomeApplicationStart());
        mainBinder.initHandlers(progressStage);

        Scene scene = guiLoader.getMainStage();
        primaryStage.setTitle(controller.getBundle().getString("Welcome.title"));
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMinHeight(scene.getHeight());
        primaryStage.setMinWidth(scene.getWidth());
    }

    @Override
    public void stop() throws Exception {
        controller.closeApplication();
        System.exit(0);
    }

    private boolean isExamEnvironment() {
        return getParameters().getRaw().contains("examEnvironment");
    }

    public static void main(String[] args) {
        WelcomeApplication.launch(args);
    }
}
