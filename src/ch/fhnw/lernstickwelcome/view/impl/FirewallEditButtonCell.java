/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallController;
import ch.fhnw.lernstickwelcome.model.firewall.IpFilter;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;

/**
 *
 * @author root
 */
public class FirewallEditButtonCell extends ButtonCell<WebsiteFilter, WebsiteFilter> {

    /**
     * Creates a new button for a firewall tableview
     *
     * @param type name of the style class
     * @param fwc The firewall controller, where in case of an edit the values
     * should be edited
     * @param table The table which contains this ButtonCell instance and the
     * other Elements to be edited
     */
    public FirewallEditButtonCell(FirewallController fwc, TableView table) {
        super("btn_edit");

        super.btn.setOnAction(e -> {
            Button b = (Button) e.getSource();
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
        });

    }
}
