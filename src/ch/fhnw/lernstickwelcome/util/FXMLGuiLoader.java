/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationBackupController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationFirewallController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationInformationController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationInstallController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationStartController;
import ch.fhnw.lernstickwelcome.view.WelcomeApplicationSystemController;
import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Roger Obrist
 */
public class FXMLGuiLoader {
    private static ResourceBundle BUNDLE;;
    
    private WelcomeController welcomeController;
    
    private Scene welcomeApplicationStart;
    private Pane system;
    private Pane firewall;
    private Pane information;
    private Pane backup;
    private Pane install;

    
    // Controller
    private WelcomeApplicationBackupController welcomeApplicationBackupController;
    private WelcomeApplicationFirewallController welcomeApplicationFirewallController;
    private WelcomeApplicationInformationController welcomeApplicationInformationController;
    private WelcomeApplicationStartController welcomeApplicationStartController;
    private WelcomeApplicationSystemController welcomeApplicationSystemController;
    private WelcomeApplicationInstallController welcomeApplicationInstallController;
    
    public FXMLGuiLoader(WelcomeController welcomeController, boolean isExamEnvironment) {
        this.welcomeController = welcomeController;

        // Create all instances with their controllers        
        try {
            
	    BUNDLE = ResourceBundle.getBundle("ch/fhnw/welcomeapplication/Bundle");
             
            FXMLLoader loadSystem = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationSystem.fxml"), BUNDLE);
            system = new Pane((Parent)loadSystem.load());
            welcomeApplicationSystemController = new WelcomeApplicationSystemController(welcomeController);
            
            FXMLLoader loadFirewall = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationFirewall.fxml"), BUNDLE);
            firewall = new Pane((Parent)loadFirewall.load());
            welcomeApplicationFirewallController = new WelcomeApplicationFirewallController(welcomeController);
                        
            FXMLLoader loadInfo = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationInformation.fxml"), BUNDLE);
            information = new Pane((Parent) loadInfo.load());
            welcomeApplicationInformationController = new WelcomeApplicationInformationController(welcomeController);
            
            FXMLLoader loadBackup = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationBackup.fxml"), BUNDLE);
            backup = new Pane((Parent) loadBackup.load());
            welcomeApplicationBackupController = new WelcomeApplicationBackupController(welcomeController);
            
            FXMLLoader loadInstall = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationInstall.fxml"), BUNDLE);
            install = new Pane((Parent) loadInstall.load());
            welcomeApplicationInstallController = new WelcomeApplicationInstallController(welcomeController);
            
            /* (...) */
            
            HashMap<String, Pane> panes = new HashMap<String, Pane>();
            panes.put("System", system);
            panes.put("Firewall", firewall);
            panes.put("Information", information);
            panes.put("Backup", backup);
            panes.put("Install", install);
            
            
            FXMLLoader loadWelcome = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationStart.fxml"), BUNDLE);
            welcomeApplicationStart = new Scene((Parent)loadWelcome.load());
            welcomeApplicationStartController = new WelcomeApplicationStartController(welcomeController, isExamEnvironment, panes);
            
            
        } catch(IOException ex) {
               ex.printStackTrace();
        }
        
        
    }
    
    
    public WelcomeApplicationStartController getWelcomeApplicationStart() {
        return welcomeApplicationStartController;
    }
    
    public Scene getMainStage()
    {
        return welcomeApplicationStart;
    }
    public WelcomeApplicationSystemController getSystem() {
        return welcomeApplicationSystemController;
    }

    public WelcomeApplicationFirewallController getFirewall() {
        return welcomeApplicationFirewallController;
    }

    public WelcomeApplicationInformationController getInformation() {
        return welcomeApplicationInformationController;
    }

    public WelcomeApplicationBackupController getBackup() {
        return welcomeApplicationBackupController;
    }

    public WelcomeApplicationInstallController getInstaller() {
        return welcomeApplicationInstallController;
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
