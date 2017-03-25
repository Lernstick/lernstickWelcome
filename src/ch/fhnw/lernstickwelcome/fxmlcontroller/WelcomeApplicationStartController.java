/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

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
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
    
    
    private ResourceBundle bundle;
    @FXML
    private VBox MenuPane;
    @FXML
    private Pane MainPane;
    @FXML
    private SplitPane SplitPane;

    public void initializeController(boolean isExam, HashMap<String, Pane> panes) {
        this.isExam = isExam;
        this.panes = panes;
        
        for (Map.Entry<String, Pane> entry : panes.entrySet())
        {
            String text = entry.getKey();
            Button button = new Button(text);
            button.setOnMouseClicked((t) -> {
                //MainPane.getChildren().removeAll();
                entry.getValue().setVisible(true);
               // MainPane.getChildren().add((Pane)entry.getValue());
            }); 
           // MenuPane.getChildren();//.add(button);
        } 
        
       // MainPane.getChildren().add(panes.get("Information"));

        
  /*     Iterator it = panes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            
            Button button = new Button(pair.getKey().toString());
            button.setOnMouseClicked((t) -> {
                showPane((Pane)pair.getValue());
            });
            MainPane.getChildren().add(button);
            
            it.remove(); 
        }  */
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
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
