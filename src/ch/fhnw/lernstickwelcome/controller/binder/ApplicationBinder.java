/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.lernstickwelcome.view.impl.ApplicationGroupView;
import ch.fhnw.lernstickwelcome.view.impl.ApplicationView;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 * Binder class to init bindings between view components and backend (model) properties
 * 
 * @author sschw
 */
public class ApplicationBinder {
    private final static Logger LOGGER = Logger.getLogger(ApplicationBinder.class.getName());
    private final VBox applicationContainer;
    private final WelcomeController controller;
    
    /**
     * Constructor of ApplicationBinder class
     * 
     * @param controller is needed to provide access to the backend properties
     * @param applicationContainer the gui container for the applications
     */
    public ApplicationBinder(WelcomeController controller, VBox applicationContainer) {
        this.controller = controller;
        this.applicationContainer = applicationContainer;
    }
    
    /**
     * Loads an application group into the applicationContainer.
     * @param appGroup the application group that should be loaded into the container
     * @param binder the binder for the help to configure the help dialog.
     * @param help the help dialog that should be shown.
     */
    public void addApplicationGroup(ApplicationGroupTask appGroup, HelpBinder binder, Stage help) {
        ResourceBundle rb = controller.getBundle();
        ApplicationGroupView groupView = new ApplicationGroupView();
        groupView.setTitle(rb.getString(appGroup.getTitle()));
        addApplicationList(groupView.getAppContainer(), appGroup.getApps(), binder, help);
        applicationContainer.getChildren().add(groupView);
    }
    
    /**
     * Loads applications of an application group into the applicationContainer.
     * @param appGroup the application group that should be loaded into the container
     * @param binder the binder for the help to configure the help dialog.
     * @param help the help dialog that should be shown.
     */
    public void addApplications(ApplicationGroupTask appGroup, HelpBinder binder, Stage help) {
        addApplicationList(applicationContainer, appGroup.getApps(), binder, help);
    }
    
    private void addApplicationList(VBox container, List<ApplicationTask> applications, HelpBinder binder, Stage help) {
        ResourceBundle rb = controller.getBundle();
        boolean even = false;
        for(ApplicationTask app : applications) {
            ApplicationView appView = new ApplicationView(rb);
            try {
                appView.setTitle(rb.getString(app.getName()));
            } catch(Exception ex) {
                appView.setTitle(app.getName());
            }
            try {
                appView.setDescription(rb.getString(app.getDescription()));
            } catch(Exception ex) {
                LOGGER.log(Level.WARNING, "Description has key but key couldnt be load from bundle for app {0}", app.getName());
            }
            
            if(app.getHelpPath() != null && !app.getHelpPath().isEmpty()) {
                if(app.getHelpPath().matches("([0-9].?)+")) {
                    appView.setHelpAction(evt -> {
                        binder.setHelpEntryByChapter(app.getHelpPath());
                        help.show();
                    });
                } else {
                    try {
                        String s = rb.getString(app.getHelpPath());
                        appView.setHelpAction(evt -> {
                            WelcomeUtil.openLinkInBrowser(s);
                        });
                    } catch(Exception ex) {
                        LOGGER.log(Level.WARNING, "Help Path not local nor key for url in bundle for app {0}", app.getName());
                    }
                }
            }
            if(app.getIcon() != null && !app.getIcon().isEmpty()) {
                String path = WelcomeConstants.ICON_APPLICATION_FILE_PATH + "/" + app.getIcon() + ".png";
                File f = new File(path);
                if(f.exists()) {
                    appView.setIcon(new Image(f.toURI().toString()));
                }
            }
            appView.disableProperty().bind(app.installedProperty());
            appView.installingProperty().bindBidirectional(app.installingProperty());
            appView.setPrefWidth(container.getWidth());
            if(even)
                appView.setBackground(new Background(new BackgroundFill(Paint.valueOf("#00000011"), CornerRadii.EMPTY, Insets.EMPTY)));
            even = !even;
            container.getChildren().add(appView);
        }
    }
}
