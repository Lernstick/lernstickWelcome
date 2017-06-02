/*
 * Copyright (C) 2017 FHNW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller.exam;

import ch.fhnw.lernstickwelcome.view.impl.ToggleSwitch;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
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
public class BackupController implements Initializable {

    private final static Integer[] backupFrequency = new Integer[] { 1, 2, 3, 4, 5, 10, 15, 30, 60 };

    @FXML
    private Button btHelp;
    @FXML
    private ToggleSwitch tsBackup;
    @FXML
    private ComboBox<Number> cbMinutes;
    @FXML
    private ToggleSwitch tsScreenshot;
    @FXML
    private TextField tfSrcPath;
    @FXML
    private Button btSrcPath;
    @FXML
    private ToggleSwitch tsUseLocal;
    @FXML
    private TextField tfDestPath;
    @FXML
    private Button btDestPath;
    @FXML
    private ToggleSwitch tsUseRemote;
    @FXML
    private ComboBox<?> cbMedium;
    @FXML
    private TextField tfRemotePath;
    @FXML
    private Button btRemotePath;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbMinutes.setConverter(new StringConverter<Number>() {
            
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
        cbMedium.setVisible(false);
        cbMedium.setEditable(true);
        
        cbMinutes.getItems().addAll(backupFrequency);
        cbMinutes.setEditable(true);
        
        // Bind options
        tsScreenshot.disableProperty().bind(tsBackup.selectedProperty().not());
        tfSrcPath.disableProperty().bind(tsBackup.selectedProperty().not());
        btSrcPath.disableProperty().bind(tsBackup.selectedProperty().not());
        tfDestPath.disableProperty().bind(tsBackup.selectedProperty().not().or(tsUseLocal.selectedProperty().not()));
        btDestPath.disableProperty().bind(tsBackup.selectedProperty().not());
        tsUseLocal.disableProperty().bind(tsBackup.selectedProperty().not());
        tfRemotePath.disableProperty().bind(tsBackup.selectedProperty().not().or(tsUseRemote.selectedProperty().not()));
        btRemotePath.disableProperty().bind(tsBackup.selectedProperty().not());
        tsUseRemote.disableProperty().bind(tsBackup.selectedProperty().not());
        cbMedium.disableProperty().bind(tsBackup.selectedProperty().not());
        cbMinutes.disableProperty().bind(tsBackup.selectedProperty().not());
    }


    @FXML
    private void onClickSetBackupPath(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Backup Path");
        File file = chooser.showDialog(new Stage());

        if(file!=null){
             tfSrcPath.textProperty().set(file.getPath());
        }
    }
    
    @FXML
    private void onClickSetRemotePartition(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Remote Partition");
        File file = chooser.showDialog(new Stage());

        if(file!=null){
             tfRemotePath.setText(file.getPath());
        }
    }

    @FXML
    private void onClickSetLocalFolder(MouseEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Set Local Folder");
        File file = chooser.showDialog(new Stage());

        if(file!=null){
             tfDestPath.setText(file.getPath());
        }
    }

    public Button getBtHelp() {
        return btHelp;
    }

    public ToggleSwitch getTsBackup() {
        return tsBackup;
    }

    public ToggleSwitch getTsScreenshot() {
        return tsScreenshot;
    }

    public TextField getTfSrcPath() {
        return tfSrcPath;
    }

    public Button getBtSrcPath() {
        return btSrcPath;
    }

    public TextField getTfDestPath() {
        return tfDestPath;
    }

    public Button getBtDestPath() {
        return btDestPath;
    }

    public ToggleSwitch getTsUseLocal() {
        return tsUseLocal;
    }

    public TextField getTfRemotePath() {
        return tfRemotePath;
    }

    public Button getBtRemotePath() {
        return btRemotePath;
    }

    public ToggleSwitch getTsUseRemote() {
        return tsUseRemote;
    }

    public ComboBox<?> getCbMedium() {
        return cbMedium;
    }

    public ComboBox<Number> getCbMinutes() {
        return cbMinutes;
    }
    
    public Button getBtnBuHelp() {
        return btHelp;
    }
    
}
