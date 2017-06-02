/*
 * Copyright (C) 2017 FHNW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fhnw.lernstickwelcome.util;

import ch.fhnw.lernstickwelcome.view.impl.MenuPaneItem;
import ch.fhnw.lernstickwelcome.fxmlcontroller.standard.AdditionalSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.BackupController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ErrorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.HelpController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.InfodialogController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.MainController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.PasswordChangeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ProgressController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallDependenciesWarningController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallPatternValidatorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.standard.RecommendedSoftwareController;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

    private String stylesheet = FXMLGuiLoader.class.getResource("/css/style.css").toExternalForm();
    private ResourceBundle rb;

    private Scene welcomeApplicationMain;

    private Scene welcomeApplicationProgress;
    private Scene welcomeApplicationError;
    private Scene welcomeApplicationHelp;

    /* exam */
    private Scene welcomeApplicationPasswordChange;
    private Scene welcomeApplicationFirewallDependenciesWarning;
    private Scene welcomeApplicationFirewallPatternValidator;

    // FXMLController
    private ProgressController progressController;
    private ErrorController errorController;
    private HelpController helpController;

    private PasswordChangeController passwordChangeController;
    private BackupController backupController;
    private FirewallController firewallController;
    private ch.fhnw.lernstickwelcome.fxmlcontroller.exam.InformationController informationExamController;
    private ch.fhnw.lernstickwelcome.fxmlcontroller.standard.InformationController informationStdController;
    private MainController mainController;
    private ch.fhnw.lernstickwelcome.fxmlcontroller.exam.SystemController systemExamController;
    private ch.fhnw.lernstickwelcome.fxmlcontroller.standard.SystemController systemStdController;
    private AdditionalSoftwareController additionalSoftwareController;
    private RecommendedSoftwareController recommendedSoftwareController;
    private FirewallPatternValidatorController firewallPatternValidatorController;
    private FirewallDependenciesWarningController firewallDependenciesWarningController;

    /**
     * This class loads the FXML files, creates the menu and saves the fxml
     * controller to provide them to backend binders.
     *
     * @param isExamEnvironment Describes which files should be loaded and added
     * to the menu.
     * @param rb The ResourceBundle for loading the language into the GUI.
     */
    public FXMLGuiLoader(boolean isExamEnvironment, ResourceBundle rb) {
        this.rb = rb;
        // Create all instances with their controllers   
        try {
            ObservableList<MenuPaneItem> menuPaneItems = FXCollections.observableArrayList();

            // load start view
            Pair<Scene, MainController> main
                    = loadScene("/ch/fhnw/lernstickwelcome/view/main.fxml", stylesheet, rb);
            welcomeApplicationMain = main.getKey();
            mainController = main.getValue();

            if (!isExamEnvironment) {
                // prepare menu for standard env.
                informationStdController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/standard/information.fxml",
                        "welcomeApplicationMain.Information",
                        "messagebox_info.png", menuPaneItems, rb
                );

                recommendedSoftwareController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/standard/recommendedSoftware.fxml",
                        "welcomeApplicationMain.RecommendedSoftware",
                        "copyright.png", menuPaneItems, rb
                );

                additionalSoftwareController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/standard/additionalSoftware.fxml",
                        "welcomeApplicationMain.AdditionalSoftware",
                        "list-add.png", menuPaneItems, rb
                );

                systemStdController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/standard/system.fxml",
                        "welcomeApplicationMain.System",
                        "system-run.png", menuPaneItems, rb
                );
            } else {
                // Load password change view.
                Pair<Scene, PasswordChangeController> pc
                        = loadScene("/ch/fhnw/lernstickwelcome/view/exam/passwordChange.fxml", stylesheet, rb);
                welcomeApplicationPasswordChange = pc.getKey();
                passwordChangeController = pc.getValue();

                // Load firewall pattern validator view.
                Pair<Scene, FirewallDependenciesWarningController> fdw
                        = loadScene("/ch/fhnw/lernstickwelcome/view/exam/firewallDependenciesWarning.fxml", stylesheet, rb);
                welcomeApplicationFirewallDependenciesWarning = fdw.getKey();
                firewallDependenciesWarningController = fdw.getValue();

                // Load firewall pattern validator view.
                Pair<Scene, FirewallPatternValidatorController> fpvc
                        = loadScene("/ch/fhnw/lernstickwelcome/view/exam/firewallPatternValidator.fxml", stylesheet, rb);
                welcomeApplicationFirewallPatternValidator = fpvc.getKey();
                firewallPatternValidatorController = fpvc.getValue();

                // prepare menu for exam env.
                informationExamController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/exam/information.fxml",
                        "welcomeApplicationMain.Information",
                        "messagebox_info.png", menuPaneItems, rb
                );

                firewallController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/exam/firewall.fxml",
                        "welcomeApplicationMain.Firewall",
                        "network-server.png", menuPaneItems, rb
                );

                backupController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/exam/backup.fxml",
                        "welcomeApplicationMain.Backup",
                        "partitionmanager.png", menuPaneItems, rb
                );

                systemExamController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/exam/system.fxml",
                        "welcomeApplicationMain.System",
                        "system-run.png", menuPaneItems, rb
                );
            }

            // Load additional Scenes
            Pair<Scene, ProgressController> progress
                    = loadScene("/ch/fhnw/lernstickwelcome/view/progress.fxml", stylesheet, rb);
            welcomeApplicationProgress = progress.getKey();
            progressController = progress.getValue();

            Pair<Scene, ErrorController> error
                    = loadScene("/ch/fhnw/lernstickwelcome/view/error.fxml", stylesheet, rb);
            welcomeApplicationError = error.getKey();
            errorController = error.getValue();

            Pair<Scene, HelpController> help
                    = loadScene("/ch/fhnw/lernstickwelcome/view/help.fxml", stylesheet, rb);
            welcomeApplicationHelp = help.getKey();
            helpController = help.getValue();

            // add menu buttons to welcome application main window and panes according to exam/std version of the lernstick
            mainController.initializeMenu(menuPaneItems);

        } catch (IOException ex) {
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
     *
     * @return the view, which contains the menu on the left and the content on
     * the right.
     */
    public Scene getMainStage() {
        return welcomeApplicationMain;
    }

    /**
     * Returns the installation/configuration view.
     *
     * @return the view, which contains the progress bar, which is shown on save
     * and exit.
     */
    public Scene getProgressScene() {
        return welcomeApplicationProgress;
    }

    /**
     * Returns the error dialog scene.
     *
     * @return the view, which can be shown to the user when an Exception
     * occures.
     */
    public Scene getErrorScene() {
        return welcomeApplicationError;
    }

    /**
     * Returns the help dialog scene.
     *
     * @return the view, which is shown to the user when he wants to see the
     * help.
     */
    public Scene getHelpScene() {
        return welcomeApplicationHelp;
    }

    /**
     * Returns the password change dialog scene.
     *
     * @return the view, which is shown at the start of the exam version which
     * recommends to change the password.
     */
    public Scene getPasswordChangeScene() {
        return welcomeApplicationPasswordChange;
    }

    /**
     * Returns the firewall dependencies warning dialog scene.
     *
     * @return the view, which is shown when a user wants to open the pattern
     * validator scene.
     */
    public Scene getFirewallDependenciesWarning() {
        return welcomeApplicationFirewallDependenciesWarning;
    }

    /**
     * Returns the firewall pattern validator dialog scene.
     *
     * @return the view, which is shown while checking for dependencies for
     * firewall patterns blocked by squid.
     */
    public Scene getPatternValidatorScene() {
        return welcomeApplicationFirewallPatternValidator;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public MainController getMainController() {
        return mainController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public ch.fhnw.lernstickwelcome.fxmlcontroller.exam.SystemController getSystemExamController() {
        return systemExamController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public FirewallController getFirewallController() {
        return firewallController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public ch.fhnw.lernstickwelcome.fxmlcontroller.exam.InformationController getInformationExamController() {
        return informationExamController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public BackupController getBackupController() {
        return backupController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public ProgressController getProgressController() {
        return progressController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public ErrorController getErrorController() {
        return errorController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public HelpController getHelpController() {
        return helpController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public PasswordChangeController getPasswordChangeController() {
        return passwordChangeController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public FirewallPatternValidatorController getFirewallPatternValidatorController() {
        return firewallPatternValidatorController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public FirewallDependenciesWarningController getFirewallDependenciesWarningController() {
        return firewallDependenciesWarningController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public ch.fhnw.lernstickwelcome.fxmlcontroller.standard.InformationController getInformationStdController() {
        return informationStdController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public RecommendedSoftwareController getRecommendedController() {
        return recommendedSoftwareController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public AdditionalSoftwareController getAddSoftwareController() {
        return additionalSoftwareController;
    }

    /**
     * Returns the Controller for this fxml view.
     *
     * @return
     */
    public ch.fhnw.lernstickwelcome.fxmlcontroller.standard.SystemController getSystemStdController() {
        return systemStdController;
    }

    /**
     * Loads an info text dialog with a specific action.
     * @param parent the parent stage
     * @param textid the id of the text for this dialog
     * @param e the action on ok
     * @return a modal info stage
     * @throws IOException 
     */
    public Stage getInfotextdialog(Stage parent, String textid, 
            EventHandler<ActionEvent> e) throws IOException {
        Pair<Scene, InfodialogController> p
                = loadScene("/ch/fhnw/lernstickwelcome/view/infodialog.fxml", stylesheet, rb);
        p.getValue().initDialog(textid, e);
        return createDialog(parent, p.getKey(), 
                rb.getString("welcomeApplicationInfodialog.title"), true);
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
        if (modal) {
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(modal);
        }
        stage.setTitle(title);
        stage.setScene(scene);
        if (scene.getRoot() instanceof Region) {
            Region rootElement = (Region) scene.getRoot();
            if (rootElement.getMinHeight() != 0) {
                stage.setMinHeight(rootElement.getMinHeight());
            }
            if (rootElement.getMinWidth() != 0) {
                stage.setMinWidth(((Region) scene.getRoot()).getMinWidth());
            }
        }
        return stage;
    }
}
