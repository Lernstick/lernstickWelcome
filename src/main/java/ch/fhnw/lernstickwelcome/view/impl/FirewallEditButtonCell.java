/*
 * Copyright (C) 2020 Ronny Standtke <ronny.standtke@gmx.net>
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
import ch.fhnw.lernstickwelcome.model.firewall.HostFilter;
import ch.fhnw.lernstickwelcome.model.firewall.HostFilter.Protocol;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

/**
 * A button cell for editing firewall rules
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class FirewallEditButtonCell extends
        ButtonCell<WebsiteFilter, WebsiteFilter> {

    /**
     * Creates a new button for a firewall tableview
     *
     * @param firewallController The firewall controller, where in case of an
     * edit the values should be edited
     * @param table The table which contains this ButtonCell instance and the
     * other Elements to be edited
     */
    public FirewallEditButtonCell(
            FirewallController firewallController, TableView table) {

        super("btn_edit", (c, e) -> {

            if (table == firewallController.getAllowedSitesTableView()) {
                WebsiteFilter element
                        = (WebsiteFilter) table.getItems().get(c.getIndex());

                ComboBox<WebsiteFilter.SearchPattern> comboBox
                        = firewallController.getAddEditPatternComboBox();
                comboBox.setValue(element.searchPatternProperty().get());

                firewallController.getAddEditCriteriaTextField().setText(
                        element.searchCriteriaProperty().get());

                Button addeditSiteButton
                        = firewallController.getAddEditSiteButton();

                addeditSiteButton.getStyleClass().remove("btn_add");
                addeditSiteButton.getStyleClass().add("btn_save");

                firewallController.setIndexSaveWebsiteFilter(c.getIndex());

                comboBox.requestFocus();

            } else {
                HostFilter element
                        = (HostFilter) table.getItems().get(c.getIndex());

                ComboBox<Protocol> comboBox
                        = firewallController.getAddEditProtocolComboBox();
                comboBox.setValue(element.protocolProperty().get());

                firewallController.getAddEditHostTextField().setText(
                        element.hostProperty().get());

                firewallController.getAddEditPortTextField().setText(
                        element.portRangeProperty().get());

                firewallController.getAddEditDescriptionTextField().setText(
                        element.descriptionProperty().get());

                Button addEditServerButton
                        = firewallController.getAddEditServerButton();

                addEditServerButton.getStyleClass().remove("btn_add");
                addEditServerButton.getStyleClass().add("btn_save");

                firewallController.setIndexSaveHostFilter(c.getIndex());

                comboBox.requestFocus();
            }
        });

    }
}
