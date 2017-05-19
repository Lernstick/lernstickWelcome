/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller.exam;

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
public class PasswordChangeController implements Initializable {

    @FXML
    private Button btCancel;
    @FXML
    private Button btOk;
    @FXML
    private PasswordField pfPassword;
    @FXML
    private PasswordField pfPasswordRepeat;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btOk.disableProperty().bind(pfPassword.textProperty().isEmpty().or(pfPasswordRepeat.textProperty().isEmpty()));
    }    
    
    public Button getBtOk() {
        return btOk;
    }
    
    public Button getBtCancel() {
        return btCancel;
    }
    
    public TextField getTxtPassword() {
        return pfPassword;
    }
    
    public TextField getTxtPasswordRepeat() {
        return pfPasswordRepeat;
    }
}
