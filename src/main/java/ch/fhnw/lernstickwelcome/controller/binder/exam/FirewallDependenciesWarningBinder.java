/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller.binder.exam;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallDependenciesWarningController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Binder class to init binings and between view components and backend (model)
 * properties and add handlers
 *
 * @author sschw
 */
public class FirewallDependenciesWarningBinder {

    private WelcomeController controller;
    private FirewallDependenciesWarningController firewallWarning;

    public FirewallDependenciesWarningBinder(WelcomeController controller, FirewallDependenciesWarningController firewallWarning) {
        this.controller = controller;
        this.firewallWarning = firewallWarning;
    }

    public void initHandlers(Stage stage, ErrorController error, Stage errorStage) {
        firewallWarning.getBtCancel().setOnAction(evt
                -> ((Stage) ((Node) evt.getSource()).getScene().getWindow()).close()
        );
        firewallWarning.getBtSave().setOnAction(evt -> {
            Task firewallSaveTask = controller.getFirewall().newTask();
            firewallSaveTask.setOnSucceeded(e -> {
                ((Node) evt.getSource()).getScene().getRoot().setDisable(false);
                ((Stage) ((Node) evt.getSource()).getScene().getWindow()).close();
                stage.show();

            });
            firewallSaveTask.setOnFailed(e -> {
                ((Node) evt.getSource()).getScene().getRoot().setDisable(false);
                ((Stage) ((Node) evt.getSource()).getScene().getWindow()).close();
                error.initErrorMessage((Exception) firewallSaveTask.getException());
                errorStage.show();
            });
            new Thread(firewallSaveTask).start();
            ((Node) evt.getSource()).getScene().getRoot().setDisable(true);
        });
    }
}
