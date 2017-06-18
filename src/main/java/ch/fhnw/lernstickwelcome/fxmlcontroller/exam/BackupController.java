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

import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author user
 */
public class BackupController implements Initializable {

    private final static Integer[] BACKUP_FREQUENCIES
            = new Integer[]{1, 2, 3, 4, 5, 10, 15, 30, 60};

    @FXML
    private Button helpButton;
    @FXML
    private ToggleSwitch backupToggleSwitch;
    @FXML
    private Label frequencyLabel;
    @FXML
    private ComboBox<Number> frequencyComboBox;
    @FXML
    private Label screenshotsLabel;
    @FXML
    private ToggleSwitch screenshotsToggleSwitch;
    @FXML
    private TitledPane sourceTitledPane;
    @FXML
    private TextField sourceTextField;
    @FXML
    private Button sourceButton;
    @FXML
    private TitledPane destinationsTitledPane;
    @FXML
    private ToggleSwitch localToggleSwitch;
    @FXML
    private Label destinationFolderLabel;
    @FXML
    private TextField destinationFolderTextField;
    @FXML
    private Button destinationFolderButton;
    @FXML
    private ToggleSwitch externalPartitionToggleSwitch;
    @FXML
    private Label externalPartitionLabel;
    @FXML
    private TextField externalPartitionTextField;
    @FXML
    private Button externalPartitionButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        frequencyComboBox.setConverter(new MinutesStringConverter(rb));
        frequencyComboBox.getItems().addAll(BACKUP_FREQUENCIES);

        bindDisabledPropertyToBackupToggleSwitch(frequencyLabel);
        bindDisabledPropertyToBackupToggleSwitch(frequencyComboBox);
        bindDisabledPropertyToBackupToggleSwitch(screenshotsLabel);
        bindDisabledPropertyToBackupToggleSwitch(screenshotsToggleSwitch);
        bindDisabledPropertyToBackupToggleSwitch(sourceTitledPane);
        bindDisabledPropertyToBackupToggleSwitch(destinationsTitledPane);

        bindDisabledPropertyToLocalToggleSwitch(destinationFolderLabel);
        bindDisabledPropertyToLocalToggleSwitch(destinationFolderTextField);
        bindDisabledPropertyToLocalToggleSwitch(destinationFolderButton);

        bindDisabledPropertyToExternalToggleSwitch(externalPartitionLabel);
        bindDisabledPropertyToExternalToggleSwitch(externalPartitionTextField);
        bindDisabledPropertyToExternalToggleSwitch(externalPartitionButton);
    }

    @FXML
    private void onClickSetBackupPath(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Backup Path");
        File file = chooser.showDialog(new Stage());

        if (file != null) {
            sourceTextField.textProperty().set(file.getPath());
        }
    }

    @FXML
    private void onClickSetRemotePartition(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Remote Partition");
        File file = chooser.showDialog(new Stage());

        if (file != null) {
            externalPartitionTextField.setText(file.getPath());
        }
    }

    @FXML
    private void onClickSetLocalFolder(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Set Local Folder");
        File file = chooser.showDialog(new Stage());

        if (file != null) {
            destinationFolderTextField.setText(file.getPath());
        }
    }

    public Button getHelpButton() {
        return helpButton;
    }

    public ToggleSwitch getBackupToggleSwitch() {
        return backupToggleSwitch;
    }

    public ToggleSwitch getScreenshotsToggleSwitch() {
        return screenshotsToggleSwitch;
    }

    public TextField getSourceTextField() {
        return sourceTextField;
    }

    public Button getSourceButton() {
        return sourceButton;
    }

    public TextField getDestinationFolderTextField() {
        return destinationFolderTextField;
    }

    public Button getDestinationFolderButton() {
        return destinationFolderButton;
    }

    public ToggleSwitch getLocalToggleSwitch() {
        return localToggleSwitch;
    }

    public TextField getExternalPartitionTextField() {
        return externalPartitionTextField;
    }

    public Button getExternalPartitionButton() {
        return externalPartitionButton;
    }

    public ToggleSwitch getExternalPartitionToggleSwitch() {
        return externalPartitionToggleSwitch;
    }

    public ComboBox<Number> getFrequencyCombobox() {
        return frequencyComboBox;
    }

    private void bindDisabledPropertyToBackupToggleSwitch(Node node) {
        bindDisabledPropertyToToggleSwitch(node, backupToggleSwitch);
    }

    private void bindDisabledPropertyToLocalToggleSwitch(Node node) {
        bindDisabledPropertyToToggleSwitch(node, localToggleSwitch);
    }

    private void bindDisabledPropertyToExternalToggleSwitch(Node node) {
        bindDisabledPropertyToToggleSwitch(node, externalPartitionToggleSwitch);
    }

    private void bindDisabledPropertyToToggleSwitch(
            Node node, ToggleSwitch toggleSwitch) {
        node.disableProperty().bind(toggleSwitch.selectedProperty().not());
    }

    private static class MinutesStringConverter
            extends StringConverter<Number> {

        String minutes;

        private MinutesStringConverter(ResourceBundle rb) {
            if (rb != null) {
                minutes = rb.getString("welcomeApplicationBackup.minutes");
            } else {
                minutes = "";
            }
        }

        @Override
        public String toString(Number v) {
            return v.intValue() + " " + minutes;
        }

        @Override
        public Number fromString(String string) {
            return Integer.valueOf(string.split(" ")[0]);
        }
    }
}
