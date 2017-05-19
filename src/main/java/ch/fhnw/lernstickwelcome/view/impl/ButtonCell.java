/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

/**
 * This TableCell shows a button which can be clicked to run table operations.
 * <br>
 * <small>Currently only DELETE is working on tables. Edit/Save would require
 * to disable editing on all rows and just enable it on the selected row. A good
 * solution to this issue is currently not found.</small>
 * @author tiagosantosb
 */
public class ButtonCell<T, U> extends TableCell<T, U> {
    
    protected Button btn;
    
    @FunctionalInterface
    public static interface ButtonCellEventHandler {
        void handle(ButtonCell cell, ActionEvent evt);
    }

    /**
     * Creates a new ButtonCell for the provided TableView with the given
     * @param type name of the style class
     * @param evt the event for this button cell
     */
    public ButtonCell(String type, ButtonCellEventHandler evt) {
        super();
        
        btn = new Button();
        btn.getStyleClass().add(type);
        btn.setPrefWidth(40);
        btn.setPrefHeight(30);
        btn.setOnAction(e -> evt.handle(this, e));
    }

    /**
     * Display button if the row is not empty
     * @see TableCell#updateItem(java.lang.Object, boolean) 
     * @param wf
     * @param empty
     */
    @Override
    protected void updateItem(U wf, boolean empty) {
        super.updateItem(wf, empty);
        if (!empty){
            setGraphic(btn);
        } else {
            setGraphic(null);
        }
    }
}
