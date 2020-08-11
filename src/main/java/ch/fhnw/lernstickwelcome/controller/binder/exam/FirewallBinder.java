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
package ch.fhnw.lernstickwelcome.controller.binder.exam;

import ch.fhnw.lernstickwelcome.controller.WelcomeApplication;
import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.controller.binder.HelpBinder;
import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallController;
import ch.fhnw.lernstickwelcome.model.firewall.FirewallTask;
import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Binder class to init binings and between view components and backend (model)
 * properties and add handlers
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class FirewallBinder {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "ch.fhnw.lernstickwelcome.Bundle");

    private final WelcomeController welcomeController;
    private final FirewallController firewallController;

    /**
     * Constructor of ExamBackupBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param firewall FXML controller which prviedes the view properties
     */
    public FirewallBinder(WelcomeController controller,
            FirewallController firewall) {

        this.welcomeController = controller;
        this.firewallController = firewall;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * packend properties
     */
    public void initBindings() {

        FirewallTask firewallTask = welcomeController.getFirewallTask();

        // Bind url_whitelist view data to model data
        firewallController.getAllowedSitesTableView().itemsProperty().bindBidirectional(
                firewallTask.getWebsiteListProperty());

        // Bind net_whitelist view data to model data
        firewallController.getAllowedServersTableView().itemsProperty().bindBidirectional(
                firewallTask.getHostFilterListProperty());

        firewallController.getAllowMonitoringToggleSwitch().selectedProperty().set(
                firewallTask.firewallRunningProperty().get());
    }

    /**
     * Method to initialize the handlers for this class.
     *
     * @param depWarningDialog the dialog if we want to open the dependency
     * validator dialog but have changes in the table.
     * @param depValidStage the dependency validator dialog.
     */
    public void initHandlers(Stage depWarningDialog, Stage depValidStage) {

        FirewallTask firewallTask = welcomeController.getFirewallTask();

        ToggleSwitch allowMonitoringToggleSwitch
                = firewallController.getAllowMonitoringToggleSwitch();

        BooleanProperty firewallRunningProperty
                = firewallTask.firewallRunningProperty();

        Label monitoringLabel = firewallController.getMonitoringLabel();

        allowMonitoringToggleSwitch.selectedProperty().addListener(cl -> {
            try {
                if (firewallStateChanged()) {
                    firewallTask.toggleFirewallState();
                }
                updateMonitoringLabel(firewallRunningProperty, monitoringLabel);
            } catch (ProcessingException ex) {
                allowMonitoringToggleSwitch.selectedProperty().set(
                        firewallRunningProperty.get());
                WelcomeApplication.showThrowable(ex);
            }
        });

        firewallRunningProperty.addListener(cl -> {
            if (firewallStateChanged()) {
                allowMonitoringToggleSwitch.selectedProperty().set(
                        firewallRunningProperty.get());
            }
        });

        firewallController.getCheckForDependenciesButton().setOnAction(evt -> {
            if (firewallTask.hasUnsavedUrls()) {
                depWarningDialog.show();
            } else {
                depValidStage.show();
            }
        });

        // Init default value because handler isn't fired the first time.
        updateMonitoringLabel(firewallRunningProperty, monitoringLabel);
    }

    /**
     * Open other view by clicking on help button
     *
     * @param helpStage additional window showing help
     * @param help links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder help) {
        firewallController.getHelpButton().setOnAction(evt -> {
            help.setHelpEntryByChapter("1");
            helpStage.show();
        });
    }

    private boolean firewallStateChanged() {
        return welcomeController.getFirewallTask().firewallRunningProperty().get()
                != firewallController.getAllowMonitoringToggleSwitch().selectedProperty().get();
    }

    private void updateMonitoringLabel(
            BooleanProperty firewallRunningProperty, Label monitoringLabel) {

        if (firewallRunningProperty.get()) {
            monitoringLabel.setText(BUNDLE.getString(
                    "welcomeApplicationFirewall.monitoringInternetAccessOn"));
            monitoringLabel.getStyleClass().remove("lbl_off");
            monitoringLabel.getStyleClass().add("lbl_on");
        } else {
            monitoringLabel.setText(BUNDLE.getString(
                    "welcomeApplicationFirewall.monitoringInternetAccessOff"));
            monitoringLabel.getStyleClass().remove("lbl_on");
            monitoringLabel.getStyleClass().add("lbl_off");
        }
    }
}
