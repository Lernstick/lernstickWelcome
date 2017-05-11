package ch.fhnw.lernstickwelcome.controller.binder.exam;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.model.firewall.SquidAccessLogWatcher;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallPatternValidatorController;

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
        try {
            Thread t = new Thread(watcher);
            t.start();
        } catch(Exception e) {
            
        }
        // Bind url_whitelist view data to model data
        firewall.getTvPatternDependencies().itemsProperty().bind(watcher.getWebsiteList());
    }
}
