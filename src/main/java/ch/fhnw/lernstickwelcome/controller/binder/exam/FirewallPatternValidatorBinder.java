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
package ch.fhnw.lernstickwelcome.controller.binder.exam;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.model.firewall.SquidAccessLogWatcher;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallPatternValidatorController;
import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import ch.fhnw.lernstickwelcome.view.impl.ButtonCell;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Binder class to init binings and between view components and backend (model)
 * properties and add handlers
 *
 * @author Line Stettler
 */
public class FirewallPatternValidatorBinder {

    private final WelcomeController controller;
    private final FirewallPatternValidatorController firewall;
    private final SquidAccessLogWatcher watcher = new SquidAccessLogWatcher();

    /**
     * Constructor of ExamBackupBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param firewall FXML controller which prviedes the view properties
     */
    public FirewallPatternValidatorBinder(WelcomeController controller, FirewallPatternValidatorController firewall) {
        this.controller = controller;
        this.firewall = firewall;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * packend properties
     */
    public void initBindings() {
        // Bind url_whitelist view data to model data
        firewall.getTvPatternDependencies().itemsProperty().bind(watcher.getWebsiteList());
    }

    public void initHandlers(Stage stage) {
        stage.setOnShowing(evt -> {
            try {
                Thread t = new Thread(watcher);
                t.start();
            } catch (Exception e) {
            }
        });
        stage.setOnHiding(evt -> watcher.stop());
        firewall.getBtOk().setOnAction(evt -> {
            ((Stage) ((Node) evt.getSource()).getScene().getWindow()).close();
        });

        firewall.getTvPatternDependencies_add().setCellFactory(w -> new ButtonCell<>("btn_add", (c, evt) -> {
            WebsiteFilter website = (WebsiteFilter) c.getTableView().getItems().get(c.getTableRow().getIndex());
            controller.getFirewallTask().getWebsiteListProperty().add(website);
            watcher.getWebsiteList().remove(website);
            // We update the firewall directly to see the effect of the change
            new Thread(controller.getFirewallTask().newTask()).start();
        }));
    }
}
