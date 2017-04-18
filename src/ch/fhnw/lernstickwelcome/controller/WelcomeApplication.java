/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller;

import ch.fhnw.lernstickwelcome.controller.binder.ExamFirewallBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ExamPasswordChangeBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ExamBackupBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ProgressBinder;
import ch.fhnw.lernstickwelcome.controller.binder.HelpBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ExamInformationBinder;
import ch.fhnw.lernstickwelcome.controller.binder.MainBinder;
import ch.fhnw.lernstickwelcome.controller.binder.ExamSystemBinder;
import ch.fhnw.lernstickwelcome.util.FXMLGuiLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author sschw
 */
public class WelcomeApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(WelcomeApplication.class.getName());
    private WelcomeController controller;
    private FXMLGuiLoader guiLoader;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try{
            controller = new WelcomeController();

            guiLoader = new FXMLGuiLoader(isExamEnvironment(), controller.getBundle(), controller.getRecApps(), controller.getTeachApps(), controller.getSoftwApps(), controller.getGamesApps());

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

                if(!controller.getSysconf().isPasswordChanged()) {
                    ExamPasswordChangeBinder examPasswordChangeBinder = new ExamPasswordChangeBinder(controller, guiLoader.getPasswordChange());
                    examPasswordChangeBinder.initHandlers(errorStage, guiLoader.getError());
                    Stage passwordChangeStage = FXMLGuiLoader.createDialog(
                            primaryStage, 
                            guiLoader.getPasswordChangeScene(), 
                            controller.getBundle().getString("welcomeApplicationPasswordChange.title"), 
                            false
                    );
                    passwordChangeStage.showAndWait();
                }

                HelpBinder helpBinder = new HelpBinder(controller, guiLoader.getHelp());
                helpBinder.initBindings();
                helpBinder.initHandlers();

                ExamInformationBinder examInformationBinder = new ExamInformationBinder(controller, guiLoader.getInformation());
                examInformationBinder.initBindings();

                ExamFirewallBinder examFirewallBinder = new ExamFirewallBinder(controller, guiLoader.getFirewall());
                examFirewallBinder.initBindings();
                examFirewallBinder.initHandlers(errorStage, guiLoader.getError(), controller.getBundle());
                examFirewallBinder.initHelp(helpStage, helpBinder);

                ExamBackupBinder examBackupBinder = new ExamBackupBinder(controller, guiLoader.getBackup());
                examBackupBinder.initBindings();
                examBackupBinder.initHelp(helpStage, helpBinder);

                ExamSystemBinder examSystemBinder = new ExamSystemBinder(controller, guiLoader.getSystem());
                examSystemBinder.initBindings();
                examSystemBinder.initHelp(helpStage, helpBinder);
            } else {
                controller.loadStandardEnvironment();

                HelpBinder helpBinder = new HelpBinder(controller, guiLoader.getHelp());
                helpBinder.initBindings();
                helpBinder.initHandlers();
            }

            ProgressBinder progressBinder = new ProgressBinder(controller, guiLoader.getProgress());
            progressBinder.initBindings();
            progressBinder.initHandlers(errorStage, guiLoader.getError());
            Stage progressStage = FXMLGuiLoader.createDialog(
                    primaryStage,
                    guiLoader.getProgressScene(),
                    controller.getBundle().getString("welcomeApplicationProgress.save"),
                    true
            );

            MainBinder mainBinder = new MainBinder(controller, guiLoader.getWelcomeApplicationStart());
            mainBinder.initHandlers(progressStage);

            Scene scene = guiLoader.getMainStage();
            primaryStage.setTitle(controller.getBundle().getString("Welcome.title"));
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setMinHeight(scene.getHeight());
            primaryStage.setMinWidth(scene.getWidth());
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, "Couldn't initialize GUI", ex);
        }
    }

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
