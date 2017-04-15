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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
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
    @FXML
    private Label lbl_sysStd_host;
    @FXML
    private Label lbl_sysStd_port;
    @FXML
    private Label lbl_sysStd_user;
    @FXML
    private Label lbl_sysStd_pwd;

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
        
        txt_sysStd_host.disableProperty().bind(cb_sysStd_proxy.selectedProperty().not());
        txt_sysStd_port.disableProperty().bind(cb_sysStd_proxy.selectedProperty().not());
        txt_sysStd_pwd.disableProperty().bind(cb_sysStd_proxy.selectedProperty().not());
        txt_sysStd_user.disableProperty().bind(cb_sysStd_proxy.selectedProperty().not());  
    }    

    public Integer[] getVisibleForValues() {
        return visibleForValues;
    }

    public Button getBtn_sys_help() {
        return btn_sys_help;
    }

    public TextField getTxt_sysStd_username() {
        return txt_sysStd_username;
    }

    public ChoiceBox<Number> getChoice_sysStd_visible_for() {
        return choice_sysStd_visible_for;
    }

    public CheckBox getCb_sysStd_start_wa() {
        return cb_sysStd_start_wa;
    }

    public CheckBox getCb_sysStd_show_warning() {
        return cb_sysStd_show_warning;
    }

    public CheckBox getCb_sysStd_direct_sound() {
        return cb_sysStd_direct_sound;
    }

    public CheckBox getCb_sysStd_block_kde() {
        return cb_sysStd_block_kde;
    }

    public TextField getTxt_sys_exchange_partition() {
        return txt_sys_exchange_partition;
    }

    public CheckBox getCb_sysStd_proxy() {
        return cb_sysStd_proxy;
    }

    public TextField getTxt_sysStd_host() {
        return txt_sysStd_host;
    }

    public TextField getTxt_sysStd_port() {
        return txt_sysStd_port;
    }

    public TextField getTxt_sysStd_user() {
        return txt_sysStd_user;
    }

    public TextField getTxt_sysStd_pwd() {
        return txt_sysStd_pwd;
    }    
    
    
}
