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

import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import ch.fhnw.util.Partition;
import ch.fhnw.util.StorageDevice;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.StringConverter;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * An abstract base class for the standard and exam system controllers
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class AbstractSystemController {

    private static final Logger LOGGER
            = Logger.getLogger(AbstractSystemController.class.getName());
    private static final ResourceBundle BUNDLE
            = ResourceBundle.getBundle("ch.fhnw.lernstickwelcome.Bundle");
    private final Integer[] timeoutValues
            = new Integer[]{5, 10, 15, 20, 25, 30, 40, 50, 60};

    @FXML
    protected Button helpButton;
    @FXML
    protected RadioButton oldBootLoaderRadioButton;
    @FXML
    protected RadioButton newBootLoaderRadioButton;
    @FXML
    protected TextFlow oldVersionTextFlow;
    @FXML
    protected TextFlow newVersionTextFlow;
    @FXML
    protected TextField systemNameTextField;
    @FXML
    protected TextField systemVersionTextField;
    @FXML
    protected ComboBox<Number> timeoutComboBox;
    @FXML
    protected TextField userNameTextField;
    @FXML
    protected TextField exchangePartitionLabelTextField;
    @FXML
    protected ToggleSwitch startWelcomeApplicationToggleSwitch;
    @FXML
    protected ToggleSwitch readOnlyWarningToggleSwitch;

    public Button getHelpButton() {
        return helpButton;
    }

    public RadioButton getNewBootLoaderRadioButton() {
        return newBootLoaderRadioButton;
    }

    public TextField getSystemNameTextField() {
        return systemNameTextField;
    }

    public TextField getSystemVersionTextField() {
        return systemVersionTextField;
    }

    public ComboBox<Number> getTimeoutComboBox() {
        return timeoutComboBox;
    }

    public TextField getUserNameTextField() {
        return userNameTextField;
    }

    public TextField getExchangePartitionLabelTextField() {
        return exchangePartitionLabelTextField;
    }

    public ToggleSwitch getStartWelcomeApplicationToggleSwitch() {
        return startWelcomeApplicationToggleSwitch;
    }

    public ToggleSwitch getReadOnlyWarningToggleSwitch() {
        return readOnlyWarningToggleSwitch;
    }

    public void initControls() throws DBusException, IOException {

        Text oldText = new Text(
                BUNDLE.getString("BootLoader_Old_Version") + "\n");
        Text oldAdvantageText = new Text(
                BUNDLE.getString("BootLoader_Old_Version_Advantage") + "\n");
        oldAdvantageText.setFill(Color.GREEN);
        Text oldDisadvantageText = new Text(
                BUNDLE.getString("BootLoader_Old_Version_Disadvantage"));
        oldDisadvantageText.setFill(Color.RED);
        oldVersionTextFlow.getChildren().addAll(
                oldText, oldAdvantageText, oldDisadvantageText);

        Text newText = new Text(
                BUNDLE.getString("BootLoader_New_Version") + "\n");
        Text newAdvantageText = new Text(
                BUNDLE.getString("BootLoader_New_Version_Advantage") + "\n");
        newAdvantageText.setFill(Color.GREEN);
        Text newDisadvantageText = new Text(
                BUNDLE.getString("BootLoader_New_Version_Disadvantage"));
        newDisadvantageText.setFill(Color.RED);
        newVersionTextFlow.getChildren().addAll(
                newText, newAdvantageText, newDisadvantageText);

        StorageDevice systemStorageDevice
                = WelcomeModelFactory.getSystemStorageDevice();
        if (systemStorageDevice == null) {
            LOGGER.warning("system storage device not found, "
                    + "can't detect which boot loader is selected");
            disableBootLoaderRadioButtons();
        } else {
            Partition efiPartition = systemStorageDevice.getEfiPartition();
            if (efiPartition == null) {
                LOGGER.warning("EFI partition not found, "
                        + "can't detect which boot loader is selected");
                disableBootLoaderRadioButtons();
            } else {
                String efiMountPath = efiPartition.mount().getMountPath();
                Path currentShimPath = Paths.get(efiMountPath,
                        "EFI/boot/bootx64.efi");
                Path newShimPath = Paths.get(efiMountPath,
                        "EFI/boot/bootx64.efi.new");
                if (Files.size(currentShimPath) == Files.size(newShimPath)) {
                    LOGGER.info("new boot loader is active");
                    newBootLoaderRadioButton.selectedProperty().set(true);
                } else {
                    LOGGER.info("old boot loader is active");
                    oldBootLoaderRadioButton.selectedProperty().set(true);
                }
            }
        }

        timeoutComboBox.setConverter(new SecondStringConverter());
        timeoutComboBox.setEditable(true);
        timeoutComboBox.getItems().addAll(timeoutValues);

        userNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isChangeUsernameAllowed(newValue)) {
                userNameTextField.setText(oldValue);
            }
        });

        exchangePartitionLabelTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            // only allow ASCII input
            if (!isASCII(newValue)) {
                exchangePartitionLabelTextField.setText(oldValue);
                return;
            }

            if (getSpecialLength(newValue) <= 11) {
                exchangePartitionLabelTextField.setText(newValue);
            } else {
                exchangePartitionLabelTextField.setText(oldValue);
                Toolkit.getDefaultToolkit().beep();
            }
        });

        if (!WelcomeUtil.isImageWritable()) {
            timeoutComboBox.setVisible(false);
            systemNameTextField.setDisable(true);
            systemVersionTextField.setDisable(true);
        }
    }

    private void disableBootLoaderRadioButtons() {
        oldBootLoaderRadioButton.setDisable(true);
        oldVersionTextFlow.setDisable(true);
        newBootLoaderRadioButton.setDisable(true);
        newVersionTextFlow.setDisable(true);
    }

    private static int getSpecialLength(String string) {
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

    private static boolean isASCII(String string) {
        for (int i = 0, length = string.length(); i < length; i++) {
            char character = string.charAt(i);
            if ((character < 0) || (character > 127)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isChangeUsernameAllowed(String string) {
        if ((string != null) && string.chars().anyMatch(
                c -> (c == ':') || (c == ',') || (c == '='))) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        return true;
    }

    private static class SecondStringConverter
            extends StringConverter<Number> {

        String second = BUNDLE.getString("welcomeApplicationSystem.second");
        String seconds = BUNDLE.getString("welcomeApplicationSystem.seconds");

        @Override
        public String toString(Number number) {
            return number.intValue() + " "
                    + (number.intValue() == 1 ? second : seconds);
        }

        @Override
        public Number fromString(String string) {
            return Integer.valueOf(string.split(" ")[0]);
        }
    }
}
