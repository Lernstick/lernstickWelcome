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
        super("btn_edit", (c, e) -> {
            if (table == fwc.getTvAllowedSites()) {
                // Prepare view for edit
                WebsiteFilter element = (WebsiteFilter) table.getItems().get(c.getIndex());
                fwc.getCbAddEditPattern().setValue(element.searchPatternProperty().get());
                fwc.getTfAddEditCriteria().setText(element.searchCriteriaProperty().get());
                fwc.getBtAddEditSite().getStyleClass().remove("btn_add");
                fwc.getBtAddEditSite().getStyleClass().add("btn_save");
                fwc.setIndexSaveWebsiteFilter(c.getIndex());
                fwc.getCbAddEditPattern().requestFocus();
                // Scroll to edit fields
                ScrollPane sp = (ScrollPane) fwc.getBtAddEditServer().getScene().lookup("#MainPane");
                sp.setVvalue(0.0);
            } else {
                // Prepare view for edit
                IpFilter element = (IpFilter) table.getItems().get(c.getIndex());
                fwc.getCbAddEditProtocol().setValue(element.protocolProperty().get());
                fwc.getTfAddEditIp().setText(element.ipAddressProperty().get());
                fwc.getTfAddEditPort().setText(element.portProperty().get());
                fwc.getTfAddEditDesc().setText(element.descriptionProperty().get());
                fwc.getBtAddEditServer().getStyleClass().remove("btn_add");
                fwc.getBtAddEditServer().getStyleClass().add("btn_save");
                fwc.setIndexSaveIpFilter(c.getIndex());
                fwc.getCbAddEditProtocol().requestFocus();
                // Scroll to edit fields
                ScrollPane sp = (ScrollPane) fwc.getBtAddEditServer().getScene().lookup("#MainPane");
                sp.setVvalue(Double.MAX_VALUE);
            }
        });

    }
}
