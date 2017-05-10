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
    private ProgressBar prog_inst_bar;
    @FXML
    private Label txt_inst_installtitle;
    @FXML
    private Label txt_inst_prc;
    @FXML
    private Label txt_inst_mesage;
    @FXML
    private ImageView img_value;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }   

    public ProgressBar getProg_inst_bar() {
        return prog_inst_bar;
    }

    public Label getTxt_inst_installtitle() {
        return txt_inst_installtitle;
    }

    public Label getTxt_inst_prc() {
        return txt_inst_prc;
    }

    public Label getTxt_inst_mesage() {
        return txt_inst_mesage;
    }

    public ImageView getImg_value() {
        return img_value;
    }
}
