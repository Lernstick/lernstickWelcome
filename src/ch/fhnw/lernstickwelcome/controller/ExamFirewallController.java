/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationFirewallController;
import ch.fhnw.lernstickwelcome.model.firewall.IpFilter;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author user
 */
public class ExamFirewallController {

    
    public ExamFirewallController(WelcomeController controller, WelcomeApplicationFirewallController firewall) {
        addBindings(controller, firewall);
    }
    
    private void addBindings(WelcomeController controller, WelcomeApplicationFirewallController firewall) {
        // Bind url_whitelist view data to model data
        firewall.getTv_fw_allowed_sites().itemsProperty().bindBidirectional(controller.getFirewall().getWebsiteListProperty());
        
        // Bind net_whitelist view data to model data
        firewall.getTv_fw_allowed_servers().itemsProperty().bindBidirectional(controller.getFirewall().getIpListProperty());
    }
}

