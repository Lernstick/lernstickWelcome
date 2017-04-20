/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationProgressController;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author user
 */
public class ProgressBinder {

    private final WelcomeController controller;
    private final WelcomeApplicationProgressController install;

    public ProgressBinder(WelcomeController controller, WelcomeApplicationProgressController install) {
        this.controller = controller;
        this.install = install;
    }

    public void initBindings() {
        install.getProg_inst_bar().progressProperty().bind(controller.getInstaller().progressProperty());
        install.getTxt_inst_prc().textProperty().bind(controller.getInstaller().progressProperty().multiply(100).asString("%.0f%%"));

        // Get the text of the title and the message from resource bundle
        StringBinding title = Bindings.createStringBinding(
                () -> {
                    try {
                        return controller.getBundle().getString(controller.getInstaller().titleProperty().get());
                    } catch(Exception ex) {
                        return controller.getInstaller().titleProperty().get(); // No text could be load
                    }
                }, controller.getInstaller().titleProperty());
        StringBinding message = Bindings.createStringBinding(
                () -> {
                    try {
                        return controller.getBundle().getString(controller.getInstaller().messageProperty().get());
                    } catch(Exception ex) {
                        return controller.getInstaller().messageProperty().get(); // No text could be load
                    }
                }, controller.getInstaller().messageProperty()
        );
        install.getTxt_inst_installtitle().textProperty().bind(title);
        install.getTxt_inst_mesage().textProperty().bind(message);
    }

    public void initHandlers(Stage errorDialog, WelcomeApplicationErrorController error) {
        // Listen on task processor finished
        controller.getInstaller().finishedProperty().addListener(cl -> {
            if (controller.getInstaller().finishedProperty().get()) {
                if(controller.getInstaller().exceptionProperty().getValue() != null) {
                    // Current stage not always on top anymore so error can be shown
                    Stage currentStage = ((Stage) install.getProg_inst_bar().getScene().getWindow());
                    currentStage.setAlwaysOnTop(false);
                    // Show error dialog on exception
                    error.initErrorMessage(controller.getInstaller().exceptionProperty().get());
                    errorDialog.showAndWait();
                    // Close stage after error was clicked away
                    currentStage.close();
                } else {
                    // Close scene if finished after 3 seconds
                    PauseTransition delay = new PauseTransition(Duration.seconds(3));
                    delay.setOnFinished(event -> ((Stage) install.getProg_inst_bar().getScene().getWindow()).close());
                    delay.play();
                }
            }
        });
    }

}
