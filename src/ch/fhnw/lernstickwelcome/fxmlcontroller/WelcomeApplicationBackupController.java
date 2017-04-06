/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationBackupController implements Initializable {

    private final static Integer[] backupFrequency = new Integer[] { 1, 2, 3, 4, 5, 10, 15, 30, 60 };

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
    private ChoiceBox<Number> choice_bu_backup;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choice_bu_backup.setConverter(new StringConverter<Number>() {
            
            @Override
            public String toString(Number v) {
                return v.intValue() + " " +  rb.getString("welcomeApplicationBackup.minutes");
            }

            @Override
            public Number fromString(String string) {
                return Integer.valueOf(string.split(" ")[0]);
            }
        });
        // Currently not supported
        choice_bu_medium.setVisible(false);
        
        choice_bu_backup.getItems().addAll(backupFrequency);
        
        // Bind options
        cb_bu_screenshot.disableProperty().bind(cb_bu_backup.selectedProperty().not());
        txt_bu_src_path.disableProperty().bind(cb_bu_backup.selectedProperty().not());
        btn_bu_src_path.disableProperty().bind(cb_bu_backup.selectedProperty().not());
        txt_bu_dest_path.disableProperty().bind(cb_bu_backup.selectedProperty().not().or(cb_bu_use_local.selectedProperty().not()));
        btn_bu_dest_path.disableProperty().bind(cb_bu_backup.selectedProperty().not());
        cb_bu_use_local.disableProperty().bind(cb_bu_backup.selectedProperty().not());
        txt_bu_remote_path.disableProperty().bind(cb_bu_backup.selectedProperty().not().or(cb_bu_use_remote.selectedProperty().not()));
        btn_bu_remote_path.disableProperty().bind(cb_bu_backup.selectedProperty().not());
        cb_bu_use_remote.disableProperty().bind(cb_bu_backup.selectedProperty().not());
        choice_bu_medium.disableProperty().bind(cb_bu_backup.selectedProperty().not());
        choice_bu_backup.disableProperty().bind(cb_bu_backup.selectedProperty().not());
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
             txt_bu_src_path.textProperty().set(file.getPath());
        }
    }
    
    @FXML
    private void onClickSetRemotePartition(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Remote Partition");
        File file = chooser.showDialog(new Stage());

        if(file!=null){
             txt_bu_remote_path.setText(file.getPath());
        }
    }

    @FXML
    private void onClickSetLocalFolder(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Set Local Folder");
        File file = chooser.showDialog(new Stage());

        if(file!=null){
             txt_bu_dest_path.setText(file.getPath());
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

    public ChoiceBox<Number> getChoice_bu_backup() {
        return choice_bu_backup;
    }
    
    public Button getBtnBuHelp() {
        return btn_bu_help;
    }
    
}
