/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.fxmlcontroller.MenuPaneItem;
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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Roger Obrist
 */
public class FXMLGuiLoader {
    private static final Logger LOGGER = Logger.getLogger(FXMLGuiLoader.class.getName());
   // private static final FXMLGuiLoader INSTANCE = new FXMLGuiLoader(true);
    
    private Scene welcomeApplicationStart;
    
    /* standard */
    private Parent informationStd;
    private Parent recommended;
    private Parent addSoftware;
    private Parent proxy;
    private Parent systemStd;
    
    /* exam */
    private Parent information;
    private Parent firewall;
    private Parent backup;
    private Parent system;

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
            ObservableList<MenuPaneItem> menuPaneItems = FXCollections.observableArrayList();
            
            FXMLLoader loadStart = new FXMLLoader(getClass().getResource("../view/welcomeApplicationStart.fxml"), rb);
            welcomeApplicationStart = new Scene(loadStart.load());
            welcomeApplicationStartController = loadStart.getController();
            
            if(!this.isExamEnvironment){
                FXMLLoader loadInfoStd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationInformationStd.fxml"), rb);
                informationStd = (Parent)loadInfoStd.load();
                welcomeApplicationInformationController = loadInfoStd.getController();
                        
                FXMLLoader loadRecomm = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationRecommendedSoftware.fxml"), rb);
                recommended = (Parent)loadRecomm.load();
                welcomeApplicationRecommendedSoftwareController = loadRecomm.getController();
                        
                FXMLLoader loadAdd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationAdditionalSoftware.fxml"), rb);
                addSoftware = (Parent)loadAdd.load();
                welcomeApplicationAdditionalSoftwareController = loadAdd.getController();
                        
                FXMLLoader loadProxy = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationProxy.fxml"), rb);
                proxy = (Parent)loadProxy.load();
                welcomeApplicationProxyController = loadProxy.getController();
                        
                FXMLLoader loadSystemStd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationSystemStd.fxml"), rb);
                systemStd = (Parent)loadSystemStd.load();
                welcomeApplicationSystemStdController = loadSystemStd.getController();
                
                menuPaneItems.add(new MenuPaneItem(informationStd, "Information", null));
                menuPaneItems.add(new MenuPaneItem(recommended, "Recommended Software", null));
                menuPaneItems.add(new MenuPaneItem(addSoftware, "Additional Software", null));
                menuPaneItems.add(new MenuPaneItem(proxy, "Proxy", null));
                menuPaneItems.add(new MenuPaneItem(systemStd, "System", null));
                
            }else{
                
                FXMLLoader loadInfo = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationInformation.fxml"), rb);
                information = (Parent) loadInfo.load();
                welcomeApplicationInformationController = loadInfo.getController();
                
                FXMLLoader loadFirewall = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationFirewall.fxml"), rb);
                firewall = (Parent)loadFirewall.load();
                welcomeApplicationFirewallController = loadFirewall.getController();

                FXMLLoader loadBackup = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationBackup.fxml"), rb);
                backup = (Parent) loadBackup.load();
                welcomeApplicationBackupController = loadBackup.getController();

                FXMLLoader loadSystem = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationSystem.fxml"), rb);
                system = (Parent)loadSystem.load();
                welcomeApplicationSystemController = loadSystem.getController();

                menuPaneItems.add(new MenuPaneItem(information, "Information", null));
                menuPaneItems.add(new MenuPaneItem(firewall, "Firewall", null));
                menuPaneItems.add(new MenuPaneItem(backup, "Backup", null));
                menuPaneItems.add(new MenuPaneItem(system, "System", null));

            }


            welcomeApplicationStartController.initializeMenu(menuPaneItems);
            
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "There was an error while loading the scene from the fxml files.", ex);
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
