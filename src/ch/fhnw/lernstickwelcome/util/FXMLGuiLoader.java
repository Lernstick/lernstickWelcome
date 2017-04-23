package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.fxmlcontroller.MenuPaneItem;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationAdditionalSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationBackupController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationFirewallController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationHelpController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationInformationController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationPasswordChangeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationProgressController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationRecommendedSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationMainController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemStdController;
import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import java.io.IOException;
import java.util.List;
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
    
    private Scene welcomeApplicationStart;
    
    private Scene welcomeApplicationProgress;
    private Scene welcomeApplicationError;
    private Scene welcomeApplicationHelp;
    
    /* standard */
    private Parent informationStd;
    private Parent recommended;
    private Parent addSoftware;
    private Parent systemStd;
    
    /* exam */
    private Scene welcomeApplicationPasswordChange;
    
    private Parent information;
    private Parent firewall;
    private Parent backup;
    private Parent system;
    
    // FXMLController
    private WelcomeApplicationProgressController welcomeApplicationProgressController;
    private WelcomeApplicationErrorController welcomeApplicationErrorController;
    private WelcomeApplicationHelpController welcomeApplicationHelpController;
    
    private WelcomeApplicationPasswordChangeController welcomeApplicationPasswordChangeController;
    private WelcomeApplicationBackupController welcomeApplicationBackupController;
    private WelcomeApplicationFirewallController welcomeApplicationFirewallController;
    private WelcomeApplicationInformationController welcomeApplicationInformationController;
    private WelcomeApplicationMainController welcomeApplicationStartController;
    private WelcomeApplicationSystemController welcomeApplicationSystemController;
    private WelcomeApplicationSystemStdController welcomeApplicationSystemStdController;
    private WelcomeApplicationAdditionalSoftwareController welcomeApplicationAdditionalSoftwareController;
    private WelcomeApplicationRecommendedSoftwareController welcomeApplicationRecommendedSoftwareController; 
    
    public FXMLGuiLoader(boolean isExamEnvironment, ResourceBundle rb) {
        // Create all instances with their controllers and load the internatioalized strings       
        try {
            ObservableList<MenuPaneItem> menuPaneItems = FXCollections.observableArrayList();
            
            // load start view
            FXMLLoader loadStart = new FXMLLoader(getClass().getResource("../view/welcomeApplicationMain.fxml"), rb);
            welcomeApplicationStart = new Scene(loadStart.load());
            welcomeApplicationStartController = loadStart.getController();
            
            if(!isExamEnvironment){
                FXMLLoader loadInfoStd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationInformationStd.fxml"), rb);
                informationStd = (Parent)loadInfoStd.load();
                welcomeApplicationInformationController = loadInfoStd.getController();
                        
                FXMLLoader loadRecomm = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationRecommendedSoftware.fxml"), rb);
                recommended = (Parent)loadRecomm.load();
                welcomeApplicationRecommendedSoftwareController = loadRecomm.getController();
                        
                FXMLLoader loadAdd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationAdditionalSoftware.fxml"), rb);
                addSoftware = (Parent)loadAdd.load();
                welcomeApplicationAdditionalSoftwareController = loadAdd.getController();
                        
                FXMLLoader loadSystemStd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationSystemStd.fxml"), rb);
                systemStd = (Parent)loadSystemStd.load();
                welcomeApplicationSystemStdController = loadSystemStd.getController();
                
                //add to pane list for the standard version
                menuPaneItems.add(new MenuPaneItem(informationStd, rb.getString("welcomeApplicationMain.Information"), null));
                menuPaneItems.add(new MenuPaneItem(recommended, rb.getString("welcomeApplicationMain.RecommendedSoftware"), null));
                menuPaneItems.add(new MenuPaneItem(addSoftware, rb.getString("welcomeApplicationMain.AdditionalSoftware"), null));
                menuPaneItems.add(new MenuPaneItem(systemStd, rb.getString("welcomeApplicationMain.System"), null));
            } else {
                FXMLLoader loadPasswordChange = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationPasswordChange.fxml"), rb);
                welcomeApplicationPasswordChange = new Scene(loadPasswordChange.load());
                welcomeApplicationPasswordChangeController = loadPasswordChange.getController();
                
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

                //add to pane list for the exam version
                menuPaneItems.add(new MenuPaneItem(information, rb.getString("welcomeApplicationMain.Information"), null));
                menuPaneItems.add(new MenuPaneItem(firewall, rb.getString("welcomeApplicationMain.Firewall"), null));
                menuPaneItems.add(new MenuPaneItem(backup, rb.getString("welcomeApplicationMain.Backup"), null));
                menuPaneItems.add(new MenuPaneItem(system, rb.getString("welcomeApplicationMain.System"), null));
            }
            
            FXMLLoader loadProgress = new FXMLLoader(getClass().getResource("../view/welcomeApplicationProgress.fxml"), rb);
            welcomeApplicationProgress = new Scene(loadProgress.load());
            welcomeApplicationProgressController = loadProgress.getController();
            
            FXMLLoader loadError = new FXMLLoader(getClass().getResource("../view/welcomeApplicationError.fxml"), rb);
            welcomeApplicationError = new Scene(loadError.load());
            welcomeApplicationErrorController = loadError.getController();
            
            FXMLLoader loadHelp = new FXMLLoader(getClass().getResource("../view/welcomeApplicationHelp.fxml"), rb);
            welcomeApplicationHelp = new Scene(loadHelp.load());
            welcomeApplicationHelpController = loadHelp.getController();
            
            // add menu buttons to welcome application main window and panes according to exam/std version of the lernsick
            welcomeApplicationStartController.initializeMenu(menuPaneItems);
            
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "There was an error while loading the scene from the fxml files.", ex);
        }
    }
    
    public Scene getMainStage()
    {
        return welcomeApplicationStart;
    }
    
    public Scene getProgressScene() {
        return welcomeApplicationProgress;
    }
    
    public Scene getErrorScene() {
        return welcomeApplicationError;
    }

    public Scene getHelpScene() {
        return welcomeApplicationHelp;
    }
    
    public Scene getPasswordChangeScene() {
        return welcomeApplicationPasswordChange;
    }
    
    public WelcomeApplicationMainController getWelcomeApplicationStart() {
        return welcomeApplicationStartController;
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

    public WelcomeApplicationProgressController getProgress() {
        return welcomeApplicationProgressController;
    }
    
    public WelcomeApplicationErrorController getError() {
        return welcomeApplicationErrorController;
    }
    
    public WelcomeApplicationHelpController getHelp() {
        return welcomeApplicationHelpController;
    }
    
    public WelcomeApplicationPasswordChangeController getPasswordChange() {
        return welcomeApplicationPasswordChangeController;
    }
    
    public WelcomeApplicationRecommendedSoftwareController getRecommended() {
        return welcomeApplicationRecommendedSoftwareController;
    }
    
    public WelcomeApplicationAdditionalSoftwareController getAddSoftware() {
        return welcomeApplicationAdditionalSoftwareController;
    }
    
    public WelcomeApplicationSystemStdController getSystemStd() {
        return welcomeApplicationSystemStdController;
    }
    
    /**
     * Method to create welcome application dialog (main window)
     * 
     * @param parent parent
     * @param scene scene
     * @param title stage title
     * @param modal modal
     * @return 
     */
    public static Stage createDialog(Stage parent, Scene scene, String title, boolean modal) {
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
