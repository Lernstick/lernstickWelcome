/*
 * Copyright (C) 2017 FHNW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller.exam;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author root
 */
public class PasswordChangeController implements Initializable {

    @FXML
    private Button btCancel;
    @FXML
    private Button btOk;
    @FXML
    private PasswordField pfPassword;
    @FXML
    private PasswordField pfPasswordRepeat;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btOk.disableProperty().bind(pfPassword.textProperty().isEmpty().or(pfPasswordRepeat.textProperty().isEmpty()));
    }    
    
    public Button getBtOk() {
        return btOk;
    }
    
    public Button getBtCancel() {
        return btCancel;
    }
    
    public TextField getTxtPassword() {
        return pfPassword;
    }
    
    public TextField getTxtPasswordRepeat() {
        return pfPasswordRepeat;
    }
}
