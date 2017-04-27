/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationProgressController;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author user
 */
public class ProgressBinder {
    private final static Logger LOGGER = Logger.getLogger(ProgressBinder.class.getName());
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
        // Get an image from the value property of the TaskProcessor
        ObjectBinding<Image> value = Bindings.createObjectBinding(
                () -> { 
                    String val = controller.getInstaller().valueProperty().get();
                    if(val != null) {
                        File f = new File(WelcomeConstants.ICON_FILE_PATH
                                + "/"
                                + val
                                + ".png");
                        if(f.exists())
                            return new Image(f.toURI().toString());
                        else
                            return null;
                    }
                    else
                        return null;
                }, controller.getInstaller().valueProperty());
        
        install.getTxt_inst_installtitle().textProperty().bind(title);
        install.getTxt_inst_mesage().textProperty().bind(message);
        install.getImg_value().imageProperty().bind(value);
        // Hide the image if none is available
        install.getImg_value().managedProperty().bind(install.getImg_value().visibleProperty());
        install.getImg_value().visibleProperty().bind(controller.getInstaller().valueProperty().isNotNull());
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
                    // Play a sound on success
                    try {
                        String musicFile = WelcomeConstants.RESOURCE_FILE_PATH + "/sound/KDE_Notify.wav";
                        Media sound = new Media(new File(musicFile).toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(sound);
                        mediaPlayer.play();
                    } catch(Exception ex) {
                        LOGGER.log(Level.WARNING, "Sound couldn't be played", ex);
                    }
                    // Close scene if finished after 3 seconds
                    PauseTransition delay = new PauseTransition(Duration.seconds(3));
                    delay.setOnFinished(event -> ((Stage) install.getProg_inst_bar().getScene().getWindow()).close());
                    delay.play();
                }
            }
        });
    }

}
