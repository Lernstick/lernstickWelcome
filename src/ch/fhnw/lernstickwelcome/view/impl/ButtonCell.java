/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;

/**
 * This TableCell shows a button which can be clicked to run table operations.
 * <br>
 * <small>Currently only DELETE is working on tables. Edit/Save would require
 * to disable editing on all rows and just enable it on the selected row. A good
 * solution to this issue is currently not found.</small>
 * @author tiagosantosb
 */
public class ButtonCell extends TableCell<WebsiteFilter, WebsiteFilter> {
    /**
     * The Type of action that this ButtonCell represents.
     */
    public enum Type {
        EDIT, SAVE, DELETE;
        
        @Override
        public String toString() {
            switch (this) {
                case EDIT: return "btn_edit";
                case SAVE: return "btn_save";
                case DELETE: return "btn_delete";
                default: return null;
            }
        }
    }
    
    private Button btn;

    /**
     * Creates a new ButtonCell for the provided TableView with the given
     * {@link Type}.
     * @param type The type of action that should be run on click.
     * @param parent The parent that should react on the action.
     */
    public ButtonCell(Type type, TableView<WebsiteFilter> parent) {
        super();
        TableView<WebsiteFilter> table = parent;
        
        btn = new Button();
        btn.getStyleClass().add(type.toString());
        btn.setPrefWidth(40);
        btn.setPrefHeight(30);
        btn.setOnAction(e -> {
            Button b = (Button) e.getSource();
            // Switch to edit mode
            if (b.getStyleClass().contains(Type.EDIT.toString())) {
                // Change icon to save
                b.getStyleClass().remove(Type.EDIT.toString());
                b.getStyleClass().add(Type.SAVE.toString());
                table.edit(this.getIndex(), table.getColumns().get(0));
            } 
            else 
            // Save changes
            if(b.getStyleClass().contains(Type.SAVE.toString())) {
                // Change icon to edit
                b.getStyleClass().remove(Type.SAVE.toString());
                b.getStyleClass().add(Type.EDIT.toString());
                table.edit(-1, null);
            } 
            // Delete item
            else if(b.getStyleClass().contains(Type.DELETE.toString())) {
                table.getItems().remove(this.getIndex());
            }
        });
    }

    /**
     * Display button if the row is not empty
     * @see TableCell#updateItem(java.lang.Object, boolean) 
     * @param wf
     * @param empty
     */
    @Override
    protected void updateItem(WebsiteFilter wf, boolean empty) {
        super.updateItem(wf, empty);
        if (!empty){
            setGraphic(btn);
        } else {
            setGraphic(null);
        }
    }
    
    
}
