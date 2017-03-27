/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationProgressController;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author user
 */
public class ProgressController {

    public ProgressController(WelcomeController controller, WelcomeApplicationProgressController install) {
        addBindings(controller, install);
        addHandlers(controller, install);
    }

    private void addBindings(WelcomeController controller, WelcomeApplicationProgressController install) {
        install.getProg_inst_bar().progressProperty().bind(controller.getInstaller().progressProperty());
        install.getTxt_inst_prc().textProperty().bind(controller.getInstaller().progressProperty().asString());

        // Get the text of the title and the message from resource bundle
        StringBinding title = Bindings.createStringBinding(
                () -> controller.getBundle().getString(controller.getInstaller().titleProperty().get()),
                controller.getInstaller().titleProperty()
        );
        StringBinding message = Bindings.createStringBinding(
                () -> controller.getBundle().getString(controller.getInstaller().messageProperty().get()),
                controller.getInstaller().messageProperty()
        );
        install.getTxt_inst_installtitle().textProperty().bind(title);
        install.getTxt_inst_mesage().textProperty().bind(message);
    }

    private void addHandlers(WelcomeController controller, WelcomeApplicationProgressController install) {
        // Close scene if finished after 3 seconds
        controller.getInstaller().finishedProperty().addListener(cl -> {
            if (controller.getInstaller().finishedProperty().get()) {
                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(event -> ((Stage) install.getProg_inst_bar().getScene().getWindow()).close());
            }
        });
    }

}
