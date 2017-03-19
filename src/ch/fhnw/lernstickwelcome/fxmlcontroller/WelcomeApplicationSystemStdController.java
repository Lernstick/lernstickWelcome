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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationSystemStdController implements Initializable {

    @FXML
    private Button btn_sysStd_help;
    @FXML
    private TextField txt_sysStd_username;
    @FXML
    private ChoiceBox<?> choice_sysStd_visible_for;
    @FXML
    private CheckBox cb_sysStd_start_wa;
    @FXML
    private CheckBox cb_sysStd_show_warning;
    @FXML
    private CheckBox cb_sysStd_direct_sound;
    @FXML
    private CheckBox cb_sysStd_block_kde;
    @FXML
    private TextField txt_sysStd_exchange_partition;

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
