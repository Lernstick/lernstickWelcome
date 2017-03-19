/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view;

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
public class WelcomeApplicationRecommendedSoftwareController implements Initializable {

    @FXML
    private Button btn_rs_help;
    @FXML
    private ToggleButton tbtn_rs_flash;
    @FXML
    private ToggleButton tbtn_rs_reader;
    @FXML
    private ToggleButton tbtn_rs_font;
    @FXML
    private ToggleButton tbtn_rs_mmFormats;

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
