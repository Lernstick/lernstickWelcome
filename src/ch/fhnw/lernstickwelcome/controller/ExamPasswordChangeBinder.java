/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationPasswordChangeController;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 *
 * @author sschw
 */
public class ExamPasswordChangeBinder {

    private final WelcomeApplicationPasswordChangeController password;
    private final WelcomeController controller;
    
    public ExamPasswordChangeBinder(WelcomeController controller, WelcomeApplicationPasswordChangeController password) {
        this.password = password;
        this.controller = controller;
    }
    
    public void initHandlers(Stage errorStage, WelcomeApplicationErrorController error) {
        password.getBtnOk().setOnAction(evt -> {
            controller.getSysconf().getPassword().setValue(password.getTxtPassword().getText());
            controller.getSysconf().getPasswordRepeat().setValue(password.getTxtPasswordRepeat().getText());
            try {
                controller.getSysconf().changePassword();
                ((Stage)((Node) evt.getSource()).getScene().getWindow()).close();
            } catch(ProcessingException ex) {
                error.initErrorMessage(ex);
                errorStage.showAndWait();
            }
        });
        password.getBtnCancel().setOnAction(evt -> 
                ((Stage)((Node) evt.getSource()).getScene().getWindow()).close());
    }
}
