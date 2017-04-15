/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author root
 */
public class WelcomeApplicationPasswordChangeController implements Initializable {

    @FXML
    private Button btnCancel;
    @FXML
    private Button btnOk;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtPasswordRepeat;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public Button getBtnOk() {
        return btnOk;
    }
    
    public Button getBtnCancel() {
        return btnCancel;
    }
    
    public TextField getTxtPassword() {
        return txtPassword;
    }
    
    public TextField getTxtPasswordRepeat() {
        return txtPasswordRepeat;
    }
}
