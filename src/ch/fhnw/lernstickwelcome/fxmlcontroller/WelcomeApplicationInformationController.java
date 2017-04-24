/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

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
public class WelcomeApplicationInformationController implements Initializable {


    @FXML
    private Label label_info_os;
    @FXML
    private Label label_info_version;
    @FXML
    private ImageView img;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        img.setImage(new Image(new File(WelcomeConstants.ICON_FILE_PATH + "/lernstick_usb.png").toURI().toString()));
    }
    
    public Label getLabel_info_os() {
        return label_info_os;
    }

    public Label getLabel_info_version() {
        return label_info_version;
    }

    
}
