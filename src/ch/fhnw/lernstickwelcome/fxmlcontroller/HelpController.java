/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.lernstickwelcome.fxmlcontroller;

import ch.fhnw.lernstickwelcome.model.help.HelpEntry;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;

/**
 * FXML Controller class
 *
 * @author user
 */
public class HelpController implements Initializable {

    @FXML
    private TreeView<HelpEntry> tvList;
    @FXML
    private WebView wvView;
    @FXML
    private Button btOk;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
    public TreeView<HelpEntry> getTvList() {
        return tvList;
    }
    
    public WebView getWvView() {
        return wvView;
    }
    
    public Button getBtOk() {
        return btOk;
    }
}
