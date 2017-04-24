/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private TextField txt_sys_systemname;
    @FXML
    private TextField txt_sys_systemversion;
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
        
        txt_sys_username.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (isAllowed(newValue)) {
                    txt_sys_username.setText(newValue);
                }
            }
            
            private boolean isAllowed(String string) {
                for (int i = 0, length = string.length(); i < length; i++) {
                    char character = string.charAt(i);
                    if ((character == ':')
                            || (character == ',')
                            || (character == '=')) {
                        Toolkit.getDefaultToolkit().beep();
                        return false;
                    }
                }
                return true;
            }
        });

        txt_sys_exchange_partition.textProperty().addListener(new ChangeListener<String>() {
            private final static int MAX_CHARS = 11;
            
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue == null) return;
                // only allow ASCII input
                if (!isASCII(newValue)) {
                    txt_sys_exchange_partition.setText(oldValue);
                    return;
                }

                if (getSpecialLength(newValue) <= MAX_CHARS) {
                    txt_sys_exchange_partition.setText(newValue);
                } else {
                    txt_sys_exchange_partition.setText(oldValue);
                    Toolkit.getDefaultToolkit().beep();
                }
            }
            
            private boolean isASCII(String string) {
                for (int i = 0, length = string.length(); i < length; i++) {
                    char character = string.charAt(i);
                    if ((character < 0) || (character > 127)) {
                        return false;
                    }
                }
                return true;
            }

            private int getSpecialLength(String string) {
                // follow special rules for VFAT labels
                int count = 0;
                for (int i = 0, length = string.length(); i < length; i++) {
                    char character = string.charAt(i);
                    if ((character >= 0) && (character <= 127)) {
                        // ASCII
                        if ((character == 39) || (character == 96)) {
                            // I have no idea why those both characters take up 3 bytes
                            // but they really do...
                            count += 3;
                        } else {
                            count++;
                        }
                    } else {
                        // non ASCII
                        count += 2;
                    }
                }
                return count;
            }
        });
        
        choice_sys_visible_for.getItems().addAll(visibleForValues);
        
        if (!WelcomeUtil.isImageWritable()) {
            choice_sys_visible_for.setVisible(false);
            txt_sys_systemname.setDisable(true);
            txt_sys_systemversion.setDisable(true);
        }
    }

    @FXML
    private void onClickShowHelp(MouseEvent event) {
        // TODO: popupwindow with Help Text
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public TextField getTxt_sys_systemname() {
        return txt_sys_systemname;
    }
    
    public TextField getTxt_sys_systemversion() {
        return txt_sys_systemversion;
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
