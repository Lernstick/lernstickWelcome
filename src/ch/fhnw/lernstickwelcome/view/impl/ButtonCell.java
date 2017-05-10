/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import ch.fhnw.lernstickwelcome.fxmlcontroller.FirewallController;
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
    public ButtonCell(Type type, FirewallController fwc, TableView table) {
        super();
        
        btn = new Button();
        btn.getStyleClass().add(type.toString());
        btn.setPrefWidth(40);
        btn.setPrefHeight(30);
        btn.setOnAction(e -> {
            Button b = (Button) e.getSource();
            // Edit item
            if (b.getStyleClass().contains(Type.EDIT.toString())) {
                if (table == fwc.getTvAllowedSites()) {
                    // Prepare view for edit
                    WebsiteFilter element = (WebsiteFilter) table.getItems().get(this.getIndex());
                    fwc.getCbAddEditPattern().setValue(element.searchPatternProperty().get());
                    fwc.getTfAddEditCriteria().setText(element.searchCriteriaProperty().get());
                    fwc.getBtAddEditSite().getStyleClass().remove("btn_add");
                    fwc.getBtAddEditSite().getStyleClass().add("btn_save");
                    fwc.setIndexSaveWebsiteFilter(this.getIndex());
                    fwc.getCbAddEditPattern().requestFocus();
                    // Scroll to edit fields
                    ScrollPane sp = (ScrollPane) fwc.getBtAddEditServer().getScene().lookup("#MainPane");
                    sp.setVvalue(0.0);
                } else {
                    // Prepare view for edit
                    IpFilter element = (IpFilter) table.getItems().get(this.getIndex());
                    fwc.getCbAddEditProtocol().setValue(element.protocolProperty().get());
                    fwc.getTfAddEditIp().setText(element.ipAddressProperty().get());
                    fwc.getTfAddEditPort().setText(element.portProperty().get());
                    fwc.getTfAddEditDesc().setText(element.descriptionProperty().get());
                    fwc.getBtAddEditServer().getStyleClass().remove("btn_add");
                    fwc.getBtAddEditServer().getStyleClass().add("btn_save");
                    fwc.setIndexSaveIpFilter(this.getIndex());
                    fwc.getCbAddEditProtocol().requestFocus();
                    // Scroll to edit fields
                    ScrollPane sp = (ScrollPane) fwc.getBtAddEditServer().getScene().lookup("#MainPane");
                    sp.setVvalue(Double.MAX_VALUE);
                }
            }
            // Delete item
            else if(b.getStyleClass().contains(Type.DELETE.toString())) {
                // Exit edit mode if deleting entry
                if (table == fwc.getTvAllowedSites() && fwc.getIndexSaveWebsiteFilter() == this.getIndex()) {
                    fwc.getCbAddEditPattern().setValue(null);
                    fwc.getTfAddEditCriteria().setText("");
                    fwc.getBtAddEditSite().getStyleClass().remove("btn_save");
                    fwc.getBtAddEditSite().getStyleClass().add("btn_add");
                } else if (table == fwc.getTvAllowedServers() && fwc.getIndexSaveIpFilter() == this.getIndex()) {
                    fwc.getCbAddEditProtocol().setValue(null);
                    fwc.getTfAddEditIp().setText("");
                    fwc.getTfAddEditPort().setText("");
                    fwc.getTfAddEditDesc().setText("");
                    fwc.getBtAddEditServer().getStyleClass().remove("btn_save");
                    fwc.getBtAddEditServer().getStyleClass().add("btn_add");
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
