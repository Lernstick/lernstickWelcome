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
package ch.fhnw.lernstickwelcome.fxmlcontroller.exam;

import ch.fhnw.lernstickwelcome.fxmlcontroller.AbstractSystemController;
import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * FXML Controller class for the exam version
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class ExamSystemController
        extends AbstractSystemController implements Initializable {

    private static final Logger LOGGER
            = Logger.getLogger(ExamSystemController.class.getName());

    @FXML
    private TitledPane bootLoaderTitledPane;
    @FXML
    private TitledPane bootMenuTitledPane;
    @FXML
    private TitledPane userTitledPane;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField repeatPasswordField;
    @FXML
    private ToggleSwitch allowFileSystemsToggleSwitch;
    @FXML
    private TitledPane partitionsTitledPane;
    @FXML
    private ToggleSwitch userExchangeAccessToggleSwitch;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initControls();
        } catch (DBusException | IOException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }
    }

    public TextField getNewPasswordField() {
        return newPasswordField;
    }

    public TextField getRepeatPasswordField() {
        return repeatPasswordField;
    }

    public ToggleSwitch getAllowFileSystemsToggleSwitch() {
        return allowFileSystemsToggleSwitch;
    }

    public ToggleSwitch getUserExchangeAccessToggleSwitch() {
        return userExchangeAccessToggleSwitch;
    }

    public void showMediaAccessConfig() {
        bootLoaderTitledPane.expandedProperty().set(false);
        bootMenuTitledPane.expandedProperty().set(false);
        userTitledPane.expandedProperty().set(true);
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
}
