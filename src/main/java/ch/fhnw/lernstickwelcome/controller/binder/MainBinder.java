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
import ch.fhnw.lernstickwelcome.fxmlcontroller.MainController;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Binder class to init binings between view components and backend (model)
 * properties
 *
 * @author sschw
 */
public class MainBinder {

    private final WelcomeController welcomeController;
    private final MainController mainController;

    /**
     * Constructor of MainBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param mainController FXML controller which proviedes the view properties
     */
    public MainBinder(WelcomeController controller,
            MainController mainController) {

        this.welcomeController = controller;
        this.mainController = mainController;
    }

    /**
     * Method to initialize the handlers for this class.
     *
     * @param progressStage the stage that is shown when clicking the save
     * button
     */
    public void initHandlers(Stage progressStage) {

        mainController.getSaveButton().setOnAction(evt -> {
            welcomeController.startProcessingTasks();
            progressStage.showAndWait();
        });

        mainController.getFinishButton().setOnAction(evt -> {
            Stage stage = 
                    ((Stage) ((Node) evt.getSource()).getScene().getWindow());
            stage.fireEvent(
                    new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }
}
