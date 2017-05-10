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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;


/**
 * FXML Controller class
 *
 * @author user
 */
public class SystemStdController implements Initializable {

    private final Integer[] visibleForValues = new Integer[] { 5, 10, 15, 20, 25, 30, 40, 50, 60 };

    
    @FXML
    private Button btHelp;
    @FXML
    private ComboBox<Number> cbVisibleFor;
    @FXML
    private ToggleSwitch tsStartWa;
    @FXML
    private ToggleSwitch tsShowWarning;
    @FXML
    private ToggleSwitch tsDirectSound;
    @FXML
    private ToggleSwitch tsBlockKde;
    @FXML
    private TextField tfExchangePartition;
    @FXML
    private ToggleSwitch tsProxy;
    @FXML
    private TextField tfHost;
    @FXML
    private TextField tfPort;
    @FXML
    private TextField tfUser;
    @FXML
    private TextField tfPwd;
    @FXML
    private Label lbHost;
    @FXML
    private Label lbPort;
    @FXML
    private Label lbUser;
    @FXML
    private Label lbPwd;
    @FXML
    private TextField tfUsername;
    @FXML
    private TextField tfSystemversion;
    @FXML
    private TextField tfSystemname;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbVisibleFor.setConverter(new StringConverter<Number>() {
            @Override
            public String toString(Number t) {
                return t.intValue() + " " + (t.intValue() == 1 ? rb.getString("welcomeApplicationSystem.second") : rb.getString("welcomeApplicationSystem.seconds"));
            }

            @Override
            public Number fromString(String string) {
                return Integer.valueOf(string.split(" ")[0]);
            }
        });
        cbVisibleFor.getItems().addAll(visibleForValues);
        cbVisibleFor.setEditable(true);
        
        tfUsername.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!isAllowed(newValue)) {
                    tfUsername.setText(oldValue);
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
        
        tfExchangePartition.textProperty().addListener(new ChangeListener<String>() {
            private final static int MAX_CHARS = 11;
            
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue == null) return;
                // only allow ASCII input
                if (!isASCII(newValue)) {
                    tfExchangePartition.setText(oldValue);
                    return;
                }

                if (getSpecialLength(newValue) <= MAX_CHARS) {
                    tfExchangePartition.setText(newValue);
                } else {
                    tfExchangePartition.setText(oldValue);
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
            cbVisibleFor.setVisible(false);
            tfSystemname.setDisable(true);
            tfSystemversion.setDisable(true);
        }
        
        tfHost.disableProperty().bind(tsProxy.selectedProperty().not());
        tfPort.disableProperty().bind(tsProxy.selectedProperty().not());
        tfPwd.disableProperty().bind(tsProxy.selectedProperty().not());
        tfUser.disableProperty().bind(tsProxy.selectedProperty().not());  
    }    

    public Integer[] getVisibleForValues() {
        return visibleForValues;
    }

    public Button getBtHelp() {
        return btHelp;
    }

    public TextField getTfUsername() {
        return tfUsername;
    }

    public TextField getTfSystemname() {
        return tfSystemname;
    }

    public TextField getTfSystemversion() {
        return tfSystemversion;
    }

    public ComboBox<Number> getCbVisibleFor() {
        return cbVisibleFor;
    }

    public ToggleSwitch getTsStartWa() {
        return tsStartWa;
    }

    public ToggleSwitch getTsShowWarning() {
        return tsShowWarning;
    }

    public ToggleSwitch getTsDirectSound() {
        return tsDirectSound;
    }

    public ToggleSwitch getTsBlockKde() {
        return tsBlockKde;
    }

    public TextField getTfExchangePartition() {
        return tfExchangePartition;
    }

    public ToggleSwitch getTsProxy() {
        return tsProxy;
    }

    public TextField getTfHost() {
        return tfHost;
    }

    public TextField getTfPort() {
        return tfPort;
    }

    public TextField getTfUser() {
        return tfUser;
    }

    public TextField getTfPwd() {
        return tfPwd;
    }    
    
    
}
