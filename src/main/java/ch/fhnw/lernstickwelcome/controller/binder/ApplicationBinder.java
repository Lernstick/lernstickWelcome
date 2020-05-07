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
package ch.fhnw.lernstickwelcome.controller.binder;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import ch.fhnw.lernstickwelcome.model.application.ApplicationGroupTask;
import ch.fhnw.lernstickwelcome.model.application.ApplicationTask;
import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import ch.fhnw.lernstickwelcome.view.impl.ApplicationGroupView;
import ch.fhnw.lernstickwelcome.view.impl.ApplicationView;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 * Binder class to init bindings between view components and backend (model)
 * properties
 *
 * @author sschw
 */
public class ApplicationBinder {

    private final static Logger LOGGER = Logger.getLogger(
            ApplicationBinder.class.getName());
    private final VBox applicationBox;
    private final TabPane applicationTabPane;
    private final WelcomeController controller;
    private final Button helpButton;

    /**
     * Constructor of ApplicationBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param applicationContainer the gui container for the applications
     * @param helpButton the help button
     */
    public ApplicationBinder(WelcomeController controller,
            VBox applicationContainer, Button helpButton) {

        this.controller = controller;
        this.applicationBox = applicationContainer;
        this.applicationTabPane = null;
        this.helpButton = helpButton;
    }

    /**
     * Constructor of ApplicationBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param applicationContainer the gui container for the applications
     * @param helpButton the help button
     */
    public ApplicationBinder(WelcomeController controller,
            TabPane applicationTabPane, Button helpButton) {

        this.controller = controller;
        this.applicationBox = null;
        this.applicationTabPane = applicationTabPane;
        this.helpButton = helpButton;
    }

    /**
     * Loads an application group into the applicationContainer.
     *
     * @param appGroup the application group that should be loaded into the
     * container
     * @param binder the binder for the help to configure the help dialog.
     * @param help the help dialog that should be shown.
     */
    public void addApplicationGroup(
            ApplicationGroupTask appGroup, HelpBinder binder, Stage help) {

        ResourceBundle ressourceBundle = controller.getBundle();

        if (applicationTabPane == null) {
            // use vbox
            ApplicationGroupView groupView = new ApplicationGroupView();
            groupView.setTitle(ressourceBundle.getString(appGroup.getTitle()));
            addApplicationList(groupView.getAppContainer(),
                    appGroup.getApps(), binder, help);
            applicationBox.getChildren().add(groupView);

        } else {
            // use tab pane
            Tab tab = new Tab(ressourceBundle.getString(appGroup.getTitle()));
            VBox vBox = new VBox();
            ScrollPane scrollPane = new ScrollPane(vBox);
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);
            addApplicationList(vBox, appGroup.getApps(), binder, help);
            tab.setContent(scrollPane);
            applicationTabPane.getTabs().add(tab);
        }
    }

    /**
     * Loads applications of an application group into the applicationContainer.
     *
     * @param appGroup the application group that should be loaded into the
     * container
     * @param binder the binder for the help to configure the help dialog.
     * @param help the help dialog that should be shown.
     */
    public void addApplications(ApplicationGroupTask appGroup,
            HelpBinder binder, Stage help) {

        addApplicationList(applicationBox,
                appGroup.getApps(), binder, help);
    }

    private void addApplicationList(VBox container,
            List<ApplicationTask> applications, HelpBinder binder, Stage help) {

        ResourceBundle rb = controller.getBundle();
        boolean even = false;
        for (ApplicationTask app : applications) {
            ApplicationView appView = new ApplicationView(rb);
            try {
                appView.setTitle(rb.getString(app.getName()));
            } catch (Exception ex) {
                appView.setTitle(app.getName());
            }
            if (app.getDescription() != null
                    && !app.getDescription().isEmpty()) {
                try {
                    appView.setDescription(rb.getString(app.getDescription()));
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING,
                            "Description has key but key couldnt be load from "
                            + "bundle for app {0}", app.getName());
                }
            }

            if (app.getHelpPath() != null && !app.getHelpPath().isEmpty()) {
                String helpPath;
                try {
                    helpPath = rb.getString(app.getHelpPath());
                } catch (Exception ex) {
                    helpPath = app.getHelpPath();
                    LOGGER.log(Level.WARNING,
                            "Help Path couldn't be found in bundle for app {0}",
                            app.getName());
                    LOGGER.log(Level.WARNING,
                            "The help path itself is taken for validation. "
                            + "(Language support isn't guaranteed)");
                }
                if (helpPath.startsWith("HelpChapter:")) {
                    String helpChapter = helpPath.substring(12);
                    appView.setHelpAction(evt -> {
                        binder.setHelpEntryByChapter(helpChapter);
                        help.show();
                    });
                } else {
                    String helpUrl = helpPath;
                    appView.setHelpAction(evt
                            -> WelcomeUtil.openLinkInBrowser(helpUrl));
                }
            }
            if (app.getIcon() != null && !app.getIcon().isEmpty()) {
                String path = WelcomeConstants.ICON_APPLICATION_FILE_PATH
                        + "/" + app.getIcon() + ".png";
                try {
                    appView.setIcon(new Image(ApplicationBinder.class.
                            getResource(path).toExternalForm()));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING,
                            "Couldn't load icon {0}", app.getIcon());
                }
            }
            appView.installedProperty().bind(app.installedProperty());
            appView.installingProperty().bindBidirectional(
                    app.installingProperty());
            appView.setPrefWidth(container.getWidth());
            if (even) {
                appView.setBackground(new Background(new BackgroundFill(
                        Paint.valueOf("#00000011"),
                        CornerRadii.EMPTY, Insets.EMPTY)));
            }
            even = !even;
            container.getChildren().add(appView);
        }
    }

    /**
     * Open other view by clicking on help button
     *
     * @param chapter the help chapter for this application view
     * @param helpStage additional window showing help
     * @param help links to online user guide
     */
    public void initHelp(String chapter, Stage helpStage, HelpBinder help) {
        helpButton.setOnAction(evt -> {
            help.setHelpEntryByChapter(chapter);
            helpStage.show();
        });
    }
}
