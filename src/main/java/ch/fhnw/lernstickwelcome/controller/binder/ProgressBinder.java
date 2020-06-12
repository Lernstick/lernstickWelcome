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
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ProgressController;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import java.net.URL;
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
 * Binder class to init binings between view components and backend (model)
 * properties
 *
 * @author sschw
 */
public class ProgressBinder {

    private final static Logger LOGGER
            = Logger.getLogger(ProgressBinder.class.getName());
    private final WelcomeController controller;
    private final ProgressController install;

    /**
     * Constructor of HelpBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param install FXML controller which proviedes the view properties
     */
    public ProgressBinder(
            WelcomeController controller, ProgressController install) {

        this.controller = controller;
        this.install = install;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * packend properties
     */
    public void initBindings() {
        install.getPbInstBar().progressProperty().bind(
                controller.getInstaller().progressProperty());
        install.getLbInstPrc().textProperty().bind(
                controller.getInstaller().progressProperty().
                        multiply(100).asString("%.0f%%"));

        // Get the text of the title and the message from resource bundle
        StringBinding title = Bindings.createStringBinding(() -> {
            try {
                return controller.getBundle().getString(
                        controller.getInstaller().titleProperty().get());
            } catch (Exception ex) {
                // no text could be loaded
                return controller.getInstaller().titleProperty().get();
            }
        }, controller.getInstaller().titleProperty());
        StringBinding message = Bindings.createStringBinding(() -> {
            try {
                return controller.getBundle().getString(
                        controller.getInstaller().messageProperty().get());
            } catch (Exception ex) {
                // no text could be loaded
                return controller.getInstaller().messageProperty().get();
            }
        }, controller.getInstaller().messageProperty()
        );
        // Get an image from the value property of the TaskProcessor
        ObjectBinding<Image> value = Bindings.createObjectBinding(() -> {
            String val = controller.getInstaller().valueProperty().get();
            if (val != null) {
                URL url = ProgressBinder.class.getResource(
                        WelcomeConstants.ICON_FILE_PATH + "/" + val + ".png");
                if (url != null) {
                    return new Image(url.toURI().toString());
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }, controller.getInstaller().valueProperty());

        install.getLbInstalltitle().textProperty().bind(title);
        install.getLbMesage().textProperty().bind(message);
        install.getIvValue().imageProperty().bind(value);
        // Hide the image if none is available
        install.getIvValue().managedProperty().bind(
                install.getIvValue().visibleProperty());
        install.getIvValue().visibleProperty().bind(
                controller.getInstaller().valueProperty().isNotNull());
    }

    /**
     * Method to initialize the handlers for this class.
     *
     * @param errorDialog the dialog that should be shown on error.
     * @param error the controller which the error message can be provided.
     */
    public void initHandlers(Stage errorDialog, ErrorController error) {
        // Listen on task processor finished
        controller.getInstaller().finishedProperty().addListener(cl -> {
            if (controller.getInstaller().finishedProperty().get()) {
                if (controller.getInstaller().exceptionProperty().getValue()
                        != null) {
                    // current stage not always on top anymore so error can be
                    // shown
                    Stage currentStage = ((Stage) install.getPbInstBar().
                            getScene().getWindow());
                    // show error dialog on exception
                    error.initErrorMessage(controller.getInstaller().
                            exceptionProperty().get());
                    errorDialog.showAndWait();
                    // Close stage after error was clicked away
                    currentStage.close();

                } else {

                    // Play a sound on success
                    try {
                        URL url = ProgressBinder.class.getResource(
                                "/sound/KDE_Notify.wav");
                        Media sound = new Media(url.toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(sound);
                        mediaPlayer.play();
                    } catch (Exception ex) {
                        // There might be an exception
                        // http://stackoverflow.com/questions/24090356/javafx-mediaplayer-could-not-create-player-error-in-ubuntu-14-04
                        LOGGER.log(Level.WARNING, "Sound couldn't be played", ex);
                    }

                    // Close scene if finished after 3 seconds
                    PauseTransition delay
                            = new PauseTransition(Duration.seconds(3));
                    delay.setOnFinished(event -> ((Stage) install.
                            getPbInstBar().getScene().getWindow()).close());
                    delay.play();
                }
            }
        });
    }
}
