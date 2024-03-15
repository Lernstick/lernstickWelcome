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

import ch.fhnw.lernstickwelcome.view.impl.MainMenuItem;
import ch.fhnw.lernstickwelcome.fxmlcontroller.standard.AdditionalSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.BackupController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.HelpController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.InfodialogController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.MainController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.PasswordChangeController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.ProgressController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.ExamSystemController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallDependenciesWarningController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.FirewallPatternValidatorController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.standard.NonFreeSoftwareController;
import ch.fhnw.lernstickwelcome.fxmlcontroller.standard.StandardSystemController;
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
import javafx.scene.Node;
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
public final class FXMLGuiLoader {

    private static final Logger LOGGER
            = Logger.getLogger(FXMLGuiLoader.class.getName());

    private String stylesheet = FXMLGuiLoader.class.getResource(
            "/css/style.css").toExternalForm();
    private ResourceBundle resourceBundle;

    // common scenes
    private Scene mainScene;
    private Scene progressScene;
    private Scene errorScene;
    private Scene helpScene;

    // exam scenes
    private Scene passwordChangeScene;
    private Scene firewallDependenciesWarningScene;
    private Scene firewallPatternValidatorScene;

    // FXMLController
    private ProgressController progressController;
    private HelpController helpController;
    private PasswordChangeController passwordChangeController;
    private BackupController backupController;
    private FirewallController firewallController;
    private ch.fhnw.lernstickwelcome.fxmlcontroller.exam.InformationController informationExamController;
    private ch.fhnw.lernstickwelcome.fxmlcontroller.standard.InformationController informationStdController;
    private MainController mainController;
    private ExamSystemController examSystemController;
    private StandardSystemController standardSystemController;
    private AdditionalSoftwareController additionalSoftwareController;
    private NonFreeSoftwareController nonFreeSoftwareController;
    private FirewallPatternValidatorController firewallPatternValidatorController;
    private FirewallDependenciesWarningController firewallDependenciesWarningController;

    /**
     * This class loads the FXML files, creates the menu and saves the fxml
     * controller to provide them to backend binders.
     *
     * @param isExamEnvironment Describes which files should be loaded and added
     * to the menu.
     * @param resourceBundle The ResourceBundle for loading the language into
     * the GUI.
     */
    public FXMLGuiLoader(boolean isExamEnvironment,
            ResourceBundle resourceBundle) {

        this.resourceBundle = resourceBundle;

        // Create all instances with their controllers   
        try {
            ObservableList<MainMenuItem> menuPaneItems
                    = FXCollections.observableArrayList();

            // load main view
            Pair<Scene, MainController> mainPair = loadScene(
                    "/ch/fhnw/lernstickwelcome/view/MainView.fxml",
                    stylesheet, resourceBundle);
            mainScene = mainPair.getKey();
            mainController = mainPair.getValue();

            if (isExamEnvironment) {
                // load password change view
                Pair<Scene, PasswordChangeController> passwordChangePair
                        = loadScene(
                                "/ch/fhnw/lernstickwelcome/view/exam/passwordChange.fxml",
                                stylesheet, resourceBundle);
                passwordChangeScene = passwordChangePair.getKey();
                passwordChangeController = passwordChangePair.getValue();

                // load firewall dependencies warning view
                Pair<Scene, FirewallDependenciesWarningController> firewallDependenciesWarningPair
                        = loadScene("/ch/fhnw/lernstickwelcome/view/exam/firewallDependenciesWarning.fxml",
                                stylesheet, resourceBundle);
                firewallDependenciesWarningScene
                        = firewallDependenciesWarningPair.getKey();
                firewallDependenciesWarningController
                        = firewallDependenciesWarningPair.getValue();

                // load firewall pattern validator view
                Pair<Scene, FirewallPatternValidatorController> firewallPatternValidatorPair
                        = loadScene("/ch/fhnw/lernstickwelcome/view/exam/firewallPatternValidator.fxml",
                                stylesheet, resourceBundle);
                firewallPatternValidatorScene
                        = firewallPatternValidatorPair.getKey();
                firewallPatternValidatorController
                        = firewallPatternValidatorPair.getValue();

                // load menu items for exam environment
                informationExamController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/exam/InformationView.fxml",
                        "welcomeApplicationMain.Information",
                        "messagebox_info.png", menuPaneItems, resourceBundle
                );
                firewallController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/exam/FirewallView.fxml",
                        "welcomeApplicationMain.Firewall",
                        "network-server.png", menuPaneItems, resourceBundle
                );
                backupController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/exam/BackupView.fxml",
                        "welcomeApplicationMain.Backup",
                        "partitionmanager.png", menuPaneItems, resourceBundle
                );
                examSystemController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/exam/ExamSystemView.fxml",
                        "Additional_Settings",
                        "system-run.png", menuPaneItems, resourceBundle
                );

            } else {
                // load menu items for exam environment
                informationStdController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/standard/InformationView.fxml",
                        "welcomeApplicationMain.Information",
                        "messagebox_info.png", menuPaneItems, resourceBundle
                );
                nonFreeSoftwareController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/standard/NonFreeSoftware.fxml",
                        "NonfreeApplication.title",
                        "copyright.png", menuPaneItems, resourceBundle
                );
                additionalSoftwareController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/standard/AdditionalSoftware.fxml",
                        "Additional_Software",
                        "list-add.png", menuPaneItems, resourceBundle
                );
                standardSystemController = loadMenuItemView(
                        "/ch/fhnw/lernstickwelcome/view/standard/StandardSystemView.fxml",
                        "Additional_Settings",
                        "system-run.png", menuPaneItems, resourceBundle
                );
            }

            // load additional scenes
            Pair<Scene, ProgressController> progressPair = loadScene(
                    "/ch/fhnw/lernstickwelcome/view/ProgressView.fxml",
                    stylesheet, resourceBundle);
            progressScene = progressPair.getKey();
            progressController = progressPair.getValue();

            // HACK: currently loading the help crashes openjfx on arm64
            if (!WelcomeUtil.ARCHITECTURE.equals("aarch64")) {
                Pair<Scene, HelpController> helpPair = loadScene(
                        "/ch/fhnw/lernstickwelcome/view/help.fxml",
                        stylesheet, resourceBundle);
                helpScene = helpPair.getKey();
                helpController = helpPair.getValue();                
            }
           
            // add menu buttons to welcome application main window and panes
            // according to exam/std version of the lernstick
            mainController.initializeMenu(menuPaneItems);

        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "There was an error while loading the "
                    + "scene from the fxml files.", ex);
        }
    }

    private <T> T loadMenuItemView(String path, String name, String imageName,
            ObservableList<MainMenuItem> mainMenuItems,
            ResourceBundle resourceBundle) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(path), resourceBundle);

        Node menuItemView = loader.load();

        mainMenuItems.add(new MainMenuItem(
                WelcomeConstants.ICON_FILE_PATH + "/menu/" + imageName,
                resourceBundle.getString(name), menuItemView));

        return loader.getController();
    }

    private <T> Pair<Scene, T> loadScene(String path, String stylesheetPath,
            ResourceBundle resourceBundle) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(path), resourceBundle);

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
        return mainScene;
    }

    /**
     * Returns the progress scene
     *
     * @return the progress scene
     */
    public Scene getProgressScene() {
        return progressScene;
    }

    /**
     * Returns the error dialog scene.
     *
     * @return the view, which can be shown to the user when an Exception
     * occures.
     */
    public Scene getErrorScene() {
        return errorScene;
    }

    /**
     * Returns the help dialog scene.
     *
     * @return the view, which is shown to the user when he wants to see the
     * help.
     */
    public Scene getHelpScene() {
        return helpScene;
    }

    /**
     * Returns the password change dialog scene.
     *
     * @return the view, which is shown at the start of the exam version which
     * recommends to change the password.
     */
    public Scene getPasswordChangeScene() {
        return passwordChangeScene;
    }

    /**
     * Returns the firewall dependencies warning dialog scene.
     *
     * @return the view, which is shown when a user wants to open the pattern
     * validator scene.
     */
    public Scene getFirewallDependenciesWarning() {
        return firewallDependenciesWarningScene;
    }

    /**
     * Returns the firewall pattern validator dialog scene.
     *
     * @return the view, which is shown while checking for dependencies for
     * firewall patterns blocked by squid.
     */
    public Scene getPatternValidatorScene() {
        return firewallPatternValidatorScene;
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
    public ExamSystemController getExamSystemController() {
        return examSystemController;
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
    public NonFreeSoftwareController getNonFreeController() {
        return nonFreeSoftwareController;
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
    public StandardSystemController getStandardSystemController() {
        return standardSystemController;
    }

    /**
     * Loads an info text dialog with a specific action.
     *
     * @param parent the parent stage
     * @param textid the id of the text for this dialog
     * @param e the action on ok
     * @return a modal info stage
     * @throws IOException
     */
    public Stage getInfotextDialog(Stage parent, String textid,
            EventHandler<ActionEvent> e) throws IOException {

        Pair<Scene, InfodialogController> pair = loadScene(
                "/ch/fhnw/lernstickwelcome/view/infodialog.fxml",
                stylesheet, resourceBundle);

        pair.getValue().initDialog(textid, e);

        return createDialog(parent, pair.getKey(),
                resourceBundle.getString("welcomeApplicationInfodialog.title"),
                true);
    }

    /**
     * creates a dialog
     *
     * @param parent parent the parent of the dialog
     * @param scene scene the scene to show in the dialog
     * @param title stage title the stage title
     * @param modal modal if the dialog should be modal
     * @return the dialog
     */
    public static Stage createDialog(
            Stage parent, Scene scene, String title, boolean modal) {

        Stage stage = new Stage();
        stage.initOwner(parent);
        if (modal) {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.setTitle(title);
        stage.setScene(scene);
        return stage;
    }
}
