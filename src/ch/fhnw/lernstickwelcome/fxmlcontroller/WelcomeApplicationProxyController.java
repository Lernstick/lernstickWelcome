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
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationProxyController implements Initializable {

    @FXML
    private Button btn_pxy_help;
    @FXML
    private CheckBox cb_pxy_use;
    @FXML
    private TextField txt_pxy_host;
    @FXML
    private TextField txt_pxy_port;
    @FXML
    private TextField txt_pxy_user;
    @FXML
    private TextField txt_pxy_pwd;

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
