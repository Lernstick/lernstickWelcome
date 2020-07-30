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

import ch.fhnw.lernstickwelcome.PasswordChangeDialog;
import ch.fhnw.lernstickwelcome.fxmlcontroller.AbstractSystemController;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import ch.fhnw.util.Partition;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * FXML Controller class for the standard version
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class StandardSystemController
        extends AbstractSystemController implements Initializable {

    private static final Logger LOGGER
            = Logger.getLogger(StandardSystemController.class.getName());
    private static final ResourceBundle BUNDLE
            = ResourceBundle.getBundle("ch.fhnw.lernstickwelcome.Bundle");

    private Partition dataPartition;

    @FXML
    private VBox dataPartitionVBox;
    @FXML
    private GridPane encryptionGridPane;
    @FXML
    private Button changePersonalPasswordButton;
    @FXML
    private Button changeSecondaryPasswordButton;
    @FXML
    private Button deleteSecondaryPasswordButton;

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

        try {
            initControls();

            proxyHostTextField.disableProperty().bind(
                    proxyToggleSwitch.selectedProperty().not());
            proxyPortTextField.disableProperty().bind(
                    proxyToggleSwitch.selectedProperty().not());
            proxyPasswordField.disableProperty().bind(
                    proxyToggleSwitch.selectedProperty().not());
            proxyUserTextField.disableProperty().bind(
                    proxyToggleSwitch.selectedProperty().not());
        } catch (DBusException | IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }

        StorageDevice systemStorageDevice
                = WelcomeModelFactory.getSystemStorageDevice();
        if (systemStorageDevice == null) {
            LOGGER.warning("system storage device not found, "
                    + "can't check data partition");
            removeEncryptionControls();
        } else {
            dataPartition = systemStorageDevice.getDataPartition();
            if (dataPartition == null) {
                LOGGER.warning(
                        "data partition not found, can't detect encryption");
                removeEncryptionControls();
            } else {
                if (!dataPartition.isLuksEncrypted()) {
                    removeEncryptionControls();
                }
                if (dataPartition.isSecondaryPasswordSet()) {
                    LOGGER.info("secondary LUKS password is set");
                } else {
                    LOGGER.info("secondary LUKS password is NOT set");
                    // TODO: change secondary password buttons
                }
            }
        }
    }

    @FXML
    void changePersonalPassword(ActionEvent event) {
        PasswordChangeDialog dialog = new PasswordChangeDialog(
                "Change_Personal_Password");
        changePassword(dialog, 0, "Personal_Password_Changed",
                "Error_Changing_Personal_Password");
    }

    @FXML
    void changeSecondaryPassword(ActionEvent event) {
        PasswordChangeDialog dialog = new PasswordChangeDialog(
                "Change_Secondary_Password");
        changePassword(dialog, 1, "Secondary_Password_Changed",
                "Error_Changing_Secondary_Password");
    }

    @FXML
    void deleteSecondaryPassword(ActionEvent event) {

        Alert alert = new Alert(AlertType.WARNING, null,
                ButtonType.OK, ButtonType.CANCEL);

        alert.setHeaderText(BUNDLE.getString(
                "Warning_Delete_Secondary_Password"));

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()
                && result.get().getButtonData() == ButtonData.OK_DONE) {

            boolean slotKilled = false;
            try {
                slotKilled = dataPartition.killSecondaryLuksSlot();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "", ex);
            }
            Alert finalAlert = null;
            if (slotKilled) {
                finalAlert = new Alert(AlertType.INFORMATION);
                finalAlert.setHeaderText(BUNDLE.getString(
                        "Secondary_Password_Deleted"));
            } else {
                finalAlert = new Alert(AlertType.ERROR);
                finalAlert.setHeaderText(BUNDLE.getString(
                        "Error_Deleting_Secondary_Password"));
            }
            finalAlert.showAndWait();
        }
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

    private void removeEncryptionControls() {
        dataPartitionVBox.getChildren().remove(encryptionGridPane);
    }

    private void changePassword(PasswordChangeDialog dialog, int slot,
            String successMessageKey, String errorMessageKey) {

        dialog.focusOldPassword();

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent()
                && result.get().getButtonData() == ButtonData.OK_DONE) {

            String oldPassword = dialog.getOldPassword();
            String newPassword = dialog.getNewPassword();
            try {
                if (dataPartition.changeLuksPassword(
                        slot, oldPassword, newPassword)) {

                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setHeaderText(BUNDLE.getString(successMessageKey));
                    alert.showAndWait();

                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setHeaderText(BUNDLE.getString(errorMessageKey));
                    alert.showAndWait();

                    // try again...
                    changePassword(dialog, slot,
                            successMessageKey, errorMessageKey);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "", ex);
            }
        }
    }
}
