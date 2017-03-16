/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationBackupController implements Initializable, WelcomeApplicationViewController {

    @FXML
    private Button btn_bu_help;
    @FXML
    private CheckBox cb_bu_backup;
    @FXML
    private CheckBox cb_bu_screenshot;
    @FXML
    private TextField txt_bu_src_path;
    @FXML
    private Button btn_bu_src_path;
    @FXML
    private TextField txt_bu_dest_path;
    @FXML
    private Button btn_bu_dest_path;
    @FXML
    private CheckBox cb_bu_use_local;
    @FXML
    private TextField txt_bu_remote_path;
    @FXML
    private Button btn_bu_remote_path;
    @FXML
    private CheckBox cb_bu_use_remote;
    @FXML
    private ChoiceBox<?> choice_bu_medium;
    @FXML
    private ChoiceBox<?> choice_bu_backup;

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

    @FXML
    private void onClickShowHelp(MouseEvent event) {
    }

    @FXML
    private void onClickSetBackupPath(MouseEvent event) {
    }
    
    @FXML
    private void onClickSetRemotePartition(MouseEvent event) {
    }

    @FXML
    private void onClickSetLocalFolder(MouseEvent event) {
    }
    
}
