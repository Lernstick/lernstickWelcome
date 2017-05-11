package ch.fhnw.lernstickwelcome.fxmlcontroller.exam;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ch.fhnw.lernstickwelcome.model.firewall.WebsiteFilter;
import ch.fhnw.lernstickwelcome.view.impl.ButtonCell;
import ch.fhnw.lernstickwelcome.view.impl.FirewallAddButtonCell;
import ch.fhnw.lernstickwelcome.view.impl.FirewallOpenButtonCell;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author root
 */
public class FirewallPatternValidatorController implements Initializable {

    @FXML
    private Button btnCancel;
    @FXML
    private Button btnOk;
    @FXML
    private TableView<WebsiteFilter> tvPatternDependencies;
    @FXML
    private TableColumn<WebsiteFilter, String> tvPatternDependencies_pattern;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tvPatternDependencies_open;
    @FXML
    private TableColumn<WebsiteFilter, WebsiteFilter> tvPatternDependencies_add;

    public TableView<WebsiteFilter> getTvPatternDependencies() {
        return tvPatternDependencies;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tvPatternDependencies_pattern.setCellValueFactory(p -> p.getValue().searchCriteriaProperty());
        tvPatternDependencies_open.setCellFactory(c -> new FirewallOpenButtonCell(c.getTableView()));
        tvPatternDependencies_add.setCellFactory(c -> new FirewallAddButtonCell(c.getTableView()));
    }    
    
}
