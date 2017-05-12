/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author root
 */
public class InfodialogController implements Initializable {

    @FXML
    private Button btCancel;
    @FXML
    private Button btOk;
    @FXML
    private Label lbInfotext;
    
    private ResourceBundle rb;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.rb = rb;
    }    
    
    public void initDialog(String textid, EventHandler<ActionEvent> okAction) {
        lbInfotext.setText(rb.getString(textid));
        btOk.setOnAction(okAction);
        btCancel.setOnAction(e -> ((Stage) ((Node) e.getSource()).getScene().getWindow()).close());
    }
    
}
