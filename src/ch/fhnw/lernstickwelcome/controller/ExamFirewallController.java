/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationFirewallController;
import javafx.stage.Stage;

/**
 *
 * @author user
 */
public class ExamFirewallController {

    private final WelcomeController controller;
    private final WelcomeApplicationFirewallController firewall;

    
    public ExamFirewallController(WelcomeController controller, WelcomeApplicationFirewallController firewall) {
        this.controller = controller;
        this.firewall = firewall;
    }
    
    public void initBindings() {
        // Bind url_whitelist view data to model data
        firewall.getTv_fw_allowed_sites().itemsProperty().bindBidirectional(controller.getFirewall().getWebsiteListProperty());
        
        // Bind net_whitelist view data to model data
        firewall.getTv_fw_allowed_servers().itemsProperty().bindBidirectional(controller.getFirewall().getIpListProperty());
        
        firewall.getCb_fw_allow_monitoring().switchOnProperty().set(controller.getFirewall().firewallRunningProperty().get());
    }
    
    public void initHandlers(Stage errorDialog, WelcomeApplicationErrorController error) {
        firewall.getCb_fw_allow_monitoring().switchOnProperty().addListener(cl -> {
            try {
                if(firewallStateChanged())
                    controller.getFirewall().toggleFirewallState();
            } catch (ProcessingException ex) {
                error.initErrorMessage(ex);
                errorDialog.show();
            }
        });
        controller.getFirewall().firewallRunningProperty().addListener(cl -> {
            if(firewallStateChanged())
                firewall.getCb_fw_allow_monitoring().switchOnProperty().set(controller.getFirewall().firewallRunningProperty().get());
        });
    }

    private boolean firewallStateChanged() {
        return controller.getFirewall().firewallRunningProperty().get() != firewall.getCb_fw_allow_monitoring().switchOnProperty().get();
    }
}

