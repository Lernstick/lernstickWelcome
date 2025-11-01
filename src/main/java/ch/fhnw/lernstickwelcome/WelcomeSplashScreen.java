/*
 * Copyright (C) 2020 Ronny Standtke <ronny.standtke@gmx.net>
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
package ch.fhnw.lernstickwelcome;

import static ch.fhnw.lernstickwelcome.model.WelcomeConstants.ICON_APPLICATION_FILE_PATH;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javafx.application.Preloader;
import javafx.application.Preloader.StateChangeNotification.Type;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Shows a splash screen during application startup
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class WelcomeSplashScreen extends Preloader {

    private Label applicationLabel;
    private ProgressBar progressBar;

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "ch.fhnw.lernstickwelcome.Bundle");
    private Stage stage;
    private int counter;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setScene(createPreloaderScene());
        stage.setTitle(BUNDLE.getString("Welcome.title"));
        stage.initStyle(StageStyle.UNDECORATED);
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        
        super.handleApplicationNotification(info);
        
        if (!stage.isShowing()) {
            stage.show();
        }
        
        if (info instanceof SplashScreenNotification splashScreenNotification) {

            // update app icon
            String path = ICON_APPLICATION_FILE_PATH + "/"
                    + splashScreenNotification.getIconName() + ".png";
            applicationLabel.setGraphic(new ImageView(new Image(
                    getClass().getResourceAsStream(path))));

            // update app name
            String name = splashScreenNotification.getName();
            try {
                name = BUNDLE.getString(name);
            } catch (MissingResourceException e) {
                // this is OK, only some applications are localized,
                // like "Additional multimedia formats" or "Additional fonts"
            }
            applicationLabel.setText(name);

            // update progress
            // (length is always the same, but our options are limited...)
            int length = splashScreenNotification.getLength();
            counter++;
            progressBar.setProgress(((double) counter) / length);
        }
    }

    @Override
    public void handleStateChangeNotification(
            StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == Type.BEFORE_START) {
            stage.hide();
        }
    }

    private Scene createPreloaderScene() {
        progressBar = new ProgressBar();

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));

        // logo
        ImageView imageView = new ImageView(new Image(
                getClass().getResourceAsStream("/icon/lernstick_bfh.png")));
        vBox.getChildren().add(imageView);

        // "checking" label
        Label checkingLabel = new Label(
                BUNDLE.getString("Checking_Installation_State"));
        checkingLabel.setAlignment(Pos.CENTER);
        checkingLabel.setMaxWidth(Integer.MAX_VALUE);
        vBox.getChildren().add(checkingLabel);

        // currently parsed application
        applicationLabel = new Label("application");
        applicationLabel.setMaxWidth(Integer.MAX_VALUE);
        applicationLabel.setPrefWidth(300);
        applicationLabel.setPrefHeight(48);
        vBox.getChildren().add(applicationLabel);

        // progress bar
        progressBar = new ProgressBar(-1);
        progressBar.setMaxWidth(Integer.MAX_VALUE);
        vBox.getChildren().add(progressBar);

        return new Scene(vBox);
    }
}
