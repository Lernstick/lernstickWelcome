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

import ch.fhnw.lernstickwelcome.fxmlcontroller.ProgressController;
import ch.fhnw.lernstickwelcome.model.TaskProcessor;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.scene.image.Image;

/**
 * Binder class to init binings between view components and backend (model)
 * properties
 *
 * @author sschw
 */
public class ProgressBinder {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "ch.fhnw.lernstickwelcome.Bundle");

    private final TaskProcessor taskProcessor;
    private final ProgressController progressController;

    /**
     * Constructor of HelpBinder class
     *
     * @param taskProcessor the task processor for the currently running welcome
     * version (standard or exam)
     * @param progressController FXML controller which proviedes the view
     * properties
     */
    public ProgressBinder(TaskProcessor taskProcessor,
            ProgressController progressController) {

        this.taskProcessor = taskProcessor;
        this.progressController = progressController;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * packend properties
     */
    public void initBindings() {

        // Get the text of the title and the message from resource bundle
        StringBinding title = Bindings.createStringBinding(() -> {
            try {
                return BUNDLE.getString(taskProcessor.titleProperty().get());
            } catch (Exception ex) {
                // return original text if no translation was found
                return taskProcessor.titleProperty().get();
            }
        }, taskProcessor.titleProperty());

        StringBinding message = Bindings.createStringBinding(() -> {
            try {
                return BUNDLE.getString(taskProcessor.messageProperty().get());
            } catch (Exception ex) {
                // return original text if no translation was found
                return taskProcessor.messageProperty().get();
            }
        }, taskProcessor.messageProperty());

        // Get an image from the value property of the TaskProcessor
        ObjectBinding<Image> value = Bindings.createObjectBinding(() -> {
            String val = taskProcessor.imageProperty().get();
            if (val != null) {
                URL url = getClass().getResource(
                        WelcomeConstants.ICON_FILE_PATH + "/" + val + ".png");
                if (url != null) {
                    return new Image(url.toURI().toString());
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }, taskProcessor.imageProperty());

        progressController.getTitleLabel().textProperty().bind(title);

        progressController.getProgressBar().progressProperty().bind(
                taskProcessor.progressProperty());

        progressController.getProgressLabel().textProperty().bind(
                taskProcessor.progressProperty().multiply(100).asString(
                        "%.0f%%"));

        progressController.getImageView().imageProperty().bind(value);
        progressController.getImageView().managedProperty().bind(
                progressController.getImageView().visibleProperty());
        progressController.getImageView().visibleProperty().bind(
                taskProcessor.imageProperty().isNotNull());

        progressController.getMessageLabel().textProperty().bind(message);
    }
}
