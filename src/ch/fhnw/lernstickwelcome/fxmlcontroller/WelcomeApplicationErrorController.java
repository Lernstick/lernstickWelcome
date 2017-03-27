/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.controller.ProcessingException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
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
public class WelcomeApplicationErrorController implements Initializable {

    @FXML
    private Label lblErrorTitle;
    @FXML
    private Label lblErrorMessage;

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
            lblErrorTitle.setText(rb.getString("WelcomeApplicationError.saveStopped"));
            lblErrorMessage.setText(MessageFormat.format(ex.getMessage(), ((ProcessingException) ex).getMessageDetails()));
        } else {
            lblErrorTitle.setText(rb.getString("WelcomeApplicationError.unknownException"));
            lblErrorMessage.setText(rb.getString("WelcomeApplicationError.unknownExceptionMessage"));
        }
    }

    @FXML
    private void btnOkOnAction(ActionEvent event) {
        ((Stage)((Node) event.getSource()).getScene().getWindow()).close(); 
    }
    
}
