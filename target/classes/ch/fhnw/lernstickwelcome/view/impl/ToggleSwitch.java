/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
    public ToggleSwitch() {
        init();
        selected.addListener((a, b, c) -> {
            if (c) {
                label.textProperty().unbind();
                label.textProperty().bind(this.textOn);
                setStyle("-fx-background-color: green;");
                label.toFront();
            } else {
                label.textProperty().unbind();
                label.textProperty().bind(this.textOff);
                setStyle("-fx-background-color: grey;");
                button.toFront();
            }
        });
    }

    public ToggleSwitch(@NamedArg("textOn") String textOn, @NamedArg("textOff") String textOff) {
        this();
        setTextOn(textOn);
        setTextOff(textOff);
    }

    private void init() {
        label.textProperty().bind(textOff);
            
        getChildren().addAll(label, button);
        button.setOnAction((e) -> {
            selected.set(!selected.get());
        });
        label.setOnMouseClicked((e) -> {
            selected.set(!selected.get());
        });
        setStyle();
            
        bindProperties();
    }

    private void setStyle() {
        //Default Width
        setMaxWidth(80);
        setMinWidth(80);
        setWidth(80);
        label.setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: grey; -fx-text-fill:black; -fx-background-radius: 4;");
        setAlignment(Pos.CENTER_LEFT);
    }

    private void bindProperties() {
        label.prefWidthProperty().bind(widthProperty().divide(2));
        label.prefHeightProperty().bind(heightProperty());
        button.prefWidthProperty().bind(widthProperty().divide(2));
        button.prefHeightProperty().bind(heightProperty());
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
    
    public void setTextOn(String textOn) {
        this.textOn.set(textOn);
    }
    
    public void setTextOff(String textOff) {
        this.textOff.set(textOff);
    }
}
