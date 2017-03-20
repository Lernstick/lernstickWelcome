/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import java.io.File;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Backup Path");
        File file = chooser.showDialog(new Stage());

        if(file!=null){
             String path = file.getPath();
        }
    }
    
    @FXML
    private void onClickSetRemotePartition(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Remote Partition");
        File file = chooser.showDialog(new Stage());

        if(file!=null){
             String path = file.getPath();
        }
    }

    @FXML
    private void onClickSetLocalFolder(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Set Local Folder");
        File file = chooser.showDialog(new Stage());

        if(file!=null){
             String path = file.getPath();
        }
    }

    public Button getBtn_bu_help() {
        return btn_bu_help;
    }

    public CheckBox getCb_bu_backup() {
        return cb_bu_backup;
    }

    public CheckBox getCb_bu_screenshot() {
        return cb_bu_screenshot;
    }

    public TextField getTxt_bu_src_path() {
        return txt_bu_src_path;
    }

    public Button getBtn_bu_src_path() {
        return btn_bu_src_path;
    }

    public TextField getTxt_bu_dest_path() {
        return txt_bu_dest_path;
    }

    public Button getBtn_bu_dest_path() {
        return btn_bu_dest_path;
    }

    public CheckBox getCb_bu_use_local() {
        return cb_bu_use_local;
    }

    public TextField getTxt_bu_remote_path() {
        return txt_bu_remote_path;
    }

    public Button getBtn_bu_remote_path() {
        return btn_bu_remote_path;
    }

    public CheckBox getCb_bu_use_remote() {
        return cb_bu_use_remote;
    }

    public ChoiceBox<?> getChoice_bu_medium() {
        return choice_bu_medium;
    }

    public ChoiceBox<?> getChoice_bu_backup() {
        return choice_bu_backup;
    }
    
    
    
}
