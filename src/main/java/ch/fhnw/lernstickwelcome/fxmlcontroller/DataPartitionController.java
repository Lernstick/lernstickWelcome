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
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.PasswordDialog;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import ch.fhnw.util.Partition;
import ch.fhnw.util.StorageDevice;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class for the standard version
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class DataPartitionController
        extends TitledPane
        implements Initializable {

    private static final Logger LOGGER
            = Logger.getLogger(DataPartitionController.class.getName());
    private static final ResourceBundle BUNDLE
            = ResourceBundle.getBundle("ch.fhnw.lernstickwelcome.Bundle");

    @FXML
    private VBox dataPartitionVBox;

    @FXML
    protected ToggleSwitch startWelcomeApplicationToggleSwitch;
    @FXML
    protected ToggleSwitch readOnlyWarningToggleSwitch;

    @FXML
    private GridPane encryptionGridPane;
    @FXML
    private Button changePersonalPasswordButton;
    @FXML
    private Button changeSecondaryPasswordButton;
    @FXML
    private Button deletePersonalPasswordButton;
    @FXML
    private Button deleteSecondaryPasswordButton;
    private Button addPersonalPasswordButton;
    private Button addSecondaryPasswordButton;

    private Partition dataPartition;

    public DataPartitionController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/ch/fhnw/lernstickwelcome/view/DataPartitionView.fxml"),
                BUNDLE);
        loader.setRoot(this);
        loader.setController(this);
        loader.setClassLoader(getClass().getClassLoader());

        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        addPersonalPasswordButton = new Button(BUNDLE.getString("Add"));
        addPersonalPasswordButton.setOnAction(t -> addPersonalPassword());

        addSecondaryPasswordButton = new Button(BUNDLE.getString("Add"));
        addSecondaryPasswordButton.setOnAction(t -> addSecondaryPassword());

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
                if (dataPartition.isLuksEncrypted()) {
                    updatePersonalPasswordGUI();
                    updateSecondaryPasswordGUI();
                } else {
                    LOGGER.info(
                            "data partition not encrypted, removing controls");
                    removeEncryptionControls();
                }
            }
        }
    }

    @FXML
    void changePersonalPassword(ActionEvent event) {
        PasswordDialog dialog = new PasswordDialog(
                "Change_Personal_Password", "Old_Password", "New_Password");
        changePassword(dialog, 0, "Personal_Password_Changed",
                "Error_Changing_Personal_Password");
    }

    @FXML
    void changeSecondaryPassword(ActionEvent event) {
        PasswordDialog dialog = new PasswordDialog(
                "Change_Secondary_Password", "Old_Password", "New_Password");
        changePassword(dialog, 1, "Secondary_Password_Changed",
                "Error_Changing_Secondary_Password");
    }

    @FXML
    void deletePersonalPassword(ActionEvent event) {
        PasswordDialog dialog = new PasswordDialog(
                "Replace_Personal_Password_With_Default", "Personal_Password");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent()
                && result.get().getButtonData() == ButtonData.OK_DONE) {

            String oldPassword = dialog.getOldPassword();

            boolean passwordChanged = false;
            try {
                passwordChanged = dataPartition.changeLuksPassword(
                        Partition.LuksSlots.PERSONAL.ordinal(),
                        oldPassword, Partition.DEFAULT_LUKS_PASSWORD);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "", ex);
            }

            Alert finalAlert;
            if (passwordChanged) {
                finalAlert = new Alert(AlertType.INFORMATION);
                finalAlert.setHeaderText(BUNDLE.getString(
                        "Personal_Password_Deleted"));
                showDeactivatedPersonalPasswordButtons();
            } else {
                finalAlert = new Alert(AlertType.ERROR);
                finalAlert.setHeaderText(BUNDLE.getString(
                        "Error_Deleting_Personal_Password"));
            }
            finalAlert.showAndWait();
        }
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
            Alert finalAlert;
            if (slotKilled) {
                finalAlert = new Alert(AlertType.INFORMATION);
                finalAlert.setHeaderText(BUNDLE.getString(
                        "Secondary_Password_Deleted"));
                showDeactivatedSecondaryPasswordButtons();
            } else {
                finalAlert = new Alert(AlertType.ERROR);
                finalAlert.setHeaderText(BUNDLE.getString(
                        "Error_Deleting_Secondary_Password"));
            }
            finalAlert.showAndWait();
        }
    }

    public ToggleSwitch getStartWelcomeApplicationToggleSwitch() {
        return startWelcomeApplicationToggleSwitch;
    }

    public ToggleSwitch getReadOnlyWarningToggleSwitch() {
        return readOnlyWarningToggleSwitch;
    }

    private void removeEncryptionControls() {
        dataPartitionVBox.getChildren().remove(encryptionGridPane);
    }

    private void changePassword(PasswordDialog dialog, int slot,
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

    private void addPersonalPassword() {

        PasswordDialog dialog = new PasswordDialog(
                "Add_Personal_Password", "Personal_Password");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent()
                && result.get().getButtonData() == ButtonData.OK_DONE) {

            String personalPassword = dialog.getOldPassword();

            try {
                if (dataPartition.changeLuksPassword(0,
                        Partition.DEFAULT_LUKS_PASSWORD, personalPassword)) {

                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setHeaderText(BUNDLE.getString(
                            "Personal_Password_Added"));
                    alert.showAndWait();
                    showActivatedPersonalPasswordButtons();

                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setHeaderText(BUNDLE.getString(
                            "Error_Adding_Secondary_Password"));
                    alert.showAndWait();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "", ex);
            }
        }
    }

    private void addSecondaryPassword() {

        boolean defaultLuksPasswordInUse = false;
        try {
            defaultLuksPasswordInUse = dataPartition.usesDefaultLuksPassword();
        } catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }

        if (defaultLuksPasswordInUse) {
            PasswordDialog dialog = new PasswordDialog(
                    "Add_Secondary_Password", "Secondary_Password");

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent()
                    && result.get().getButtonData() == ButtonData.OK_DONE) {

                String secondaryPassword = dialog.getOldPassword();

                try {
                    if (dataPartition.addSecondaryLuksPassword(
                            Partition.DEFAULT_LUKS_PASSWORD,
                            secondaryPassword)) {

                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setHeaderText(BUNDLE.getString(
                                "Secondary_Password_Added"));
                        alert.showAndWait();
                        showActivatedSecondaryPasswordButtons();

                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setHeaderText(BUNDLE.getString(
                                "Error_Adding_Secondary_Password"));
                        alert.showAndWait();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "", ex);
                }
            }

        } else {

            PasswordDialog dialog = new PasswordDialog("Add_Secondary_Password",
                    "Personal_Password", "Secondary_Password");

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent()
                    && result.get().getButtonData() == ButtonData.OK_DONE) {

                String personalPassword = dialog.getOldPassword();
                String secondaryPassword = dialog.getNewPassword();

                try {
                    if (dataPartition.addSecondaryLuksPassword(
                            personalPassword, secondaryPassword)) {

                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setHeaderText(BUNDLE.getString(
                                "Secondary_Password_Added"));
                        alert.showAndWait();
                        showActivatedSecondaryPasswordButtons();

                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setHeaderText(BUNDLE.getString(
                                "Error_Adding_Secondary_Password"));
                        alert.showAndWait();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "", ex);
                }
            }
        }
    }

    private void updatePersonalPasswordGUI() {
        try {
            if (dataPartition.usesDefaultLuksPassword()) {
                LOGGER.info("data partition encrypted with default password");
                showDeactivatedPersonalPasswordButtons();
            } else {
                LOGGER.info("data partition encrypted with custom password");
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
    }

    private void updateSecondaryPasswordGUI() {
        if (dataPartition.isSecondaryPasswordSet()) {
            LOGGER.info("secondary LUKS password is set");
            showActivatedSecondaryPasswordButtons();
        } else {
            LOGGER.info("secondary LUKS password is NOT set");
            showDeactivatedSecondaryPasswordButtons();
        }
    }

    private void showActivatedPersonalPasswordButtons() {
        if (encryptionGridPane.getChildren().contains(
                addPersonalPasswordButton)) {

            encryptionGridPane.getChildren().remove(addPersonalPasswordButton);
            encryptionGridPane.add(changePersonalPasswordButton, 1, 0);
            encryptionGridPane.add(deletePersonalPasswordButton, 2, 0);
        }
    }

    private void showActivatedSecondaryPasswordButtons() {
        if (encryptionGridPane.getChildren().contains(
                addSecondaryPasswordButton)) {

            encryptionGridPane.getChildren().remove(addSecondaryPasswordButton);
            encryptionGridPane.add(changeSecondaryPasswordButton, 1, 1);
            encryptionGridPane.add(deleteSecondaryPasswordButton, 2, 1);
        }
    }

    private void showDeactivatedPersonalPasswordButtons() {
        if (!encryptionGridPane.getChildren().contains(
                addPersonalPasswordButton)) {

            encryptionGridPane.getChildren().remove(
                    changePersonalPasswordButton);
            encryptionGridPane.getChildren().remove(
                    deletePersonalPasswordButton);
            encryptionGridPane.add(addPersonalPasswordButton, 1, 0);
        }
    }

    private void showDeactivatedSecondaryPasswordButtons() {
        if (!encryptionGridPane.getChildren().contains(
                addSecondaryPasswordButton)) {

            encryptionGridPane.getChildren().remove(
                    changeSecondaryPasswordButton);
            encryptionGridPane.getChildren().remove(
                    deleteSecondaryPasswordButton);
            encryptionGridPane.add(addSecondaryPasswordButton, 1, 1);
        }
    }
}
