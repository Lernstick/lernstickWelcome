/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.view.WelcomeApplicationBackupController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationFirewallController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationInformationController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationStartController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationSystemController;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Roger Obrist
 */
public final class FXMLGuiLoader {
    private static ResourceBundle BUNDLE;
    private static final FXMLGuiLoader INSTANCE = new FXMLGuiLoader();
    
    private Scene welcomeApplicationStart;
    private Scene system;
    private Scene firewall;
    private Scene information;
    private Scene backup;

    
    // Controller
    private WelcomeApplicationBackupController welcomeApplicationBackupController;
    private WelcomeApplicationFirewallController welcomeApplicationFirewallController;
    private WelcomeApplicationInformationController welcomeApplicationInformationController;
    private WelcomeApplicationStartController welcomeApplicationStartController;
    private WelcomeApplicationSystemController welcomeApplicationSystemController;
    
    private FXMLGuiLoader() {
        // Create all instances with their controllers
        try {
	    BUNDLE = ResourceBundle.getBundle("ch/fhnw/welcomeapplication/Bundle");
            welcomeApplicationStart = new Scene((Parent)FXMLLoader.load(getClass().getResource("../view/WelcomeApplicationStart.fxml"), BUNDLE));
            
            FXMLLoader loadProvSup = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationSystem.fxml"), BUNDLE);
            system = new Scene((Parent)loadProvSup.load());
            welcomeApplicationSystemController = (WelcomeApplicationSystemController) loadProvSup.getController();
            
            FXMLLoader loadReqSup = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationFirewall.fxml"), BUNDLE);
            firewall = new Scene((Parent)loadReqSup.load());
            welcomeApplicationFirewallController = (WelcomeApplicationFirewallController) loadReqSup.getController();
                        
            FXMLLoader loadConnecting = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationInformation.fxml"), BUNDLE);
            information = new Scene((Parent) loadConnecting.load());
            welcomeApplicationInformationController = (WelcomeApplicationInformationController) loadConnecting.getController();
            
            FXMLLoader loadConnected = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationBackup.fxml"), BUNDLE);
            backup = new Scene((Parent) loadConnected.load());
            welcomeApplicationBackupController = (WelcomeApplicationBackupController) loadConnected.getController();
            
        } catch(IOException ex) {
               ex.printStackTrace();
        }
    }
    
    public static FXMLGuiLoader getInstance() {
        return INSTANCE;
    }
    
    public Scene getWelcomeApplicationStart() {
        return welcomeApplicationStart;
    }

    public Scene getSystem() {
        return system;
    }

    public Scene getFirewall() {
        return firewall;
    }

    public Scene getInformation() {
        return information;
    }

    public Scene getBackup() {
        return backup;
    }
    
    /**
     * 
     * @param parent parent
     * @param scene scene
     * @param title stage title
     * @param modal modal
     * @return 
     */
    public Stage createDialog(Stage parent, Scene scene, String title, boolean modal) {
        Stage stage = new Stage();
        stage.initOwner(parent);
        if(modal) {
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(modal);
        }
        stage.setTitle(title);
        stage.setScene(scene);
        return stage;
    }
    
}
