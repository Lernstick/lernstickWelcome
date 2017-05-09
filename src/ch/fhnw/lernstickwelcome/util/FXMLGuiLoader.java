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
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

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
            Pair<Scene, WelcomeApplicationMainController> main = 
                    loadScene("../view/welcomeApplicationMain.fxml", stylesheet, rb);
            welcomeApplicationMain = main.getKey();
            mainController = main.getValue();
            
            if(!isExamEnvironment){
                // prepare menu for standard env.
                informationController = loadMenuItemView(
                        "../view/standard/welcomeApplicationInformationStd.fxml", 
                        "welcomeApplicationMain.Information", 
                        "messagebox_info.png", menuPaneItems, rb
                );
                
                recommendedSoftwareController = loadMenuItemView(
                        "../view/standard/welcomeApplicationRecommendedSoftware.fxml", 
                        "welcomeApplicationMain.RecommendedSoftware", 
                        "copyright.png", menuPaneItems, rb
                );
                
                additionalSoftwareController = loadMenuItemView(
                        "../view/standard/welcomeApplicationAdditionalSoftware.fxml", 
                        "welcomeApplicationMain.AdditionalSoftware", 
                        "list-add.png", menuPaneItems, rb
                );
                
                systemStdController = loadMenuItemView(
                        "../view/standard/welcomeApplicationSystemStd.fxml", 
                        "welcomeApplicationMain.System", 
                        "system-run.png", menuPaneItems, rb
                );
            } else {
                // Load password change view.
                Pair<Scene, WelcomeApplicationPasswordChangeController> pc = 
                        loadScene("../view/exam/welcomeApplicationPasswordChange.fxml", stylesheet, rb);
                welcomeApplicationPasswordChange = pc.getKey();
                passwordChangeController = pc.getValue();
                
                // prepare menu for exam env.
                informationController = loadMenuItemView(
                        "../view/exam/welcomeApplicationInformation.fxml", 
                        "welcomeApplicationMain.Information", 
                        "messagebox_info.png", menuPaneItems, rb
                );
                
                firewallController = loadMenuItemView(
                        "../view/exam/welcomeApplicationFirewall.fxml", 
                        "welcomeApplicationMain.Firewall", 
                        "network-server.png", menuPaneItems, rb
                );

                backupController = loadMenuItemView(
                        "../view/exam/welcomeApplicationBackup.fxml", 
                        "welcomeApplicationMain.Backup", 
                        "partitionmanager.png", menuPaneItems, rb
                );

                systemExamController = loadMenuItemView(
                        "../view/exam/welcomeApplicationSystem.fxml", 
                        "welcomeApplicationMain.System", 
                        "system-run.png", menuPaneItems, rb
                );
            }
            
            // Load additional Scenes
            Pair<Scene, WelcomeApplicationProgressController> progress = 
                    loadScene("../view/welcomeApplicationProgress.fxml", stylesheet, rb);
            welcomeApplicationProgress = progress.getKey();
            progressController = progress.getValue();
            
            Pair<Scene, WelcomeApplicationErrorController> error = 
                    loadScene("../view/welcomeApplicationError.fxml", stylesheet, rb);
            welcomeApplicationError = error.getKey();
            errorController = error.getValue();
            
            Pair<Scene, WelcomeApplicationHelpController> help = 
                    loadScene("../view/welcomeApplicationHelp.fxml", stylesheet, rb);
            welcomeApplicationHelp = help.getKey();
            helpController = help.getValue();
            
            
            // add menu buttons to welcome application main window and panes according to exam/std version of the lernstick
            mainController.initializeMenu(menuPaneItems);
            
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "There was an error while loading the scene from the fxml files.", ex);
        }
    }
    
    private <T> T loadMenuItemView(String path, String name, String imgName, ObservableList<MenuPaneItem> menuPaneItems, ResourceBundle rb) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path), rb);
        Parent menuItemView = loader.<Parent>load();
        menuPaneItems.add(new MenuPaneItem(menuItemView, rb.getString(name), WelcomeConstants.ICON_FILE_PATH + "/menu/" + imgName));
        return loader.getController();
    }
    
    private <T> Pair<Scene, T> loadScene(String path, String stylesheetPath, ResourceBundle rb) throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path), rb);
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(stylesheetPath);
            return new Pair<>(scene, loader.getController());
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
     * @param scene scene that has a {@link Region} as Root
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
        stage.setMinHeight(((Region) scene.getRoot()).getMinHeight());
        stage.setMinWidth(((Region) scene.getRoot()).getMinWidth());
        return stage;
    }
}
