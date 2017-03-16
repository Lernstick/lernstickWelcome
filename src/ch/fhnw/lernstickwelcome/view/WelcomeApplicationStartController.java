/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;


/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationStartController implements Initializable {

    @FXML
    private Button FinishButton;
    @FXML
    private Button SaveButton;
    
    
    private ArrayList<String> menuStrings = new ArrayList<String>();
    private ArrayList<String> menuStringsExam = new ArrayList<String>();
    
    private Scene backupEX, informationEX, systemEX, firewallEX;
    private Scene backup, information, system, firewall;
    
    private Button btnShowBackup, btnShowInfo, btnShowSystem, btnShowFirewall;
    private Button btnShowBackupEX, btnShowInfoEX, btnShowSystemEX, btnShowFirewallEX;
    
    private boolean isExam;
    
    
    private HashMap<String, Pane> panes;
    
    private WelcomeController controller;
    
    //Konstruktor (Controller, boolean ExamVersion)
    
    private ResourceBundle bundle;
    @FXML
    private VBox menuPane;
    @FXML
    private AnchorPane mainPane;

    public WelcomeApplicationStartController(WelcomeController controller, 
                                            boolean isExam, HashMap<String, Pane> panes) {
        this.controller = controller;
        menuStringsExam.add("Information");
        menuStringsExam.add("Firewall");
        menuStringsExam.add("Backup");
        menuStringsExam.add("System");
        this.isExam = isExam;
        this.panes = panes;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //im Controller bundle = rb;
            
        Iterator it = panes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            
            Button button = new Button(pair.getKey().toString());
            button.setOnMouseClicked((t) -> {
                showPane((Pane)pair.getValue());
            });
            mainPane.getChildren().add(button);
            
            it.remove(); 
        }

        
        
        
    } 
    
    private void showPane(Pane pane){
        Iterator it = panes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            ((Pane)pair.getValue()).setVisible(false);
            it.remove();
        }
        pane.setVisible(true);
    }
    
    @FXML
     private void onFinishClickedAction(MouseEvent event) {
        ((Node)(event.getSource())).getScene().getWindow().hide();
        //create progress bar
    }
     
    @FXML
     private void onSaveClickedAction(MouseEvent event) {
        ((Node)(event.getSource())).getScene().getWindow().hide();
        //save all
    }

  /*   private void onClickShowSystem*/
    
    
}
