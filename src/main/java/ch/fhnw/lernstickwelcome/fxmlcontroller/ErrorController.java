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
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class ErrorController implements Initializable {

    private static final Logger LOGGER
            = Logger.getLogger(ErrorController.class.getName());

    @FXML
    private Label titleLabel;
    @FXML
    private Label messageLabel;

    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public void initErrorMessage(Throwable throwable) {
        
        if (throwable instanceof ProcessingException) {
            titleLabel.setText(resourceBundle.getString(
                    "welcomeApplicationError.saveStopped"));
            messageLabel.setText(MessageFormat.format(
                    resourceBundle.getString(throwable.getMessage()),
                    ((ProcessingException) throwable).getMessageDetails()));
            LOGGER.log(Level.INFO,
                    "Error Dialog shown for ProcessingException", throwable);
        } else {
            titleLabel.setText(resourceBundle.getString(
                    "welcomeApplicationError.unknownException"));
            messageLabel.setText(resourceBundle.getString(
                    "welcomeApplicationError.unknownExceptionMessage"));
            LOGGER.log(Level.SEVERE,
                    "Error Dialog shown for unexpected Exception", throwable);
        }
    }

    @FXML
    private void okButtonAction(ActionEvent event) {
        ((Node) event.getSource()).getScene().getWindow().hide();
    }
}
