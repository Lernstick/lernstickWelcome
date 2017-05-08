/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    private ChoiceBox<Number> choice_sysStd_visible_for;
    @FXML
    private ToggleSwitch cb_sysStd_start_wa;
    @FXML
    private ToggleSwitch cb_sysStd_show_warning;
    @FXML
    private ToggleSwitch cb_sysStd_direct_sound;
    @FXML
    private ToggleSwitch cb_sysStd_block_kde;
    @FXML
    private TextField txt_sys_exchange_partition;
    @FXML
    private ToggleSwitch cb_sysStd_proxy;
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
    @FXML
    private TextField txt_sys_username;
    @FXML
    private TextField txt_sys_systemversion;
    @FXML
    private TextField txt_sys_systemname;

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
        
        txt_sys_username.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!isAllowed(newValue)) {
                    txt_sys_username.setText(oldValue);
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
        
        if (!WelcomeUtil.isImageWritable()) {
            choice_sysStd_visible_for.setVisible(false);
            txt_sys_systemname.setDisable(true);
            txt_sys_systemversion.setDisable(true);
        }
        
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

    public TextField getTxt_sys_username() {
        return txt_sys_username;
    }

    public TextField getTxt_sys_systemname() {
        return txt_sys_systemname;
    }

    public TextField getTxt_sys_systemversion() {
        return txt_sys_systemversion;
    }

    public ChoiceBox<Number> getChoice_sysStd_visible_for() {
        return choice_sysStd_visible_for;
    }

    public ToggleSwitch getCb_sysStd_start_wa() {
        return cb_sysStd_start_wa;
    }

    public ToggleSwitch getCb_sysStd_show_warning() {
        return cb_sysStd_show_warning;
    }

    public ToggleSwitch getCb_sysStd_direct_sound() {
        return cb_sysStd_direct_sound;
    }

    public ToggleSwitch getCb_sysStd_block_kde() {
        return cb_sysStd_block_kde;
    }

    public TextField getTxt_sys_exchange_partition() {
        return txt_sys_exchange_partition;
    }

    public ToggleSwitch getCb_sysStd_proxy() {
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
