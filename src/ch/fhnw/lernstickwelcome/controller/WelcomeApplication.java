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
    private ExamBackupController examBackupController;
    private ExamSystemController examSystemController;
    private ProgressController progressController;
    private FXMLGuiLoader guiLoader;
    private MainBinder mainBinder;

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new WelcomeController();

        guiLoader = new FXMLGuiLoader(isExamEnvironment(), controller.getBundle());
        Scene scene = guiLoader.getMainStage();

        if (isExamEnvironment()) {
            controller.loadExamEnvironment();

            examInformationController = new ExamInformationController(controller, guiLoader.getInformation());
            examBackupController = new ExamBackupController(controller, guiLoader.getBackup());
            examSystemController = new ExamSystemController(controller, guiLoader.getSystem());
        } else {
            controller.loadStandardEnvironment();
        }
        progressController = new ProgressController(controller, guiLoader.getProgress());

        Stage progressStage = FXMLGuiLoader.createDialog(
                primaryStage,
                guiLoader.getProgressScene(),
                controller.getBundle().getString("welcomeApplicationProgress.save"),
                true
        );

        mainBinder = new MainBinder(controller, guiLoader.getWelcomeApplicationStart());
        mainBinder.initHandlers(progressStage);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    @Override
    public void stop() throws Exception {
        controller.closeApplication();
        System.exit(0);
    }

    public boolean isExamEnvironment() {
        return getParameters().getRaw().contains("examEnvironment");
    }

    public static void main(String[] args) {
        WelcomeApplication.launch(args);
    }
}
