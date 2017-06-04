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
package ch.fhnw.lernstickwelcome.fxmlcontroller.exam;

import ch.fhnw.lernstickwelcome.util.WelcomeUtil;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

/**
 * FXML Controller class for the exam info panel
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
        hyperlink.setOnAction(e
                -> WelcomeUtil.openLinkInBrowser("https://www.lernstick.ch"));
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
