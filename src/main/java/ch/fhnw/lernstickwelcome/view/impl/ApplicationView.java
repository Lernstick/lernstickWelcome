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
package ch.fhnw.lernstickwelcome.view.impl;

import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * This class represents an application in the GUI.
 *
 * @author sschw
 */
public class ApplicationView extends GridPane {

    private final Label title = new Label();
    private final Label description = new Label();
    private final ImageView icon = new ImageView();
    private final Button help = new Button();
    private final Label installDescription = new Label();
    private final ToggleSwitch toggleSwitch = new ToggleSwitch();
    private final BooleanProperty installed = new SimpleBooleanProperty();

    public ApplicationView(ResourceBundle rb) {
        super();
        init(rb);
    }

    private void init(ResourceBundle rb) {
        setVgap(10);
        setHgap(10);

        help.setVisible(false);
        help.getStyleClass().add("btn_help");
        help.setMinHeight(30.0);
        help.setMinWidth(30.0);

        title.setAlignment(Pos.CENTER_LEFT);
        title.setFont(Font.font(null, FontWeight.BOLD, 14));
        title.setPadding(new Insets(0, 0, 0, 10));
        description.setAlignment(Pos.CENTER_LEFT);
        description.setFont(Font.font(null, FontWeight.NORMAL, 13));
        description.setPadding(new Insets(0, 0, 0, 10));
        installDescription.setMinWidth(80);
        installDescription.setAlignment(Pos.BOTTOM_CENTER);

        add(icon, 0, 0, 1, 2);
        add(title, 1, 0, 1, 2);
        add(help, 2, 0, 1, 2);
        add(installDescription, 3, 0);
        add(toggleSwitch, 3, 1);

        setValignment(help, VPos.CENTER);
        setValignment(installDescription, VPos.BOTTOM);
        setValignment(toggleSwitch, VPos.TOP);

        setHgrow(title, Priority.ALWAYS);
        setHgrow(description, Priority.ALWAYS);
        setHgrow(icon, Priority.NEVER);
        setHgrow(help, Priority.NEVER);
        setHgrow(installDescription, Priority.NEVER);
        setHgrow(toggleSwitch, Priority.NEVER);

        setPadding(new Insets(10, 5, 10, 5));

        installDescription.setText(rb.getString("ApplicationView.install"));
        installed.addListener((observable, oldValue, newValue) -> {
            installDescription.setText(rb.getString(newValue
                    ? "ApplicationView.installed"
                    : "ApplicationView.install"));
        });
        toggleSwitch.disableProperty().bind(installed);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setDescription(String description) {
        this.description.setText(description);
        setRowSpan(title, 1);
        add(this.description, 1, 1);
    }

    public void setIcon(Image icon) {
        this.icon.setImage(icon);
    }

    public void setHelpAction(EventHandler<ActionEvent> evt) {
        help.setOnAction(evt);
        help.setVisible(true);
    }

    public BooleanProperty installedProperty() {
        return installed;
    }

    public BooleanProperty installingProperty() {
        return toggleSwitch.selectedProperty();
    }
}
