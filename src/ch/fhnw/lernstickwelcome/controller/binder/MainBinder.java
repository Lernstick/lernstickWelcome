/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    private final WelcomeController controller;
    private final MainController welcomeApplicationStart;

    /**
     * Constructor of MainBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param welcomeApplicationStart FXML controller which proviedes the view
     * properties
     */
    public MainBinder(WelcomeController controller, MainController welcomeApplicationStart) {
        this.controller = controller;
        this.welcomeApplicationStart = welcomeApplicationStart;
    }

    /**
     * Method to initialize the handlers for this class.
     *
     * @param progressDialog the progressDialog that should be shown when
     * clicking on save.
     */
    public void initHandlers(Stage progressDialog) {
        welcomeApplicationStart.getBtSaveButton().setOnAction(evt -> {
            controller.startProcessingTasks();
            progressDialog.showAndWait();
        });
        welcomeApplicationStart.getBtFinishButton().setOnAction(evt -> {
            Stage s =((Stage) ((Node) evt.getSource()).getScene().getWindow());
            s.fireEvent(
                new WindowEvent(
                    s,
                    WindowEvent.WINDOW_CLOSE_REQUEST
                )
            );
        });
    }
}
