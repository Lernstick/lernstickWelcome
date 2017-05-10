package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.view.impl.MenuPaneItem;
import ch.fhnw.lernstickwelcome.fxmlcontroller.AdditionalSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.BackupController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.FirewallController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.HelpController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.InformationController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.MainController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.PasswordChangeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ProgressController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.RecommendedSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.SystemController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.SystemStdController;
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
    private ProgressController progressController;
    private ErrorController errorController;
    private HelpController helpController;
    
    private PasswordChangeController passwordChangeController;
    private BackupController backupController;
    private FirewallController firewallController;
    private InformationController informationController;
    private MainController mainController;
    private SystemController systemExamController;
    private SystemStdController systemStdController;
    private AdditionalSoftwareController additionalSoftwareController;
    private RecommendedSoftwareController recommendedSoftwareController; 
    
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
            Pair<Scene, MainController> main = 
                    loadScene("../view/main.fxml", stylesheet, rb);
            welcomeApplicationMain = main.getKey();
            mainController = main.getValue();
            
            if(!isExamEnvironment){
                // prepare menu for standard env.
                informationController = loadMenuItemView(
                        "../view/standard/informationStd.fxml", 
                        "welcomeApplicationMain.Information", 
                        "messagebox_info.png", menuPaneItems, rb
                );
                
                recommendedSoftwareController = loadMenuItemView(
                        "../view/standard/recommendedSoftware.fxml", 
                        "welcomeApplicationMain.RecommendedSoftware", 
                        "copyright.png", menuPaneItems, rb
                );
                
                additionalSoftwareController = loadMenuItemView(
                        "../view/standard/additionalSoftware.fxml", 
                        "welcomeApplicationMain.AdditionalSoftware", 
                        "list-add.png", menuPaneItems, rb
                );
                
                systemStdController = loadMenuItemView(
                        "../view/standard/systemStd.fxml", 
                        "welcomeApplicationMain.System", 
                        "system-run.png", menuPaneItems, rb
                );
            } else {
                // Load password change view.
                Pair<Scene, PasswordChangeController> pc = 
                        loadScene("../view/exam/passwordChange.fxml", stylesheet, rb);
                welcomeApplicationPasswordChange = pc.getKey();
                passwordChangeController = pc.getValue();
                
                // prepare menu for exam env.
                informationController = loadMenuItemView(
                        "../view/exam/information.fxml", 
                        "welcomeApplicationMain.Information", 
                        "messagebox_info.png", menuPaneItems, rb
                );
                
                firewallController = loadMenuItemView(
                        "../view/exam/firewall.fxml", 
                        "welcomeApplicationMain.Firewall", 
                        "network-server.png", menuPaneItems, rb
                );

                backupController = loadMenuItemView(
                        "../view/exam/backup.fxml", 
                        "welcomeApplicationMain.Backup", 
                        "partitionmanager.png", menuPaneItems, rb
                );

                systemExamController = loadMenuItemView(
                        "../view/exam/system.fxml", 
                        "welcomeApplicationMain.System", 
                        "system-run.png", menuPaneItems, rb
                );
            }
            
            // Load additional Scenes
            Pair<Scene, ProgressController> progress = 
                    loadScene("../view/progress.fxml", stylesheet, rb);
            welcomeApplicationProgress = progress.getKey();
            progressController = progress.getValue();
            
            Pair<Scene, ErrorController> error = 
                    loadScene("../view/error.fxml", stylesheet, rb);
            welcomeApplicationError = error.getKey();
            errorController = error.getValue();
            
            Pair<Scene, HelpController> help = 
                    loadScene("../view/help.fxml", stylesheet, rb);
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
    public MainController getMainController() {
        return mainController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public SystemController getSystemExamController() {
        return systemExamController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public FirewallController getFirewallController() {
        return firewallController;
    }

    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public InformationController getInformationController() {
        return informationController;
    }

    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public BackupController getBackupController() {
        return backupController;
    }

    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public ProgressController getProgressController() {
        return progressController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public ErrorController getErrorController() {
        return errorController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public HelpController getHelpController() {
        return helpController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public PasswordChangeController getPasswordChangeController() {
        return passwordChangeController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public RecommendedSoftwareController getRecommendedController() {
        return recommendedSoftwareController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public AdditionalSoftwareController getAddSoftwareController() {
        return additionalSoftwareController;
    }
    
    /**
     * Returns the Controller for this fxml view.
     * @return 
     */
    public SystemStdController getSystemStdController() {
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
        if(scene.getRoot() instanceof Region) {
            Region rootElement = (Region) scene.getRoot();
            if(rootElement.getMinHeight() != 0)
                stage.setMinHeight(rootElement.getMinHeight());
            if(rootElement.getMinWidth() != 0)
                stage.setMinWidth(((Region) scene.getRoot()).getMinWidth());
        }
        return stage;
    }
}
