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

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * A simple toggle switch.
 * <br>
 * Optimized for the use in a fxml file.
 * 
 * @author TheItachiUchiha & sschw
 */
public class ToggleSwitch extends HBox {

    private final Label label = new Label();
    private final Button button = new Button();

    private SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    private SimpleStringProperty textOn = new SimpleStringProperty();
    private SimpleStringProperty textOff = new SimpleStringProperty();
    
    /**
     * Initializes a toggle switch.
     */
    public ToggleSwitch() {
        init();
        selected.addListener((a, b, c) -> {
            if (c) {
                label.textProperty().unbind();
                label.textProperty().bind(this.textOn);
                label.setStyle("-fx-background-color: green; -fx-text-fill:black; -fx-background-radius: 4 0 0 4;");
                button.setStyle("-fx-text-fill:black; -fx-background-radius: 0 4 4 0;");
                button.toFront();
            } else {
                label.textProperty().unbind();
                label.textProperty().bind(this.textOff);
                label.setStyle("-fx-background-color: grey; -fx-text-fill:black; -fx-background-radius: 0 4 4 0;");
                button.setStyle("-fx-text-fill:black; -fx-background-radius: 4 0 0 4;");
                label.toFront();
            }
        });
    }

    /**
     * Initializes a toggle switch.
     * @param textOn Text if the toggle switch is selected.
     * @param textOff Text if the toggle switch is deselected.
     */
    public ToggleSwitch(@NamedArg("textOn") String textOn, @NamedArg("textOff") String textOff) {
        this();
        setTextOn(textOn);
        setTextOff(textOff);
    }

    /**
     * Sets the bindings of the text, the click handlers and the style.
     */
    private void init() {
        label.textProperty().bind(textOff);
            
        getChildren().addAll(button, label);
        button.setOnAction((e) -> {
            selected.set(!selected.get());
        });
        label.setOnMouseClicked((e) -> {
            selected.set(!selected.get());
        });
        setStyle();
            
        bindProperties();
    }

    /**
     * Defines the style of the toggle switch.
     */
    private void setStyle() {
        //Default Width
        setMaxWidth(80);
        setMinWidth(80);
        setWidth(80);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: grey; -fx-text-fill:black; -fx-background-radius: 0 4 4 0;");
        button.setStyle("-fx-text-fill:black; -fx-background-radius: 4 0 0 4;");
        setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * Binds the label size and button size to the whole toggle switch.
     */
    private void bindProperties() {
        label.prefWidthProperty().bind(widthProperty().divide(2));
        label.prefHeightProperty().bind(heightProperty());
        button.prefWidthProperty().bind(widthProperty().divide(2));
        button.prefHeightProperty().bind(heightProperty());
    }

    /**
     * The selected state of the toggle switch.
     * @return 
     */
    public BooleanProperty selectedProperty() {
        return selected;
    }
    
    /**
     * Sets the selected text.
     * @param textOn 
     */
    public void setTextOn(String textOn) {
        this.textOn.set(textOn);
    }
    
    /**
     * Sets the deselected text.
     * @param textOff 
     */
    public void setTextOff(String textOff) {
        this.textOff.set(textOff);
    }
}
