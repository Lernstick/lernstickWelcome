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

import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 *
 * @author root
 */
public class FirewallOpenButtonCell extends ButtonCell<WebsiteFilter, WebsiteFilter> {

    public FirewallOpenButtonCell(TableView<WebsiteFilter> table) {
        super("btn_browse", (c, e) -> {
            Button b = (Button) e.getSource();
            WebsiteFilter pattern = table.getItems().get(c.getTableRow().getIndex());
            WelcomeUtil.openLinkInBrowser(pattern.searchCriteriaProperty().get());
        });
    }
}
