/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationFirewallController;
import ch.fhnw.lernstickwelcome.model.firewall.IpFilter;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
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
        EDIT, DELETE;
        
        @Override
        public String toString() {
            switch (this) {
                case EDIT: return "btn_edit";
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
     * @param fwc The firewall controller, where in case of an edit the values should be edited
     * @param table The table which contains this ButtonCell instance and the other Elements to be edited
     */
    public ButtonCell(Type type, WelcomeApplicationFirewallController fwc, TableView table) {
        super();
        
        btn = new Button();
        btn.getStyleClass().add(type.toString());
        btn.setPrefWidth(40);
        btn.setPrefHeight(30);
        btn.setOnAction(e -> {
            Button b = (Button) e.getSource();
            // Edit item
            if (b.getStyleClass().contains(Type.EDIT.toString())) {
                if (table == fwc.getTv_fw_allowed_sites()) {
                    // Prepare view for edit
                    WebsiteFilter element = (WebsiteFilter) table.getItems().get(this.getIndex());
                    fwc.getChoice_fw_search_pattern().setValue(element.searchPatternProperty().get());
                    fwc.getTxt_fw_search_criteria().setText(element.searchCriteriaProperty().get());
                    fwc.getBtn_fw_new_rule().getStyleClass().remove("btn_add");
                    fwc.getBtn_fw_new_rule().getStyleClass().add("btn_save");
                    fwc.setIndexSaveWebsiteFilter(this.getIndex());
                    fwc.getChoice_fw_search_pattern().requestFocus();
                } else {
                    // Prepare view for edit
                    IpFilter element = (IpFilter) table.getItems().get(this.getIndex());
                    fwc.getChoice_fw_protocol().setValue(element.protocolProperty().get());
                    fwc.getTxt_fw_new_ip().setText(element.ipAddressProperty().get());
                    fwc.getTxt_fw_new_port().setText(element.portProperty().get());
                    fwc.getTxt_fw_new_desc().setText(element.descriptionProperty().get());
                    fwc.getBtn_fw_add_new_server().getStyleClass().remove("btn_add");
                    fwc.getBtn_fw_add_new_server().getStyleClass().add("btn_save");
                    fwc.setIndexSaveIpFilter(this.getIndex());
                    fwc.getChoice_fw_protocol().requestFocus();
                }
            }
            // Delete item
            else if(b.getStyleClass().contains(Type.DELETE.toString())) {
                // Exit edit mode if deleting entry
                if (table == fwc.getTv_fw_allowed_sites() && fwc.getIndexSaveWebsiteFilter() == this.getIndex()) {
                    fwc.getChoice_fw_search_pattern().setValue(null);
                    fwc.getTxt_fw_search_criteria().setText("");
                    fwc.getBtn_fw_new_rule().getStyleClass().remove("btn_save");
                    fwc.getBtn_fw_new_rule().getStyleClass().add("btn_add");
                } else if (table == fwc.getTv_fw_allowed_servers() && fwc.getIndexSaveIpFilter() == this.getIndex()) {
                    fwc.getChoice_fw_protocol().setValue(null);
                    fwc.getTxt_fw_new_ip().setText("");
                    fwc.getTxt_fw_new_port().setText("");
                    fwc.getTxt_fw_new_desc().setText("");
                    fwc.getBtn_fw_add_new_server().getStyleClass().remove("btn_save");
                    fwc.getBtn_fw_add_new_server().getStyleClass().add("btn_add");
                }
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
