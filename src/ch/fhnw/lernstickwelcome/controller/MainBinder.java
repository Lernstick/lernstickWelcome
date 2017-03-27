/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationStartController;
import javafx.stage.Stage;

/**
 *
 * @author sschw
 */
public class MainBinder {

    private final WelcomeController controller;
    private final WelcomeApplicationStartController welcomeApplicationStart;

    MainBinder(WelcomeController controller, WelcomeApplicationStartController welcomeApplicationStart) {
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
