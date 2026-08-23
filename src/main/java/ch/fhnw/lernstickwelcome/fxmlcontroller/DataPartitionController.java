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
    private Button changeDuressPasswordButton;
    @FXML
    private Button deletePersonalPasswordButton;
    @FXML
    private Button deleteSecondaryPasswordButton;
    @FXML
    private Button deleteDuressPasswordButton;
    private Button addPersonalPasswordButton;
    private Button addSecondaryPasswordButton;
    private Button addDuressPasswordButton;

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

        addDuressPasswordButton = new Button(BUNDLE.getString("Add"));
        addDuressPasswordButton.setOnAction(t -> addDuressPassword());

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
                    updateDuressPasswordGUI();
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
        changePassword(dialog, Partition.LuksSlots.PERSONAL.ordinal(),
                "Personal_Password_Changed",
                "Error_Changing_Personal_Password");
    }

    @FXML
    void changeSecondaryPassword(ActionEvent event) {
        PasswordDialog dialog = new PasswordDialog(
                "Change_Secondary_Password", "Old_Password", "New_Password");
        changePassword(dialog, Partition.LuksSlots.SECONDARY.ordinal(),
                "Secondary_Password_Changed",
                "Error_Changing_Secondary_Password");
    }

    @FXML
    void changeDuressPassword(ActionEvent event) {
        PasswordDialog dialog = new PasswordDialog(
                "Change_Duress_Password", "Old_Password", "New_Password");
        changePassword(dialog, Partition.LuksSlots.DURESS.ordinal(),
                "Duress_Password_Changed",
                "Error_Changing_Duress_Password");
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

    @FXML
    void deleteDuressPassword(ActionEvent event) {

        Alert alert = new Alert(AlertType.WARNING, null,
                ButtonType.OK, ButtonType.CANCEL);

        alert.setHeaderText(BUNDLE.getString(
                "Warning_Delete_Duress_Password"));

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()
                && result.get().getButtonData() == ButtonData.OK_DONE) {

            boolean slotKilled = false;
            try {
                slotKilled = dataPartition.killDuressLuksSlot();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "", ex);
            }
            Alert finalAlert;
            if (slotKilled) {
                finalAlert = new Alert(AlertType.INFORMATION);
                finalAlert.setHeaderText(BUNDLE.getString(
                        "Duress_Password_Deleted"));
                showDeactivatedDuressPasswordButtons();
            } else {
                finalAlert = new Alert(AlertType.ERROR);
                finalAlert.setHeaderText(BUNDLE.getString(
                        "Error_Deleting_Duress_Password"));
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
                addSecondaryPasswordAndShowResult(
                        Partition.DEFAULT_LUKS_PASSWORD,
                        dialog.getOldPassword());
            }

        } else {

            PasswordDialog dialog = new PasswordDialog("Add_Secondary_Password",
                    "Personal_Password", "Secondary_Password");

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent()
                    && result.get().getButtonData() == ButtonData.OK_DONE) {
                addSecondaryPasswordAndShowResult(
                        dialog.getOldPassword(),
                        dialog.getNewPassword());
            }
        }
    }

    private void addSecondaryPasswordAndShowResult(
            String existingPassword, String secondaryPassword) {

        try {
            if (dataPartition.addSecondaryLuksPassword(
                    existingPassword, secondaryPassword)) {

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

    private void addDuressPassword() {

        boolean defaultLuksPasswordInUse = false;
        try {
            defaultLuksPasswordInUse = dataPartition.usesDefaultLuksPassword();
        } catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }

        if (defaultLuksPasswordInUse) {
            PasswordDialog dialog = new PasswordDialog(
                    "Add_Duress_Password", "Duress_Password");

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent()
                    && result.get().getButtonData() == ButtonData.OK_DONE) {
                addDuressPasswordAndShowResult(
                        Partition.DEFAULT_LUKS_PASSWORD,
                        dialog.getOldPassword());
            }

        } else {

            PasswordDialog dialog = new PasswordDialog("Add_Duress_Password",
                    "Personal_Password", "Duress_Password");

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent()
                    && result.get().getButtonData() == ButtonData.OK_DONE) {

                addDuressPasswordAndShowResult(
                        dialog.getOldPassword(),
                        dialog.getNewPassword());
            }
        }
    }

    private void addDuressPasswordAndShowResult(
            String existingPassword, String duressPassword) {

        try {
            if (dataPartition.addDuressLuksPassword(
                    existingPassword, duressPassword)) {

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText(BUNDLE.getString(
                        "Duress_Password_Added"));
                alert.showAndWait();
                showActivatedDuressPasswordButtons();

            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(BUNDLE.getString(
                        "Error_Adding_Duress_Password"));
                alert.showAndWait();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "", ex);
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

    private void updateDuressPasswordGUI() {
        if (dataPartition.isDuressPasswordSet()) {
            LOGGER.info("duress LUKS password is set");
            showActivatedDuressPasswordButtons();
        } else {
            LOGGER.info("duress LUKS password is NOT set");
            showDeactivatedDuressPasswordButtons();
        }
    }

    private void showActivatedPersonalPasswordButtons() {
        showActivatedPasswordButtons(
                addPersonalPasswordButton,
                changePersonalPasswordButton,
                deletePersonalPasswordButton, 0);
    }

    private void showActivatedSecondaryPasswordButtons() {
        showActivatedPasswordButtons(
                addSecondaryPasswordButton,
                changeSecondaryPasswordButton,
                deleteSecondaryPasswordButton, 1);
    }

    private void showActivatedDuressPasswordButtons() {
        showActivatedPasswordButtons(
                addDuressPasswordButton,
                changeDuressPasswordButton,
                deleteDuressPasswordButton, 2);
    }

    private void showActivatedPasswordButtons(Button addPasswordButton,
            Button changePasswordButton, Button deletePasswordButton, int row) {
        if (encryptionGridPane.getChildren().contains(addPasswordButton)) {
            encryptionGridPane.getChildren().remove(addPasswordButton);
            encryptionGridPane.add(changePasswordButton, 1, row);
            encryptionGridPane.add(deletePasswordButton, 2, row);
        }
    }

    private void showDeactivatedPersonalPasswordButtons() {
        showDeactivatedPasswordButtons(
                addPersonalPasswordButton,
                changePersonalPasswordButton,
                deletePersonalPasswordButton, 0);
    }

    private void showDeactivatedSecondaryPasswordButtons() {
        showDeactivatedPasswordButtons(
                addSecondaryPasswordButton,
                changeSecondaryPasswordButton,
                deleteSecondaryPasswordButton, 1);
    }

    private void showDeactivatedDuressPasswordButtons() {
        showDeactivatedPasswordButtons(
                addDuressPasswordButton,
                changeDuressPasswordButton,
                deleteDuressPasswordButton, 2);
    }

    private void showDeactivatedPasswordButtons(Button addPasswordButton,
            Button changePasswordButton, Button deletePasswordButton, int row) {
        if (!encryptionGridPane.getChildren().contains(addPasswordButton)) {
            encryptionGridPane.getChildren().remove(changePasswordButton);
            encryptionGridPane.getChildren().remove(deletePasswordButton);
            encryptionGridPane.add(addPasswordButton, 1, row);
        }
    }
}
