/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationAdditionalSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationBackupController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationFirewallController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationInformationController;
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
   // private static final FXMLGuiLoader INSTANCE = new FXMLGuiLoader(true);
    
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

    private boolean isExamEnvironment;
    
    // Controller
    private WelcomeApplicationBackupController welcomeApplicationBackupController;
    private WelcomeApplicationFirewallController welcomeApplicationFirewallController;
    private WelcomeApplicationInformationController welcomeApplicationInformationController;
    private WelcomeApplicationStartController welcomeApplicationStartController;
    private WelcomeApplicationSystemController welcomeApplicationSystemController;
    private WelcomeApplicationSystemStdController welcomeApplicationSystemStdController;
    private WelcomeApplicationInstallController welcomeApplicationInstallController;
    private WelcomeApplicationAdditionalSoftwareController welcomeApplicationAdditionalSoftwareController;
    private WelcomeApplicationRecommendedSoftwareController welcomeApplicationRecommendedSoftwareController; 
    private WelcomeApplicationProxyController welcomeApplicationProxyController;
    
    public FXMLGuiLoader(boolean isExamEnvironment, ResourceBundle rb) {
        this.isExamEnvironment = isExamEnvironment;
        // Create all instances with their controllers        
        try {

            HashMap<String, Pane> panes = new HashMap<String, Pane>();
            
            welcomeApplicationStart = new Scene((Parent)FXMLLoader.load(getClass().getResource("../view/welcomeApplicationStart.fxml"), rb));
            welcomeApplicationStartController = new WelcomeApplicationStartController();
            
            if(!this.isExamEnvironment){
                FXMLLoader loadInfoStd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationInformationStd.fxml"), rb);
                informationStd = new Pane((Parent)loadInfoStd.load());
                welcomeApplicationInformationController = loadInfoStd.getController();
                        
                FXMLLoader loadRecomm = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationRecommendedSoftware.fxml"), rb);
                recommended = new Pane((Parent)loadRecomm.load());
                welcomeApplicationRecommendedSoftwareController = loadRecomm.getController();
                        
                FXMLLoader loadAdd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationAdditionalSoftware.fxml"), rb);
                addSoftware = new Pane((Parent)loadAdd.load());
                welcomeApplicationAdditionalSoftwareController = loadAdd.getController();
                        
                FXMLLoader loadProxy = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationProxy.fxml"), rb);
                proxy = new Pane((Parent)loadProxy.load());
                welcomeApplicationProxyController = loadProxy.getController();
                        
                FXMLLoader loadSystemStd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationSystemStd.fxml"), rb);
                systemStd = new Pane((Parent)loadSystemStd.load());
                welcomeApplicationSystemStdController = loadSystemStd.getController();
                
                panes.put("Information", informationStd);
                panes.put("Recommended Software", recommended);
                panes.put("Additional Software", addSoftware);
                panes.put("Proxy", proxy);
                panes.put("System", systemStd);
                
            }else{
                
                FXMLLoader loadInfo = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationInformation.fxml"), rb);
                information = new Pane((Parent) loadInfo.load());
                welcomeApplicationInformationController = loadInfo.getController();
                
                FXMLLoader loadFirewall = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationFirewall.fxml"), rb);
                firewall = new Pane((Parent)loadFirewall.load());
                welcomeApplicationFirewallController = loadFirewall.getController();

                FXMLLoader loadBackup = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationBackup.fxml"), rb);
                backup = new Pane((Parent) loadBackup.load());
                welcomeApplicationBackupController = loadBackup.getController();

                FXMLLoader loadSystem = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationSystem.fxml"), rb);
                system = new Pane((Parent)loadSystem.load());
                welcomeApplicationSystemController = loadSystem.getController();

                panes.put("Information", information);
                panes.put("Firewall", firewall);
                panes.put("Backup", backup);
                panes.put("System", system);

            }


            welcomeApplicationStartController.initializeController(isExamEnvironment, panes);
            
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
