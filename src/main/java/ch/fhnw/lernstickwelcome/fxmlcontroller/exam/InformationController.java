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

import ch.fhnw.lernstickwelcome.model.WelcomeConstants;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * FXML Controller class
 *
 * @author user
 */
public class InformationController implements Initializable {


    @FXML
    private Label lbOs;
    @FXML
    private Label lbVersion;
    @FXML
    private ImageView ivInfo;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ivInfo.setImage(new Image(
                InformationController.class.getResource(
                        WelcomeConstants.ICON_FILE_PATH + "/lernstick_usb.png"
                ).toExternalForm())
        );
    }
    
    public Label getLbOs() {
        return lbOs;
    }

    public Label getLbVersion() {
        return lbVersion;
    }

    
}
