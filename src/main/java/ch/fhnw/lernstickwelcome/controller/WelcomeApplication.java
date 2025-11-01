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

import ch.fhnw.lernstickwelcome.WelcomeSplashScreen;
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
import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import ch.fhnw.lernstickwelcome.util.FXMLGuiLoader;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

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
    private static final ResourceBundle BUNDLE
            = ResourceBundle.getBundle("ch.fhnw.lernstickwelcome.Bundle");

    private WelcomeController controller;
    private FXMLGuiLoader guiLoader;
    private Stage passwordChangeStage;

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("javafx.preloader",
                WelcomeSplashScreen.class.getName());
        WelcomeApplication.launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();

        controller = new WelcomeController();

        if (isExamEnvironment()) {
            controller.loadExamEnvironment();
        } else {
            controller.loadStandardEnvironment(this);
        }
    }

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

            guiLoader = new FXMLGuiLoader(
                    isExamEnvironment(), controller.getBundle());

            Stage helpStage = FXMLGuiLoader.createDialog(
                    primaryStage,
                    guiLoader.getHelpScene(),
                    controller.getBundle().getString(
                            "welcomeApplicationHelp.title"),
                    false
            );

            if (isExamEnvironment()) {

                if (controller.getSystemConfigTask().showPasswordDialog()) {

                    PasswordChangeBinder examPasswordChangeBinder
                            = new PasswordChangeBinder(controller,
                                    guiLoader.getPasswordChangeController());

                    examPasswordChangeBinder.initHandlers();

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

                FirewallDependenciesWarningBinder firewallDependenciesWarningBinder
                        = new FirewallDependenciesWarningBinder(controller,
                                guiLoader.getFirewallDependenciesWarningController());
                firewallDependenciesWarningBinder.initHandlers(
                        firewallPatternValidatorStage);

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
                        firewallPatternValidatorStage);
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
                        guiLoader.getNonFreeController().getTabPane(),
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
                        guiLoader.getAddSoftwareController().getTabPane(),
                        guiLoader.getAddSoftwareController().getHelpButton()
                );
                addAppsBinder.addApplicationGroup(
                        controller.getTeachingAppsTask(), helpBinder, helpStage);
                addAppsBinder.addApplicationGroup(
                        controller.getMiscAppsTask(), helpBinder, helpStage);
                addAppsBinder.addApplicationGroup(
                        controller.getGamesAppsTask(), helpBinder, helpStage);
                addAppsBinder.initHelp("2", helpStage, helpBinder);

                SystemBinder binder = new StandardSystemBinder(
                        controller, guiLoader.getStandardSystemController());
                binder.initBindings();
                binder.initHelp(helpStage, helpBinder);
            }

            Stage progressStage = FXMLGuiLoader.createDialog(primaryStage,
                    guiLoader.getProgressScene(),
                    controller.getBundle().getString(
                            "welcomeApplicationProgress.save"), true);

            controller.getTaskProcessor().setProgressStage(progressStage);

            ProgressBinder progressBinder = new ProgressBinder(
                    controller.getTaskProcessor(),
                    guiLoader.getProgressController());
            progressBinder.initBindings();

            MainBinder mainBinder = new MainBinder(
                    controller, guiLoader.getMainController());
            mainBinder.initHandlers(progressStage);

            Scene scene = guiLoader.getMainStage();
            primaryStage.setTitle(
                    controller.getBundle().getString("Welcome.title"));
            primaryStage.setScene(scene);
            primaryStage.show();
            if (isExamEnvironment()) {
                primaryStage.getIcons().add(new Image(
                        getClass().getResourceAsStream("/icon/lernstick_exam.png")));
            } else {
                primaryStage.getIcons().add(new Image(
                        getClass().getResourceAsStream("/icon/lernstick_edu.png")));
            }

            // Set close warnings
            primaryStage.setOnCloseRequest(evt -> {
                try {
                    if (isExamEnvironment()) {
                        if (controller.getSystemConfigTask().showPasswordDialog()) {
                            passwordChangeStage.showAndWait();
                        }
                        if (controller.getBackupTask().hasExchangePartition()
                                && !controller.getBackupTask().isBackupConfigured()) {
                            guiLoader.getInfotextDialog(primaryStage,
                                    "WelcomeApplication.Warning_No_Backup_Configured", e -> {
                                        guiLoader.getMainController().setView(2);
                                        ((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
                                        evt.consume();
                                    }).showAndWait();
                        }
                        if ((WelcomeUtil.isInternalFileSystemMountAllowed()
                                || WelcomeUtil.isExternalFileSystemMountAllowed())
                                && !evt.isConsumed()) {
                            guiLoader.getInfotextDialog(primaryStage,
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
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't initialize GUI", ex);
            System.exit(1);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "", ex);
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

    public static void playNotifySound() {
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.open(AudioSystem.getAudioInputStream(
                    WelcomeApplication.class.getResource(
                            "/sound/KDE_Notify.wav")));
            clip.start();
        } catch (UnsupportedAudioFileException | IOException
                | LineUnavailableException | IllegalArgumentException ex) {
            LOGGER.log(Level.INFO, "", ex);
        }
    }

    public static void showThrowable(Throwable exception) {
        LOGGER.log(Level.SEVERE, "", exception);
        if (exception instanceof ProcessingException processingException) {
            showProcessingException(processingException);
        } else {
            if (exception != null) {
                Throwable cause = exception.getCause();
                if (cause instanceof ProcessingException processingException) {
                    showProcessingException(processingException);
                } else {
                    showErrorMessage(null, exception.getMessage());
                }
            }
        }
    }

    public static void showErrorMessage(String headerText, String errorMessage) {
        FutureTask<Void> showErrorTask = new FutureTask<>(() -> {
            Alert warning = new Alert(
                    Alert.AlertType.ERROR, errorMessage, ButtonType.CLOSE);
            warning.setHeaderText(headerText);
            warning.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            warning.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            warning.showAndWait();
            return null;
        });

        Platform.runLater(showErrorTask);

        try {
            showErrorTask.get();
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }
    }

    private static void showProcessingException(ProcessingException exception) {
        showErrorMessage(BUNDLE.getString(exception.getTitleKey()),
                MessageFormat.format(BUNDLE.getString(exception.getMessage()),
                        exception.getMessageDetails()));
    }

    private boolean isExamEnvironment() {
        return getParameters().getRaw().contains("examEnvironment");
    }
}
