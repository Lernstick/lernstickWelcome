/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;


/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationStartController implements Initializable {

    private Button FinishButton;
    private Button SaveButton;
    private VBox MenuBox;
    private AnchorPane MainPane;
    
    private WelcomeApplicationBackupController backup;
    private WelcomeApplicationFirewallController firewall;
    private WelcomeApplicationInformationController information;
    private WelcomeApplicationSystemController system;
    
    //placholder list
    private String[] menu = {"Information", "Firewall", "Backup", "System"};
    
    private ResourceBundle bundle;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;
        for(int i = 0; i < menu.length; ++i)
        {
            MenuBox.add(new Button(menu[i]));
        }
        information = new WelcomeApplicationInformationController();
        MainPane.add(new Pane ( information.getPane()));
    } 
    
     private void onFinishClickedAction(MouseEvent event) {
        ((Node)(event.getSource())).getScene().getWindow().hide();
        //create loading bar
    }
     
     private void onSaveClickedAction(MouseEvent event) {
        ((Node)(event.getSource())).getScene().getWindow().hide();
        //save all
    }
    
    
}
