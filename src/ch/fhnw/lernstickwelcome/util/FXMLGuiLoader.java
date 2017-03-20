/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationAdditionalSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationBackupController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationFirewallController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationInformationController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationInformationStdController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationInstallController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationProxyController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationRecommendedSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationStartController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemStdController;
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
    
    /* standard */
    private Pane informationStd;
    private Pane recommended;
    private Pane addSoftware;
    private Pane proxy;
    private Pane systemStd;
    
    /* exam */
    private Pane information;
    private Pane firewall;
    private Pane backup;
    private Pane system;

    
    // Controller
    private WelcomeApplicationBackupController welcomeApplicationBackupController;
    private WelcomeApplicationFirewallController welcomeApplicationFirewallController;
    private WelcomeApplicationInformationController welcomeApplicationInformationController;
    private WelcomeApplicationInformationStdController welcomeApplicationInformationStdController;
    private WelcomeApplicationStartController welcomeApplicationStartController;
    private WelcomeApplicationSystemController welcomeApplicationSystemController;
    private WelcomeApplicationSystemStdController welcomeApplicationSystemStdController;
    private WelcomeApplicationInstallController welcomeApplicationInstallController;
    private WelcomeApplicationAdditionalSoftwareController welcomeApplicationAdditionalSoftwareController;
    private WelcomeApplicationRecommendedSoftwareController welcomeApplicationRecommendedSoftwareController; 
    private WelcomeApplicationProxyController welcomeApplicationProxyController;
    
    public FXMLGuiLoader(WelcomeController welcomeController, boolean isExamEnvironment) {
        this.welcomeController = welcomeController;

        // Create all instances with their controllers        
        try {
            
	    BUNDLE = ResourceBundle.getBundle("ch/fhnw/welcomeapplication/Bundle");
            HashMap<String, Pane> panes = new HashMap<String, Pane>();
            
            if(!isExamEnvironment){
                FXMLLoader loadInfoStd = new FXMLLoader(getClass().getResource("../view/standard/WelcomeApplicationAdditionalSoftware.fxml"), BUNDLE);
                informationStd = new Pane((Parent)loadInfoStd.load());
                welcomeApplicationInformationStdController = new WelcomeApplicationInformationStdController();
                        
                FXMLLoader loadRecomm = new FXMLLoader(getClass().getResource("../view/standard/WelcomeApplicationRecommendedSoftware.fxml"), BUNDLE);
                recommended = new Pane((Parent)loadRecomm.load());
                welcomeApplicationRecommendedSoftwareController = new WelcomeApplicationRecommendedSoftwareController();
                        
                FXMLLoader loadAdd = new FXMLLoader(getClass().getResource("../view/standard/WelcomeApplicationAdditionalSoftware.fxml"), BUNDLE);
                addSoftware = new Pane((Parent)loadAdd.load());
                welcomeApplicationAdditionalSoftwareController = new WelcomeApplicationAdditionalSoftwareController();
                        
                FXMLLoader loadProxy = new FXMLLoader(getClass().getResource("../view/standard/WelcomeApplicationProxy.fxml"), BUNDLE);
                proxy = new Pane((Parent)loadProxy.load());
                welcomeApplicationProxyController = new WelcomeApplicationProxyController();
                        
                FXMLLoader loadSystemStd = new FXMLLoader(getClass().getResource("../view/standard/WelcomeApplicationSystemStd.fxml"), BUNDLE);
                systemStd = new Pane((Parent)loadSystemStd.load());
                welcomeApplicationSystemStdController = new WelcomeApplicationSystemStdController();
                
                panes.put("Info", informationStd);
                panes.put("Recommended Software", recommended);
                panes.put("Additional Software", addSoftware);
                panes.put("Proxy", proxy);
                panes.put("System", systemStd);
                
            }else{
                
                FXMLLoader loadInfo = new FXMLLoader(getClass().getResource("../view/exam/WelcomeApplicationInformation.fxml"), BUNDLE);
                information = new Pane((Parent) loadInfo.load());
                welcomeApplicationInformationController = new WelcomeApplicationInformationController();
                
                FXMLLoader loadFirewall = new FXMLLoader(getClass().getResource("../view/exam/WelcomeApplicationFirewall.fxml"), BUNDLE);
                firewall = new Pane((Parent)loadFirewall.load());
                welcomeApplicationFirewallController = new WelcomeApplicationFirewallController();

                FXMLLoader loadBackup = new FXMLLoader(getClass().getResource("../view/exam/WelcomeApplicationBackup.fxml"), BUNDLE);
                backup = new Pane((Parent) loadBackup.load());
                welcomeApplicationBackupController = new WelcomeApplicationBackupController();

                FXMLLoader loadSystem = new FXMLLoader(getClass().getResource("../view/exam/WelcomeApplicationSystem.fxml"), BUNDLE);
                system = new Pane((Parent)loadSystem.load());
                welcomeApplicationSystemController = new WelcomeApplicationSystemController();

                panes.put("Info", information);
                panes.put("Firewall", firewall);
                panes.put("Backup", backup);
                panes.put("System", system);

            }
            
            FXMLLoader loadWelcome = new FXMLLoader(getClass().getResource("../view/WelcomeApplicationStart.fxml"), BUNDLE);
            welcomeApplicationStart = new Scene((Parent)loadWelcome.load());
            welcomeApplicationStartController = new WelcomeApplicationStartController(isExamEnvironment, panes);
            
            
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
