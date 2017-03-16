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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author user
 */
public class WelcomeApplicationFirewallController implements Initializable, WelcomeApplicationViewController {

    @FXML
    private Button btn_fw_help;
    @FXML
    private CheckBox cb_fw_allow_monitoring;
    @FXML
    private TableView<?> tv_fw_allowed_sites;
    @FXML
    private TableColumn<?, ?> tab_fw_allowed_sites;
    @FXML
    private TableColumn<?, ?> tab_fw_allowed_sites_firewall;
    @FXML
    private TextField txt_fw_matching_string;
    @FXML
    private ComboBox<?> choice_fw_matchtype;
    @FXML
    private Button btn_fw_new_rule;
    @FXML
    private TableView<?> tv_fw_allowed_servers;
    @FXML
    private TableColumn<?, ?> tab_fw_server_protocol;
    @FXML
    private TableColumn<?, ?> tab_fw_server_ip;
    @FXML
    private TableColumn<?, ?> tab_fw_server_port;
    @FXML
    private TableColumn<?, ?> tab_fw_server_desc;
    @FXML
    private Button btn_fw_add_new_server;
    @FXML
    private TextField txt_fw_new_desc;
    @FXML
    private TextField txt_fw_new_port;
    @FXML
    private TextField txt_fw_new_ip;
    @FXML
    private ComboBox<?> choice_fw_protocol;

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
    private void onClickNewWebsiteRule(MouseEvent event) {
    }

    @FXML
    private void onClickNewServerRule(MouseEvent event) {
    }
    
}
