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
import javafx.util.StringConverter;


/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationSystemStdController implements Initializable {

    private final Integer[] visibleForValues = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

    
    @FXML
    private Button btn_sys_help;
    @FXML
    private TextField txt_sysStd_username;
    @FXML
    private ChoiceBox<Number> choice_sysStd_visible_for;
    @FXML
    private CheckBox cb_sysStd_start_wa;
    @FXML
    private CheckBox cb_sysStd_show_warning;
    @FXML
    private CheckBox cb_sysStd_direct_sound;
    @FXML
    private CheckBox cb_sysStd_block_kde;
    @FXML
    private TextField txt_sys_exchange_partition;
    @FXML
    private CheckBox cb_sysStd_proxy;
    @FXML
    private TextField txt_sysStd_host;
    @FXML
    private TextField txt_sysStd_port;
    @FXML
    private TextField txt_sysStd_user;
    @FXML
    private TextField txt_sysStd_pwd;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choice_sysStd_visible_for.setConverter(new StringConverter<Number>() {
            @Override
            public String toString(Number t) {
                return t.intValue() + " " + (t.intValue() == 1 ? rb.getString("welcomeApplicationSystem.second") : rb.getString("welcomeApplicationSystem.seconds"));
            }

            @Override
            public Number fromString(String string) {
                return Integer.valueOf(string.split(" ")[0]);
            }
        });
        
        choice_sysStd_visible_for.getItems().addAll(visibleForValues);
        
        if(!cb_sysStd_proxy.isSelected()){
            txt_sysStd_host.setEditable(false);
            txt_sysStd_port.setEditable(false);
            txt_sysStd_user.setEditable(false);
            txt_sysStd_pwd.setEditable(false);
        }
    }    

    @FXML
    private void onClickShowHelp(MouseEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @FXML
    private void onProxyClicked(MouseEvent event) {
        if(cb_sysStd_proxy.isSelected()){
            txt_sysStd_host.setEditable(true);
            txt_sysStd_port.setEditable(true);
            txt_sysStd_user.setEditable(true);
            txt_sysStd_pwd.setEditable(true);
        }else{
            txt_sysStd_host.setEditable(false);
            txt_sysStd_port.setEditable(false);
            txt_sysStd_user.setEditable(false);
            txt_sysStd_pwd.setEditable(false);
        }
    }
    
}
