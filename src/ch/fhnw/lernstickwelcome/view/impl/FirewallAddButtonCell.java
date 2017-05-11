/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallController;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 *
 * @author root
 */
public class FirewallDeleteButtonCell extends ButtonCell<WebsiteFilter, WebsiteFilter> {

    public FirewallDeleteButtonCell(FirewallController fwc, TableView table) {
        super("btn_delete");

        btn.setOnAction(e -> {
            Button b = (Button) e.getSource();

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
        });
    }
}
