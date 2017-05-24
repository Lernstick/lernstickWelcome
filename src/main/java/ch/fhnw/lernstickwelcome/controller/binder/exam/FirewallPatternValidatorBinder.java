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
            controller.getFirewall().getWebsiteListProperty().add(website);
            watcher.getWebsiteList().remove(website);
            // We update the firewall directly to see the effect of the change
            new Thread(controller.getFirewall().newTask()).start();
        }));
    }
}
