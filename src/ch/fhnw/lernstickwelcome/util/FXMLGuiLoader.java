package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.fxmlcontroller.MenuPaneItem;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationAdditionalSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationBackupController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationFirewallController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationHelpController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationInformationController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationMainController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationPasswordChangeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationProgressController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationRecommendedSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.WelcomeApplicationSystemStdController;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import java.io.File;
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
 * @author sschw
 */
public class FXMLGuiLoader {
    private static final Logger LOGGER = Logger.getLogger(FXMLGuiLoader.class.getName());
    
    private Scene welcomeApplicationMain;
    
    private Scene welcomeApplicationProgress;
    private Scene welcomeApplicationError;
    private Scene welcomeApplicationHelp;
    
    /* exam */
    private Scene welcomeApplicationPasswordChange;
    
    
    // FXMLController
    private WelcomeApplicationProgressController progressController;
    private WelcomeApplicationErrorController errorController;
    private WelcomeApplicationHelpController helpController;
    
    private WelcomeApplicationPasswordChangeController passwordChangeController;
    private WelcomeApplicationBackupController backupController;
    private WelcomeApplicationFirewallController firewallController;
    private WelcomeApplicationInformationController informationController;
    private WelcomeApplicationMainController mainController;
    private WelcomeApplicationSystemController systemExamController;
    private WelcomeApplicationSystemStdController systemStdController;
    private WelcomeApplicationAdditionalSoftwareController additionalSoftwareController;
    private WelcomeApplicationRecommendedSoftwareController recommendedSoftwareController; 
    
    /**
     * This class loads the FXML files, creates the menu and saves the fxml 
     * controller to provide them to backend binders.
     * 
     * @param isExamEnvironment Describes which files should be loaded and added
     * to the menu.
     * @param rb The ResourceBundle for loading the language into the GUI. 
     */
    public FXMLGuiLoader(boolean isExamEnvironment, ResourceBundle rb) {
        // Create all instances with their controllers   
        try {
            ObservableList<MenuPaneItem> menuPaneItems = FXCollections.observableArrayList();
            String stylesheet = new File(WelcomeConstants.RESOURCE_FILE_PATH + "/css/style.css").toURI().toString();
            
            // load start view
            FXMLLoader loadStart = new FXMLLoader(getClass().getResource("../view/welcomeApplicationMain.fxml"), rb);
            welcomeApplicationMain = new Scene(loadStart.load());
            welcomeApplicationMain.getStylesheets().add(stylesheet);
            mainController = loadStart.getController();
            
            if(!isExamEnvironment){
                FXMLLoader loadInfoStd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationInformationStd.fxml"), rb);
                Parent informationStd = loadInfoStd.<Parent>load();
                informationController = loadInfoStd.getController();
                        
                FXMLLoader loadRecomm = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationRecommendedSoftware.fxml"), rb);
                Parent recommended = loadRecomm.<Parent>load();
                recommendedSoftwareController = loadRecomm.getController();
                        
                FXMLLoader loadAdd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationAdditionalSoftware.fxml"), rb);
                Parent addSoftware = loadAdd.<Parent>load();
                additionalSoftwareController = loadAdd.getController();
                        
                FXMLLoader loadSystemStd = new FXMLLoader(getClass().getResource("../view/standard/welcomeApplicationSystemStd.fxml"), rb);
                Parent systemStd = loadSystemStd.<Parent>load();
                systemStdController = loadSystemStd.getController();
                
                //add to pane list for the standard version
                menuPaneItems.add(new MenuPaneItem(informationStd, rb.getString("welcomeApplicationMain.Information"), WelcomeConstants.ICON_FILE_PATH + "/menu/messagebox_info.png"));
                menuPaneItems.add(new MenuPaneItem(recommended, rb.getString("welcomeApplicationMain.RecommendedSoftware"), WelcomeConstants.ICON_FILE_PATH + "/menu/copyright.png"));
                menuPaneItems.add(new MenuPaneItem(addSoftware, rb.getString("welcomeApplicationMain.AdditionalSoftware"), WelcomeConstants.ICON_FILE_PATH + "/menu/list-add.png"));
                menuPaneItems.add(new MenuPaneItem(systemStd, rb.getString("welcomeApplicationMain.System"), WelcomeConstants.ICON_FILE_PATH + "/menu/system-run.png"));
            } else {
                FXMLLoader loadPasswordChange = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationPasswordChange.fxml"), rb);
                welcomeApplicationPasswordChange = new Scene(loadPasswordChange.load());
                welcomeApplicationPasswordChange.getStylesheets().add(stylesheet);
                passwordChangeController = loadPasswordChange.getController();
                
                FXMLLoader loadInfo = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationInformation.fxml"), rb);
                Parent information = loadInfo.<Parent>load();
                informationController = loadInfo.getController();
                
                FXMLLoader loadFirewall = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationFirewall.fxml"), rb);
                Parent firewall = loadFirewall.<Parent>load();
                firewallController = loadFirewall.getController();

                FXMLLoader loadBackup = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationBackup.fxml"), rb);
                Parent backup = loadBackup.<Parent>load();
                backupController = loadBackup.getController();

                FXMLLoader loadSystem = new FXMLLoader(getClass().getResource("../view/exam/welcomeApplicationSystem.fxml"), rb);
                Parent system = loadSystem.<Parent>load();
                systemExamController = loadSystem.getController();

                //add to pane list for the exam version
                menuPaneItems.add(new MenuPaneItem(information, rb.getString("welcomeApplicationMain.Information"), WelcomeConstants.ICON_FILE_PATH + "/menu/messagebox_info.png"));
                menuPaneItems.add(new MenuPaneItem(firewall, rb.getString("welcomeApplicationMain.Firewall"), WelcomeConstants.ICON_FILE_PATH + "/menu/network-server.png"));
                menuPaneItems.add(new MenuPaneItem(backup, rb.getString("welcomeApplicationMain.Backup"), WelcomeConstants.ICON_FILE_PATH + "/menu/partitionmanager.png"));
                menuPaneItems.add(new MenuPaneItem(system, rb.getString("welcomeApplicationMain.System"), WelcomeConstants.ICON_FILE_PATH + "/menu/system-run.png"));
            }
            
            FXMLLoader loadProgress = new FXMLLoader(getClass().getResource("../view/welcomeApplicationProgress.fxml"), rb);
            welcomeApplicationProgress = new Scene(loadProgress.load());
            welcomeApplicationProgress.getStylesheets().add(stylesheet);
            progressController = loadProgress.getController();
            
            FXMLLoader loadError = new FXMLLoader(getClass().getResource("../view/welcomeApplicationError.fxml"), rb);
            welcomeApplicationError = new Scene(loadError.load());
            welcomeApplicationError.getStylesheets().add(stylesheet);
            errorController = loadError.getController();
            
            FXMLLoader loadHelp = new FXMLLoader(getClass().getResource("../view/welcomeApplicationHelp.fxml"), rb);
            welcomeApplicationHelp = new Scene(loadHelp.load());
            welcomeApplicationHelp.getStylesheets().add(stylesheet);
            helpController = loadHelp.getController();
            
            
            // add menu buttons to welcome application main window and panes according to exam/std version of the lernstick
            mainController.initializeMenu(menuPaneItems);
            
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "There was an error while loading the scene from the fxml files.", ex);
        }
    }
   
    /**
     * Returns the main view.
     * @return the view, which contains the menu on the left and the content on 
     * the right.
     */
    public Scene getMainStage()
    {
        return welcomeApplicationMain;
    }
    
    /**
     * Returns the installation/configuration view.
     * @return the view, which contains the progress bar, which is shown on
     * save and exit.
     */
    public Scene getProgressScene() {
        return welcomeApplicationProgress;
    }
    
    /**
     * Returns the error dialog scene.
     * @return the view, which can be shown to the user when an Exception 
     * occures.
     */
    public Scene getErrorScene() {
        return welcomeApplicationError;
    }

    /**
     * Returns the help dialog scene.
     * @return the view, which is shown to the user when he wants to see the 
     * help.
     */
    public Scene getHelpScene() {
        return welcomeApplicationHelp;
    }
    
    /**
     * Returns the password change dialog scene.
     * @return the view, which is shown at the start of the exam version which
     * recommends to change the password.
     */
    public Scene getPasswordChangeScene() {
        return welcomeApplicationPasswordChange;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationMainController getMainController() {
        return mainController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationSystemController getSystemExamController() {
        return systemExamController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationFirewallController getFirewallController() {
        return firewallController;
    }

    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationInformationController getInformationController() {
        return informationController;
    }

    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationBackupController getBackupController() {
        return backupController;
    }

    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationProgressController getProgressController() {
        return progressController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationErrorController getErrorController() {
        return errorController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationHelpController getHelpController() {
        return helpController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationPasswordChangeController getPasswordChangeController() {
        return passwordChangeController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationRecommendedSoftwareController getRecommendedController() {
        return recommendedSoftwareController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationAdditionalSoftwareController getAddSoftwareController() {
        return additionalSoftwareController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public WelcomeApplicationSystemStdController getSystemStdController() {
        return systemStdController;
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
