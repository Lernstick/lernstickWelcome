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
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationAdditionalSoftwareController implements Initializable {

    @FXML
    private Button btn_as_help;
    @FXML
    private ToggleButton tbtn_as_teaching;
    @FXML
    private ToggleButton tbtn_as_skype;
    @FXML
    private ToggleButton tbtn_as_gComris1;
    @FXML
    private ToggleButton tbtn_as_gComris2;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void onClickShowHelp(MouseEvent event) {
    }
    
}
