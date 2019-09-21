/*
 * Copyright (C) 2017 FHNW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fhnw.lernstickwelcome.controller.binder.exam;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallDependenciesWarningController;
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
            Task firewallSaveTask = controller.getFirewallTask().newTask();
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
