/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.controller.binder.ApplicationBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ExamBackupBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ExamFirewallBinder;
import ch.fhnw.lernstickwelcome.controller.binder.InformationBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ExamPasswordChangeBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ExamSystemBinder;
import ch.fhnw.lernstickwelcome.controller.binder.HelpBinder;
import ch.fhnw.lernstickwelcome.controller.binder.MainBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ProgressBinder;
import ch.fhnw.lernstickwelcome.controller.binder.StdSystemBinder;
import ch.fhnw.lernstickwelcome.util.FXMLGuiLoader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
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
public class WelcomeApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(WelcomeApplication.class.getName());
    private WelcomeController controller;
    private FXMLGuiLoader guiLoader;

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
     * </ol>
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            controller = new WelcomeController();

            guiLoader = new FXMLGuiLoader(isExamEnvironment(), controller.getBundle());

            Stage errorStage = FXMLGuiLoader.createDialog(
                    primaryStage,
                    guiLoader.getErrorScene(),
                    controller.getBundle().getString("welcomeApplicationError.title"),
                    true
            );

            Stage helpStage = FXMLGuiLoader.createDialog(
                    primaryStage,
                    guiLoader.getHelpScene(),
                    controller.getBundle().getString("welcomeApplicationHelp.title"),
                    false
            );

            if (isExamEnvironment()) {
                controller.loadExamEnvironment();

                if (!controller.getSysconf().isPasswordChanged()) {
                    ExamPasswordChangeBinder examPasswordChangeBinder = new ExamPasswordChangeBinder(controller, guiLoader.getPasswordChangeController());
                    examPasswordChangeBinder.initHandlers(errorStage, guiLoader.getErrorController());
                    Stage passwordChangeStage = FXMLGuiLoader.createDialog(
                            primaryStage,
                            guiLoader.getPasswordChangeScene(),
                            controller.getBundle().getString("welcomeApplicationPasswordChange.title"),
                            false
                    );
                    passwordChangeStage.showAndWait();
                }

                HelpBinder helpBinder = new HelpBinder(controller, guiLoader.getHelpController());
                helpBinder.initBindings();
                helpBinder.initHandlers();

                InformationBinder examInformationBinder = new InformationBinder(controller, guiLoader.getInformationController());
                examInformationBinder.initBindings();

                ExamFirewallBinder examFirewallBinder = new ExamFirewallBinder(controller, guiLoader.getFirewallController());
                examFirewallBinder.initBindings();
                examFirewallBinder.initHandlers(errorStage, guiLoader.getErrorController());
                examFirewallBinder.initHelp(helpStage, helpBinder);

                ExamBackupBinder examBackupBinder = new ExamBackupBinder(controller, guiLoader.getBackupController());
                examBackupBinder.initBindings();
                examBackupBinder.initHelp(helpStage, helpBinder);

                ExamSystemBinder examSystemBinder = new ExamSystemBinder(controller, guiLoader.getSystemExamController());
                examSystemBinder.initBindings();
                examSystemBinder.initHelp(helpStage, helpBinder);
            } else {
                controller.loadStandardEnvironment();

                HelpBinder helpBinder = new HelpBinder(controller, guiLoader.getHelpController());
                helpBinder.initBindings();
                helpBinder.initHandlers();

                InformationBinder information = new InformationBinder(controller, guiLoader.getInformationController());
                information.initBindings();

                ApplicationBinder recAppsBinder = new ApplicationBinder(
                        controller, 
                        guiLoader.getRecommendedController().getVbApps(),
                        guiLoader.getRecommendedController().getBtn_sys_help()
                );
                recAppsBinder.addApplicationGroup(controller.getNonfreeApps(), helpBinder, helpStage);
                recAppsBinder.addApplicationGroup(controller.getUtilityApps(), helpBinder, helpStage);
                recAppsBinder.initHelp("1", helpStage, helpBinder);

                ApplicationBinder addAppsBinder = new ApplicationBinder(
                        controller, 
                        guiLoader.getAddSoftwareController().getVbApps(),
                        guiLoader.getAddSoftwareController().getBtn_sys_help()
                );
                addAppsBinder.addApplicationGroup(controller.getTeachApps(), helpBinder, helpStage);
                addAppsBinder.addApplicationGroup(controller.getSoftwApps(), helpBinder, helpStage);
                addAppsBinder.addApplicationGroup(controller.getGamesApps(), helpBinder, helpStage);
                addAppsBinder.initHelp("2", helpStage, helpBinder);

                StdSystemBinder stdSystemBinder = new StdSystemBinder(controller, guiLoader.getSystemStdController());
                stdSystemBinder.initBindings();
                stdSystemBinder.initHelp(helpStage, helpBinder);
            }

            ProgressBinder progressBinder = new ProgressBinder(controller, guiLoader.getProgressController());
            progressBinder.initBindings();
            progressBinder.initHandlers(errorStage, guiLoader.getErrorController());
            Stage progressStage = FXMLGuiLoader.createDialog(
                    primaryStage,
                    guiLoader.getProgressScene(),
                    controller.getBundle().getString("welcomeApplicationProgress.save"),
                    true
            );

            MainBinder mainBinder = new MainBinder(controller, guiLoader.getMainController());
            mainBinder.initHandlers(progressStage);

            Scene scene = guiLoader.getMainStage();
            primaryStage.setTitle(controller.getBundle().getString("Welcome.title"));
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setMinHeight(primaryStage.getHeight());
            primaryStage.setMinWidth(primaryStage.getWidth());
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't initialize GUI", ex);
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
        WelcomeApplication.launch(args);
    }
}
