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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 */
public class PasswordChangeController implements Initializable {

    @FXML
    private Button cancelButton;
    @FXML
    private Button okButton;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField repeatPasswordField;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        okButton.disableProperty().bind(
                newPasswordField.textProperty().isEmpty().or(
                        repeatPasswordField.textProperty().isEmpty()));

        Platform.runLater(() -> {
            newPasswordField.requestFocus();
        });
    }

    @FXML
    void newPasswordFieldAction(ActionEvent event) {
        repeatPasswordField.requestFocus();
    }

    public Button getOkButton() {
        return okButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public TextField getNewPasswordField() {
        return newPasswordField;
    }

    public TextField getRepeatPasswordField() {
        return repeatPasswordField;
    }
}
