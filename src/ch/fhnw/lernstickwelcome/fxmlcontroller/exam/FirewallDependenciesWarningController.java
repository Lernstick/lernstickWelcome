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

/**
 * FXML Controller class
 *
 * @author root
 */
public class FirewallDependenciesWarningController implements Initializable {

    @FXML
    private Button btCancel;
    @FXML
    private Button btSave;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
    public Button getBtSave() {
        return btSave;
    }
    
    public Button getBtCancel() {
        return btCancel;
    }
    
}
