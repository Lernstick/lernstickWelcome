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
package ch.fhnw.lernstickwelcome.fxmlcontroller.standard;

import ch.fhnw.util.ProcessExecutor;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

/**
 * FXML Controller class for the standard info panel
 *
 * @author Ronny Standtke <ronny.standtke@gmx.net>
 */
public class InformationController implements Initializable {

    @FXML
    private Label operatingSystemLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private Hyperlink hyperlink;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // hostServices.showDocument() doesn't work with OpenJDK/OpenJFX
        // getHostServices() just throws the following exception:
        // java.lang.ClassNotFoundException:
        //   com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory
        
        hyperlink.setOnAction(e -> {
            Thread browserThread = new Thread() {
                @Override
                public void run() {
                    ProcessExecutor processExecutor = new ProcessExecutor();
                    processExecutor.executeProcess("firefox",
                            "https://www.lernstick.ch");
                }
            };
            browserThread.start();
        });
    }

    /**
     * returns the operating system property
     *
     * @return the operating system property
     */
    public StringProperty operatingSystemProperty() {
        return operatingSystemLabel.textProperty();
    }

    /**
     * returns the version property
     *
     * @return the version property
     */
    public StringProperty versionProperty() {
        return versionLabel.textProperty();
    }
}
