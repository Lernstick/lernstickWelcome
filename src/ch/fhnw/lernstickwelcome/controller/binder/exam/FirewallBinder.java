package ch.fhnw.lernstickwelcome.controller.binder.exam;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.controller.binder.HelpBinder;
import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallController;
import java.util.ResourceBundle;
import javafx.stage.Stage;

/**
 * Binder class to init binings and between view components and backend (model)
 * properties and add handlers
 *
 * @author Line Stettler
 */
public class FirewallBinder {

    private final WelcomeController controller;
    private final FirewallController firewall;

    /**
     * Constructor of ExamBackupBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param firewall FXML controller which prviedes the view properties
     */
    public FirewallBinder(WelcomeController controller, FirewallController firewall) {
        this.controller = controller;
        this.firewall = firewall;
    }

    /**
     * Method to initialize the bidirectional bindings between the view and
     * packend properties
     */
    public void initBindings() {
        // Bind url_whitelist view data to model data
        firewall.getTvAllowedSites().itemsProperty().bindBidirectional(controller.getFirewall().getWebsiteListProperty());

        // Bind net_whitelist view data to model data
        firewall.getTvAllowedServers().itemsProperty().bindBidirectional(controller.getFirewall().getIpListProperty());

        firewall.getTsAllowMonitoring().selectedProperty().set(controller.getFirewall().firewallRunningProperty().get());
    }

    /**
     * Method to initialize the handlers for this class.
     *
     * @param errorDialog the dialog that should be shown on error.
     * @param error the controller which the error message can be provided.
     */
    public void initHandlers(Stage errorDialog, ErrorController error) {
        ResourceBundle rb = controller.getBundle();
        firewall.getTsAllowMonitoring().selectedProperty().addListener(cl -> {
            try {
                if (firewallStateChanged()) {
                    controller.getFirewall().toggleFirewallState();

                    if (controller.getFirewall().firewallRunningProperty().get()) {
                        firewall.getLbAllowMonitoring().setText(rb.getString("welcomeApplicationFirewall.monitoringInternetAccessOn"));
                        firewall.getLbAllowMonitoring().getStyleClass().remove("lbl_off");
                        firewall.getLbAllowMonitoring().getStyleClass().add("lbl_on");
                    } else {
                        firewall.getLbAllowMonitoring().setText(rb.getString("welcomeApplicationFirewall.monitoringInternetAccessOff"));
                        firewall.getLbAllowMonitoring().getStyleClass().remove("lbl_on");
                        firewall.getLbAllowMonitoring().getStyleClass().add("lbl_off");
                    }
                }
            } catch (ProcessingException ex) {
                error.initErrorMessage(ex);
                errorDialog.show();
            }
        });

        controller.getFirewall().firewallRunningProperty().addListener(cl -> {
            if (firewallStateChanged()) {
                firewall.getTsAllowMonitoring().selectedProperty().set(controller.getFirewall().firewallRunningProperty().get());
            }
        });

        // Init default value because handler isn't fired the first time.
        if (controller.getFirewall().firewallRunningProperty().get()) {
            firewall.getLbAllowMonitoring().setText(rb.getString("welcomeApplicationFirewall.monitoringInternetAccessOn"));
            firewall.getLbAllowMonitoring().getStyleClass().remove("lbl_off");
            firewall.getLbAllowMonitoring().getStyleClass().add("lbl_on");
        } else {
            firewall.getLbAllowMonitoring().setText(rb.getString("welcomeApplicationFirewall.monitoringInternetAccessOff"));
            firewall.getLbAllowMonitoring().getStyleClass().remove("lbl_on");
            firewall.getLbAllowMonitoring().getStyleClass().add("lbl_off");
        }
    }

    /**
     * Returns if the gui state of the firewall is different to the backend
     * state
     *
     * @return
     */
    private boolean firewallStateChanged() {
        return controller.getFirewall().firewallRunningProperty().get() != firewall.getTsAllowMonitoring().selectedProperty().get();
    }

    /**
     * Open other view by clicking on help button
     *
     * @param helpStage additional window showing help
     * @param help links to online user guide
     */
    public void initHelp(Stage helpStage, HelpBinder help) {
        firewall.getBtHelp().setOnAction(evt -> {
            help.setHelpEntryByChapter("1");
            helpStage.show();
        });
    }
}
