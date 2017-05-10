/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

/**
 * FXML Controller class
 *
 * @author user
 */
public class ProgressController implements Initializable {

    @FXML
    private ProgressBar pbInstBar;
    @FXML
    private Label lbInstalltitle;
    @FXML
    private Label lbInstPrc;
    @FXML
    private Label lbMesage;
    @FXML
    private ImageView ivValue;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }   

    public ProgressBar getPbInstBar() {
        return pbInstBar;
    }

    public Label getLbInstalltitle() {
        return lbInstalltitle;
    }

    public Label getLbInstPrc() {
        return lbInstPrc;
    }

    public Label getLbMesage() {
        return lbMesage;
    }

    public ImageView getIvValue() {
        return ivValue;
    }
}
