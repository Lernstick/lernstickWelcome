/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller.standard;

import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.*;
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
        ivInfo.setImage(new Image(new File(WelcomeConstants.ICON_FILE_PATH + "/lernstick_usb.png").toURI().toString()));
    }
    
    public Label getLbOs() {
        return lbOs;
    }

    public Label getLbVersion() {
        return lbVersion;
    }

    
}
