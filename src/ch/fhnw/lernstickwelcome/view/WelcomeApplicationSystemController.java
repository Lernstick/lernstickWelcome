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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationSystemController implements Initializable, WelcomeApplicationViewController {

    @FXML
    private Button btn_sys_help;
    @FXML
    private TextField txt_sys_username;
    @FXML
    private TextField txt_sys_password;
    @FXML
    private TextField txt_sys_password_repeat;
    @FXML
    private ChoiceBox<?> choice_sys_visible_for;
    @FXML
    private CheckBox cb_sys_start_wa;
    @FXML
    private CheckBox cb_sys_direct_sound;
    @FXML
    private CheckBox cb_sys_block_kde;
    @FXML
    private CheckBox cb_sys_allow_file_systems;
    @FXML
    private TextField txt_sys_exchange_partition;
    @FXML
    private CheckBox cb_sys_access_user;
    @FXML
    private CheckBox cb_sys_show_warning;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @Override
    public Pane getPane() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @FXML
    private void onClickShowHelp(MouseEvent event) {
    }
    
}
