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
public class WelcomeApplicationSystemController implements Initializable {

    @FXML
    private Button btn_sys_help;
    @FXML
    private TextField txt_sys_username;
    @FXML
    private TextField txt_sys_password;
    @FXML
    private TextField txt_sys_password_repeat;
    @FXML
    private ChoiceBox<Number> choice_sys_visible_for;
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
    
    private final Integer[] visibleForValues = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choice_sys_visible_for.setConverter(new StringConverter<Number>() {
            @Override
            public String toString(Number t) {
                return t.intValue() + " " + (t.intValue() == 1 ? rb.getString("welcomeApplicationSystem.second") : rb.getString("welcomeApplicationSystem.seconds"));
            }

            @Override
            public Number fromString(String string) {
                return Integer.valueOf(string.split(" ")[0]);
            }
        });
        
        choice_sys_visible_for.getItems().addAll(visibleForValues);
    }

    @FXML
    private void onClickShowHelp(MouseEvent event) {
        // TODO: popupwindow with Help Text
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TextField getTxt_sys_username() {
        return txt_sys_username;
    }

    public TextField getTxt_sys_password() {
        return txt_sys_password;
    }

    public TextField getTxt_sys_password_repeat() {
        return txt_sys_password_repeat;
    }

    public ChoiceBox<Number> getChoice_sys_visible_for() {
        return choice_sys_visible_for;
    }

    public CheckBox getCb_sys_start_wa() {
        return cb_sys_start_wa;
    }

    public CheckBox getCb_sys_direct_sound() {
        return cb_sys_direct_sound;
    }

    public CheckBox getCb_sys_block_kde() {
        return cb_sys_block_kde;
    }

    public CheckBox getCb_sys_allow_file_systems() {
        return cb_sys_allow_file_systems;
    }

    public TextField getTxt_sys_exchange_partition() {
        return txt_sys_exchange_partition;
    }

    public CheckBox getCb_sys_access_user() {
        return cb_sys_access_user;
    }

    public CheckBox getCb_sys_show_warning() {
        return cb_sys_show_warning;
    }
    
    public Button getBtnSysHelp() {
        return btn_sys_help;
    }
}
