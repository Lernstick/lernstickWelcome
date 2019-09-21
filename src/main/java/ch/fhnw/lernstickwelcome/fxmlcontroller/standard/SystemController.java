/*
 * Copyright (C) 2019 Ronny Standtke <ronny.standtke@gmx.net>
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
package ch.fhnw.lernstickwelcome.fxmlcontroller.standard;

import ch.fhnw.lernstickwelcome.fxmlcontroller.AbstractSystemController;
import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML Controller class for the standard version
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class SystemController
        extends AbstractSystemController implements Initializable {

    @FXML
    private ToggleSwitch proxyToggleSwitch;
    @FXML
    private TextField proxyHostTextField;
    @FXML
    private TextField proxyPortTextField;
    @FXML
    private TextField proxyUserTextField;
    @FXML
    private PasswordField proxyPasswordField;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        initControls();

        proxyHostTextField.disableProperty().bind(proxyToggleSwitch.selectedProperty().not());
        proxyPortTextField.disableProperty().bind(proxyToggleSwitch.selectedProperty().not());
        proxyPasswordField.disableProperty().bind(proxyToggleSwitch.selectedProperty().not());
        proxyUserTextField.disableProperty().bind(proxyToggleSwitch.selectedProperty().not());
    }

    public ToggleSwitch getProxyToggleSwitch() {
        return proxyToggleSwitch;
    }

    public TextField getProxyHostTextField() {
        return proxyHostTextField;
    }

    public TextField getProxyPortTextField() {
        return proxyPortTextField;
    }

    public TextField getProxyUserTextField() {
        return proxyUserTextField;
    }

    public PasswordField getProxyPasswordField() {
        return proxyPasswordField;
    }
}
