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
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.controller.binder.AbstractSystemBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ApplicationBinder;
import ch.fhnw.lernstickwelcome.controller.binder.exam.BackupBinder;
import ch.fhnw.lernstickwelcome.controller.binder.exam.FirewallBinder;
import ch.fhnw.lernstickwelcome.controller.binder.exam.PasswordChangeBinder;
import ch.fhnw.lernstickwelcome.controller.binder.HelpBinder;
import ch.fhnw.lernstickwelcome.controller.binder.MainBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ProgressBinder;
import ch.fhnw.lernstickwelcome.controller.binder.SystemBinder;
import ch.fhnw.lernstickwelcome.controller.binder.exam.ExamSystemBinder;
import ch.fhnw.lernstickwelcome.controller.binder.exam.FirewallDependenciesWarningBinder;
import ch.fhnw.lernstickwelcome.controller.binder.exam.FirewallPatternValidatorBinder;
import ch.fhnw.lernstickwelcome.controller.binder.standard.StandardSystemBinder;
import ch.fhnw.lernstickwelcome.util.FXMLGuiLoader;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * The JavaFX Application.
 * <p>
 * This class starts the application by fulfilling the following tasks:
 * <ol>
 * <li>Create the WelcomeController</li>
 * <li>Create the FXMLGuiLoader</li>
 * <li>Initialize the Error Dialog</li>
 * <li>Initialize the Help Dialog</li>
 * <li>Load the Backend with the Welcome Controller for the given
 * environment</li>
 * <li>Create the needed Binder and bind the Backend to the Views</li>
 * <li>Create the Progress Dialog</li>
 * <li>Create the scene for the Welcome Application</li>
 * <li>Initialize the stage with the main scene</li>
 * </ol>
 * </p>
 *
 * @author sschw
 */
public final class WelcomeApplication extends Application {

    private static final Logger LOGGER
            = Logger.getLogger(WelcomeApplication.class.getName());
    private WelcomeController controller;
    private FXMLGuiLoader guiLoader;
    private Stage passwordChangeStage;

    /**
     * Initializes the stage.
     * <ol>
     * <li>Create the WelcomeController</li>
     * <li>Create the FXMLGuiLoader</li>
     * <li>Initialize the Error Dialog</li>
     * <li>Initialize the Help Dialog</li>
     * <li>Load the Backend with the Welcome Controller for the given
     * environment</li>
     * <li>Create the needed Binder and bind the Backend to the Views</li>
     * <li>Create the Progress Dialog</li>
     * <li>Create the scene for the Welcome Application</li>
     * <li>Initialize the stage with the main scene</li>
     * <li>Register a close event for the primaryStage which shows warnings</li>
     * </ol>
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            controller = new WelcomeController();

            guiLoader = new FXMLGuiLoader(
                    isExamEnvironment(), controller.getBundle());

            Stage errorStage = FXMLGuiLoader.createDialog(
                    primaryStage,
                    guiLoader.getErrorScene(),
                    controller.getBundle().getString(
                            "welcomeApplicationError.title"),
                    true
            );

            Stage helpStage = FXMLGuiLoader.createDialog(
                    primaryStage,
                    guiLoader.getHelpScene(),
                    controller.getBundle().getString(
                            "welcomeApplicationHelp.title"),
                    false
            );

            if (isExamEnvironment()) {

                controller.loadExamEnvironment();

                if (controller.getSystemConfigTask().showPasswordDialog()) {

                    PasswordChangeBinder examPasswordChangeBinder
                            = new PasswordChangeBinder(controller,
                                    guiLoader.getPasswordChangeController());

                    examPasswordChangeBinder.initHandlers(
                            errorStage, guiLoader.getErrorController());

                    passwordChangeStage = FXMLGuiLoader.createDialog(
                            primaryStage,
                            guiLoader.getPasswordChangeScene(),
                            controller.getBundle().getString(
                                    "welcomeApplicationPasswordChange.title"),
                            false
                    );

                    passwordChangeStage.showAndWait();
                }

                Stage firewallPatternValidatorStage = FXMLGuiLoader.createDialog(
                        primaryStage,
                        guiLoader.getPatternValidatorScene(),
                        controller.getBundle().getString(
                                "welcomeApplicationFirewallPatternValidator.title"),
                        true
                );

                FirewallPatternValidatorBinder firewallPatternValidatorBinder
                        = new FirewallPatternValidatorBinder(controller,
                                guiLoader.getFirewallPatternValidatorController());
                firewallPatternValidatorBinder.initBindings();
                firewallPatternValidatorBinder.initHandlers(
                        firewallPatternValidatorStage);

                Stage firewallDependenciesWarningStage = FXMLGuiLoader.createDialog(
                        primaryStage,
                        guiLoader.getFirewallDependenciesWarning(),
                        controller.getBundle().getString(
                                "welcomeApplicationFirewallDependenciesWarning.title"),
                        true
                );

                FirewallDependenciesWarningBinder fdwBinder
                        = new FirewallDependenciesWarningBinder(controller,
                                guiLoader.getFirewallDependenciesWarningController());
                fdwBinder.initHandlers(firewallPatternValidatorStage,
                        guiLoader.getErrorController(), errorStage);

                HelpBinder helpBinder = new HelpBinder(controller,
                        guiLoader.getHelpController());
                helpBinder.initBindings();
                helpBinder.initHandlers();

                ch.fhnw.lernstickwelcome.controller.binder.exam.InformationBinder examInformationBinder
                        = new ch.fhnw.lernstickwelcome.controller.binder.exam.InformationBinder(
                                controller, guiLoader.getInformationExamController());
                examInformationBinder.initBindings();

                FirewallBinder examFirewallBinder = new FirewallBinder(
                        controller, guiLoader.getFirewallController());
                examFirewallBinder.initBindings();
                examFirewallBinder.initHandlers(
                        firewallDependenciesWarningStage,
                        firewallPatternValidatorStage,
                        errorStage, guiLoader.getErrorController());
                examFirewallBinder.initHelp(helpStage, helpBinder);

                BackupBinder examBackupBinder = new BackupBinder(controller,
                        guiLoader.getBackupController());
                examBackupBinder.initBindings();
                examBackupBinder.initHelp(helpStage, helpBinder);

                SystemBinder binder = new ExamSystemBinder(
                        controller, guiLoader.getExamSystemController());
                binder.initBindings();
                binder.initHelp(helpStage, helpBinder);
                
            } else {
                controller.loadStandardEnvironment();

                HelpBinder helpBinder = new HelpBinder(
                        controller, guiLoader.getHelpController());
                helpBinder.initBindings();
                helpBinder.initHandlers();

                ch.fhnw.lernstickwelcome.controller.binder.standard.InformationBinder information
                        = new ch.fhnw.lernstickwelcome.controller.binder.standard.InformationBinder(
                                controller, guiLoader.getInformationStdController());
                information.initBindings();

                ApplicationBinder nonFreeAppsBinder = new ApplicationBinder(
                        controller,
                        guiLoader.getNonFreeController().getVbApps(),
                        guiLoader.getNonFreeController().getHelpButton()
                );
                nonFreeAppsBinder.addApplicationGroup(
                        controller.getRecommendedAppsTask(),
                        helpBinder, helpStage);
                nonFreeAppsBinder.addApplicationGroup(
                        controller.getUtilityAppsTask(),
                        helpBinder, helpStage);
                nonFreeAppsBinder.initHelp("1", helpStage, helpBinder);

                ApplicationBinder addAppsBinder = new ApplicationBinder(
                        controller,
                        guiLoader.getAddSoftwareController().getVbApps(),
                        guiLoader.getAddSoftwareController().getHelpButton()
                );
                addAppsBinder.addApplicationGroup(
                        controller.getTeachAppsTask(), helpBinder, helpStage);
                addAppsBinder.addApplicationGroup(
                        controller.getSoftwAppsTask(), helpBinder, helpStage);
                addAppsBinder.addApplicationGroup(
                        controller.getGamesAppsTask(), helpBinder, helpStage);
                addAppsBinder.initHelp("2", helpStage, helpBinder);

                SystemBinder binder = new StandardSystemBinder(
                        controller, guiLoader.getStandardSystemController());
                binder.initBindings();
                binder.initHelp(helpStage, helpBinder);
            }

            ProgressBinder progressBinder = new ProgressBinder(
                    controller, guiLoader.getProgressController());
            progressBinder.initBindings();
            progressBinder.initHandlers(errorStage,
                    guiLoader.getErrorController());
            Stage progressStage = FXMLGuiLoader.createDialog(
                    primaryStage,
                    guiLoader.getProgressScene(),
                    controller.getBundle().getString(
                            "welcomeApplicationProgress.save"),
                    true
            );

            MainBinder mainBinder = new MainBinder(
                    controller, guiLoader.getMainController());
            mainBinder.initHandlers(progressStage);

            Scene scene = guiLoader.getMainStage();
            primaryStage.setTitle(
                    controller.getBundle().getString("Welcome.title"));
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setMinHeight(primaryStage.getHeight());
            primaryStage.setMinWidth(primaryStage.getWidth());
            primaryStage.getIcons().add(new Image(
                    getClass().getResourceAsStream("/icon/lernstick_usb.png")));

            // Set close warnings
            primaryStage.setOnCloseRequest(evt -> {
                try {
                    if (isExamEnvironment()) {
                        if (controller.getSystemConfigTask().showPasswordDialog()) {
                            passwordChangeStage.showAndWait();
                        }
                        if (controller.getBackupTask().hasExchangePartition()
                                && !controller.getBackupTask().isBackupConfigured()) {
                            guiLoader.getInfotextdialog(primaryStage,
                                    "WelcomeApplication.Warning_No_Backup_Configured", e -> {
                                        guiLoader.getMainController().setView(2);
                                        ((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
                                        evt.consume();
                                    }).showAndWait();
                        }
                        if (WelcomeUtil.isFileSystemMountAllowed() && !evt.isConsumed()) {
                            guiLoader.getInfotextdialog(primaryStage,
                                    "WelcomeApplication.Warning_Mount_Allowed", e -> {
                                        guiLoader.getMainController().setView(3);
                                        guiLoader.getExamSystemController().showMediaAccessConfig();
                                        ((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
                                        evt.consume();
                                    }).showAndWait();
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Couldn't show dialogs", ex);
                }
            });
        } catch (IllegalArgumentException | IOException
                | ParserConfigurationException | SAXException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't initialize GUI", ex);
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Stops the backend tasks and uses System.exit() to ensure that everything
     * closes.
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        controller.closeApplication();
        System.exit(0);
    }

    private boolean isExamEnvironment() {
        return getParameters().getRaw().contains("examEnvironment");
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        WelcomeApplication.launch(args);
    }
}
