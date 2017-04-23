/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationMainController;
import javafx.stage.Stage;

/**
 *
 * @author sschw
 */
public class MainBinder {

    private final WelcomeController controller;
    private final WelcomeApplicationMainController welcomeApplicationStart;

    public MainBinder(WelcomeController controller, WelcomeApplicationMainController welcomeApplicationStart) {
        this.controller = controller;
        this.welcomeApplicationStart = welcomeApplicationStart;
    }

    public void initHandlers(Stage progressDialog) {
        welcomeApplicationStart.getSaveButton().setOnAction(evt -> {
            controller.startProcessingTasks();
            progressDialog.showAndWait();
        });
        welcomeApplicationStart.getFinishButton().setOnAction(evt -> {
            controller.startProcessingTasks();
            progressDialog.showAndWait();
            ((Stage)progressDialog.getOwner()).close();
        });
    }
}
