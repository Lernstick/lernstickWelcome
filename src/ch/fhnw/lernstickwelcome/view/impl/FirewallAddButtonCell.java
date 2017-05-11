/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view.impl;

import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 *
 * @author root
 */
public class FirewallAddButtonCell extends ButtonCell<WebsiteFilter, WebsiteFilter> {

    public FirewallAddButtonCell(TableView<WebsiteFilter> table) {
        super("btn_add");

        btn.setOnAction(e -> {
            Button b = (Button) e.getSource();
        });
    }
}
