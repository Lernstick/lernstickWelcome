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
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 * A button cell for deleting firewall rules
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class FirewallDeleteButtonCell
        extends ButtonCell<WebsiteFilter, WebsiteFilter> {

    public FirewallDeleteButtonCell(
            FirewallController firewallController, TableView table) {

        super("btn_delete", (c, e) -> {

            // exit edit mode if deleting entry
            if ((table == firewallController.getAllowedSitesTableView())
                    && (firewallController.getIndexSaveWebsiteFilter() == c.getIndex())) {

                firewallController.getAddEditPatternComboBox().setValue(null);
                firewallController.getAddEditCriteriaTextField().setText("");

                Button addEditSiteButton
                        = firewallController.getAddEditSiteButton();
                addEditSiteButton.getStyleClass().remove("btn_save");
                addEditSiteButton.getStyleClass().add("btn_add");

            } else if ((table == firewallController.getAllowedServersTableView())
                    && (firewallController.getIndexSaveIpFilter() == c.getIndex())) {

                firewallController.getAddEditProtocolComboBox().setValue(null);
                firewallController.getAddEditHostTextField().setText("");
                firewallController.getAddEditPortTextField().setText("");
                firewallController.getAddEditDescriptionTextField().setText("");

                Button addEditSiteButton
                        = firewallController.getAddEditServerButton();

                addEditSiteButton.getStyleClass().remove("btn_save");
                addEditSiteButton.getStyleClass().add("btn_add");
            }

            table.getItems().remove(c.getIndex());
        });
    }
}
