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
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 *
 * @author root
 */
public class FirewallDeleteButtonCell extends ButtonCell<WebsiteFilter, WebsiteFilter> {

    public FirewallDeleteButtonCell(FirewallController fwc, TableView table) {
        super("btn_delete", (c, e) -> {

            // Exit edit mode if deleting entry
            if (table == fwc.getTvAllowedSites() && fwc.getIndexSaveWebsiteFilter() == c.getIndex()) {
                fwc.getCbAddEditPattern().setValue(null);
                fwc.getTfAddEditCriteria().setText("");
                fwc.getBtAddEditSite().getStyleClass().remove("btn_save");
                fwc.getBtAddEditSite().getStyleClass().add("btn_add");
            } else if (table == fwc.getTvAllowedServers() && fwc.getIndexSaveIpFilter() == c.getIndex()) {
                fwc.getCbAddEditProtocol().setValue(null);
                fwc.getTfAddEditIp().setText("");
                fwc.getTfAddEditPort().setText("");
                fwc.getTfAddEditDesc().setText("");
                fwc.getBtAddEditServer().getStyleClass().remove("btn_save");
                fwc.getBtAddEditServer().getStyleClass().add("btn_add");
            }
            table.getItems().remove(c.getIndex());
        });
    }
}
