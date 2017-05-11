/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallController;
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
        super("btn_open_url", (c, e) -> {
            Button b = (Button) e.getSource();
            WebsiteFilter pattern = table.getItems().get(c.getTableRow().getIndex());
            WelcomeUtil.openLinkInBrowser(pattern.searchCriteriaProperty().get());
        });
    }
}
