/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationInformationController implements Initializable, WelcomeApplicationViewController {


    @FXML
    private Label label_info_os;
    @FXML
    private Label label_info_version;
    
    WelcomeController controller;

    public WelcomeApplicationInformationController(WelcomeController controller)
    {
        this.controller = controller;
    }
    
    /**
     * Initializes the controller class.
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // TODO
    }    

    @Override
    public Pane getPane() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Label getLabel_info_os() {
        return label_info_os;
    }

    public Label getLabel_info_version() {
        return label_info_version;
    }

    
}
