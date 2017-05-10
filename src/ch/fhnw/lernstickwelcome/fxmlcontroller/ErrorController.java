/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author root
 */
public class ErrorController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ErrorController.class.getName());
    
    @FXML
    private Label lbTitle;
    @FXML
    private Label lblMessage;

    private ResourceBundle rb;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.rb = rb;
    }
    
    public void initErrorMessage(Exception ex) {
        if(ex instanceof ProcessingException) {
            lbTitle.setText(rb.getString("welcomeApplicationError.saveStopped"));
            lblMessage.setText(MessageFormat.format(rb.getString(ex.getMessage()), ((ProcessingException) ex).getMessageDetails()));
            LOGGER.log(Level.INFO, "Error Dialog shown for ProcessingException", ex);
        } else {
            lbTitle.setText(rb.getString("welcomeApplicationError.unknownException"));
            lblMessage.setText(rb.getString("welcomeApplicationError.unknownExceptionMessage"));
            LOGGER.log(Level.SEVERE, "Error Dialog shown for unexpected Exception", ex);
        }
    }

    @FXML
    private void btOkOnAction(ActionEvent event) {
        ((Stage)((Node) event.getSource()).getScene().getWindow()).close(); 
    }
    
}
