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
import ch.fhnw.lernstickwelcome.view.impl.ApplicationGroupView;
import ch.fhnw.lernstickwelcome.view.impl.ApplicationView;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author sschw
 */
public class ApplicationBinder {
    private final VBox applicationContainer;
    private final WelcomeController controller;
    
    public ApplicationBinder(WelcomeController controller, VBox applicationContainer) {
        this.controller = controller;
        this.applicationContainer = applicationContainer;
    }
    
    public void addApplicationGroup(ApplicationGroupTask appGroup, HelpBinder binder, Stage help) {
        ResourceBundle rb = controller.getBundle();
        ApplicationGroupView groupView = new ApplicationGroupView();
        groupView.setTitle(rb.getString(appGroup.getTitle()));
        addApplicationList(groupView.getAppContainer(), appGroup.getApps(), binder, help);
        applicationContainer.getChildren().add(groupView);
    }
    
    public void addApplications(ApplicationGroupTask appGroup, HelpBinder binder, Stage help) {
        addApplicationList(applicationContainer, appGroup.getApps(), binder, help);
    }
    
    private void addApplicationList(VBox container, List<ApplicationTask> applications, HelpBinder binder, Stage help) {
        ResourceBundle rb = controller.getBundle();
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
                appView.setDescription(app.getDescription());
            }
            
            if(app.getHelpPath() != null && !app.getHelpPath().isEmpty()) {
                appView.setHelpAction(evt -> {
                    binder.setHelpEntryByChapter(app.getHelpPath());
                    help.show();
                });
            }
            if(app.getIcon() != null && !app.getIcon().isEmpty()) {
                String path = WelcomeConstants.ICON_APPLICATION_FILE_PATH + "/" + app.getIcon() + ".png";
                File f = new File(path);
                if(f.exists()) {
                    appView.setIcon(new Image(f.toURI().toString()));
                }
            }
            appView.setDisable(app.isInstalled());
            appView.installingProperty().bindBidirectional(app.installingProperty());
            appView.setPrefWidth(container.getWidth());
            container.getChildren().add(appView);
        }
    }
}
