/*
 * Copyright (C) 2020 Ronny Standtke <ronny.standtke@gmx.net>
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
package ch.fhnw.lernstickwelcome;

import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 *
 * A custom password change dialog
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class PasswordDialog extends Alert {

    private static final ResourceBundle BUNDLE
            = ResourceBundle.getBundle("ch.fhnw.lernstickwelcome.Bundle");

    private final PasswordField oldPasswordField;
    private final TextField oldPasswordTextField;
    private final PasswordField newPasswordField;

    public PasswordDialog(String titleKey,
            String oldPasswordKey, String newPasswordKey) {

        super(AlertType.NONE);

        setTitle(BUNDLE.getString(titleKey));
        setHeaderText(BUNDLE.getString(titleKey));

        setGraphic(new ImageView(this.getClass().getResource(
                "/icon/dialog-password.png").toString()));
        ButtonType changeButtonType = new ButtonType(BUNDLE.getString(titleKey),
                ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(
                changeButtonType, ButtonType.CANCEL);

        // Create the password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label(BUNDLE.getString(oldPasswordKey)), 0, 0);
        oldPasswordField = new PasswordField();
        oldPasswordField.setMaxWidth(Double.MAX_VALUE);
        grid.add(oldPasswordField, 1, 0);
        oldPasswordTextField = new TextField();
        oldPasswordTextField.textProperty().bindBidirectional(
                oldPasswordField.textProperty());

        ToggleButton showOldPasswordButton = new ToggleButton();
        showOldPasswordButton.setFocusTraversable(false);
        showOldPasswordButton.setGraphic(new ImageView(
                this.getClass().getResource(
                        "/icon/password-show-on.png").toString()));
        showOldPasswordButton.setOnAction((t) -> {
            if (showOldPasswordButton.isSelected()) {
                grid.getChildren().remove(oldPasswordField);
                oldPasswordField.setVisible(false);
                oldPasswordTextField.setVisible(true);
                grid.add(oldPasswordTextField, 1, 0);
                showOldPasswordButton.setGraphic(new ImageView(
                        this.getClass().getResource(
                                "/icon/password-show-off.png").toString()));
            } else {
                grid.getChildren().remove(oldPasswordTextField);
                oldPasswordTextField.setVisible(false);
                oldPasswordField.setVisible(true);
                grid.add(oldPasswordField, 1, 0);
                showOldPasswordButton.setGraphic(new ImageView(
                        this.getClass().getResource(
                                "/icon/password-show-on.png").toString()));
            }
        });
        grid.add(showOldPasswordButton, 2, 0);

        grid.add(new Label(BUNDLE.getString(newPasswordKey)), 0, 1);
        newPasswordField = new PasswordField();
        newPasswordField.setMaxWidth(Double.MAX_VALUE);
        grid.add(newPasswordField, 1, 1);
        TextField newPasswordTextField = new TextField();
        newPasswordTextField.textProperty().bindBidirectional(
                newPasswordField.textProperty());

        ToggleButton showNewPasswordButton = new ToggleButton();
        showNewPasswordButton.setFocusTraversable(false);
        showNewPasswordButton.setGraphic(new ImageView(
                this.getClass().getResource(
                        "/icon/password-show-on.png").toString()));
        showNewPasswordButton.setOnAction((t) -> {
            if (showNewPasswordButton.isSelected()) {
                grid.getChildren().remove(newPasswordField);
                newPasswordField.setVisible(false);
                newPasswordTextField.setVisible(true);
                grid.add(newPasswordTextField, 1, 1);
                newPasswordTextField.setFocusTraversable(true);
                showNewPasswordButton.setGraphic(new ImageView(
                        this.getClass().getResource(
                                "/icon/password-show-off.png").toString()));
            } else {
                grid.getChildren().remove(newPasswordTextField);
                newPasswordTextField.setVisible(false);
                newPasswordField.setVisible(true);
                grid.add(newPasswordField, 1, 1);
                showNewPasswordButton.setGraphic(new ImageView(
                        this.getClass().getResource(
                                "/icon/password-show-on.png").toString()));
            }
        });
        grid.add(showNewPasswordButton, 2, 1);

        grid.getColumnConstraints().add(new ColumnConstraints());
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().add(columnConstraints);

        EventHandler<ActionEvent> switchToNewPasswordHandler = (t) -> {
            if (newPasswordField.isVisible()) {
                newPasswordField.requestFocus();
            } else {
                newPasswordTextField.requestFocus();
            }
        };
        oldPasswordField.setOnAction(switchToNewPasswordHandler);
        oldPasswordTextField.setOnAction(switchToNewPasswordHandler);

        // Enable/Disable login button depending on whether old and new
        // passwords were entered.
        Node changeButton = getDialogPane().lookupButton(changeButtonType);
        changeButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> oldPasswordField.getText().isEmpty()
                || newPasswordField.getText().isEmpty(),
                oldPasswordField.textProperty(),
                newPasswordField.textProperty()));

        getDialogPane().setContent(grid);
    }

    public void focusOldPassword() {
        Platform.runLater(() -> {
            if (oldPasswordField.isVisible()) {
                oldPasswordField.requestFocus();
            } else {
                oldPasswordTextField.requestFocus();
            }
        });
    }

    public String getOldPassword() {
        return oldPasswordField.getText();
    }

    public String getNewPassword() {
        return newPasswordField.getText();
    }
}
