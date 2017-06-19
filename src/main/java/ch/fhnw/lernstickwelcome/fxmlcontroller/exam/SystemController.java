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

import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author user
 */
public class SystemController implements Initializable {

    @FXML
    private Button helpButton;
    @FXML
    private TitledPane bootMenuTitledPane;
    @FXML
    private TextField systemNameTextField;
    @FXML
    private TextField systemVersionTextField;
    @FXML
    private ComboBox<Number> timeoutComboBox;
    @FXML
    private TitledPane userTitledPane;
    @FXML
    private TextField userNameTextField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField repeatPasswordField;
    @FXML
    private ToggleSwitch allowFileSystemsToggleSwitch;
    @FXML
    private ToggleSwitch blockKdeToggleSwitch;
    @FXML
    private TitledPane systemTitledPane;
    @FXML
    private ToggleSwitch directSoundToggleSwitch;
    @FXML
    private TitledPane partitionsTitledPane;
    @FXML
    private TextField exchangePartitionTextField;
    @FXML
    private ToggleSwitch userExchangeAccessToggleSwitch;
    @FXML
    private ToggleSwitch startWelcomeApplicationToggleSwitch;
    @FXML
    private ToggleSwitch readOnlyWarningToggleSwitch;

    private final Integer[] visibleForValues
            = new Integer[]{5, 10, 15, 20, 25, 30, 40, 50, 60};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timeoutComboBox.setConverter(new SecondStringConverter(rb));
        timeoutComboBox.setEditable(true);

        userNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isChangeUsernameAllowed(newValue)) {
                userNameTextField.setText(oldValue);
            }
        });

        exchangePartitionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            // only allow ASCII input
            if (!isASCII(newValue)) {
                exchangePartitionTextField.setText(oldValue);
                return;
            }

            if (getSpecialLength(newValue) <= 11) {
                exchangePartitionTextField.setText(newValue);
            } else {
                exchangePartitionTextField.setText(oldValue);
                Toolkit.getDefaultToolkit().beep();
            }
        });

        timeoutComboBox.getItems().addAll(visibleForValues);

        if (!WelcomeUtil.isImageWritable()) {
            timeoutComboBox.setVisible(false);
            systemNameTextField.setDisable(true);
            systemVersionTextField.setDisable(true);
        }
    }

    public TextField getSystemNameTextField() {
        return systemNameTextField;
    }

    public TextField getSystemVersionTextField() {
        return systemVersionTextField;
    }

    public TextField getUserNameTextField() {
        return userNameTextField;
    }

    public TextField getNewPasswordField() {
        return newPasswordField;
    }

    public TextField getRepeatPasswordField() {
        return repeatPasswordField;
    }

    public ComboBox<Number> getTimeoutComboBox() {
        return timeoutComboBox;
    }

    public ToggleSwitch getStartWelcomeApplicationToggleSwitch() {
        return startWelcomeApplicationToggleSwitch;
    }

    public ToggleSwitch getDirectSoundToggleSwitch() {
        return directSoundToggleSwitch;
    }

    public ToggleSwitch getBlockKdeToggleSwitch() {
        return blockKdeToggleSwitch;
    }

    public ToggleSwitch getAllowFileSystemsToggleSwitch() {
        return allowFileSystemsToggleSwitch;
    }

    public TextField getExchangePartitionTextField() {
        return exchangePartitionTextField;
    }

    public ToggleSwitch getUserExchangeAccessToggleSwitch() {
        return userExchangeAccessToggleSwitch;
    }

    public ToggleSwitch getReadOnlyWarningToggleSwitch() {
        return readOnlyWarningToggleSwitch;
    }

    public Button getHelpButton() {
        return helpButton;
    }

    public void showMediaAccessConfig() {
        bootMenuTitledPane.expandedProperty().set(false);
        userTitledPane.expandedProperty().set(true);
        systemTitledPane.expandedProperty().set(false);
        partitionsTitledPane.expandedProperty().set(false);

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    highlight();
                }
            }

            private void highlight() {
                try {
                    TimeUnit.MILLISECONDS.sleep(150);

                    Platform.runLater(() -> {
                        int depth = 70;
                        DropShadow borderGlow = new DropShadow();
                        borderGlow.setOffsetY(0f);
                        borderGlow.setOffsetX(0f);
                        borderGlow.setColor(Color.RED);
                        borderGlow.setWidth(depth);
                        borderGlow.setHeight(depth);
                        allowFileSystemsToggleSwitch.setEffect(borderGlow);
                    });

                    TimeUnit.MILLISECONDS.sleep(150);

                    Platform.runLater(() -> {
                        allowFileSystemsToggleSwitch.setEffect(null);
                    });
                } catch (InterruptedException ex) {
                    // ignored...
                }
            }

        }.start();
    }

    private boolean isChangeUsernameAllowed(String string) {
        if ((string != null) && string.chars().anyMatch(c
                -> (c == ':') || (c == ',') || (c == '='))) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        return true;
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

    private static class SecondStringConverter extends StringConverter<Number> {

        String seconds;
        String second;

        public SecondStringConverter(ResourceBundle rb) {
            if (rb != null) {
                seconds = rb.getString("welcomeApplicationSystem.seconds");
                second = rb.getString("welcomeApplicationSystem.second");
            } else {
                seconds = "";
                second = "";
            }
        }

        @Override
        public String toString(Number t) {
            return t.intValue() + " " + (t.intValue() == 1 ? second : seconds);
        }

        @Override
        public Number fromString(String string) {
            return Integer.valueOf(string.split(" ")[0]);
        }
    }
}
