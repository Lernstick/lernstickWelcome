/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationFirewallController;
import ch.fhnw.lernstickwelcome.model.firewall.IpFilter;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import javafx.beans.Observable;
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
        ObservableList<WebsiteFilter> modelWFData = controller.getFirewall().getWebsiteList();
        ObservableList<WebsiteFilter> viewWFData = (ObservableList<WebsiteFilter>) firewall.getTv_fw_allowed_sites().getItems();
        // Bind url_whitelist view data to model data
        viewWFData.addListener((ListChangeListener.Change<?> changes) -> {
            if(changes.wasAdded()) modelWFData.addAll((ObservableList<WebsiteFilter>) changes.getAddedSubList());
            if(changes.wasRemoved()) modelWFData.removeAll((ObservableList<WebsiteFilter>) changes.getRemoved());
        });
        // Bind url_whitelist model data to view data
        /*controller.getFirewall().getWebsiteList().addListener((ListChangeListener.Change<? extends WebsiteFilter> changes) -> {
            
        });*/
        
        // Bind net_whitelist view data to model data
        ObservableList<IpFilter> modelIFData = controller.getFirewall().getIpList();
        ObservableList<IpFilter> viewIFData = (ObservableList<IpFilter>) firewall.getTv_fw_allowed_servers().getItems();
        viewIFData.addListener((ListChangeListener.Change<?> changes) -> {
            if(changes.wasAdded()) modelIFData.addAll((ObservableList<IpFilter>) changes.getAddedSubList());
            if(changes.wasRemoved()) modelIFData.removeAll((ObservableList<IpFilter>) changes.getRemoved());
        });
        // Bind net_whitelist model data to view data
        firewall.getBtn_fw_add_new_server().onActionProperty().addListener(cl -> {
                if(firewall.validateFields()) {
                    viewIFData.add(new IpFilter(
                            firewall.getChoice_fw_protocol().getValue(), 
                            firewall.getTxt_fw_new_ip().getText(), 
                            firewall.getTxt_fw_new_port().getText(), 
                            firewall.getTxt_fw_new_desc().getText()));
                }
        });
        /*controller.getFirewall().getIpList().addListener((ListChangeListener.Change<? extends IpFilter> changes) -> {
            
        });*/
    }
}

